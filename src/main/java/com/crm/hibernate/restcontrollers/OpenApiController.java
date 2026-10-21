package com.crm.hibernate.restcontrollers;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@RestController
public class OpenApiController {

    private final RequestMappingHandlerMapping mapping;

    public OpenApiController(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @GetMapping(value = "/v3/api-docs", produces = "application/json")
    public String docs() {
        Paths paths = new Paths();
        Components components = new Components();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : mapping.getHandlerMethods().entrySet()) {
            HandlerMethod hm = e.getValue();
            if (hm.getBeanType() == OpenApiController.class
                    || hm.getBeanType() == SwaggerUiController.class
                    || hm.getBeanType().getName().startsWith("org.springframework")) continue;

            Set<String> patterns = e.getKey().getPathPatternsCondition() != null
                    ? e.getKey().getPathPatternsCondition().getPatternValues() : Set.of();
            Set<RequestMethod> methods = e.getKey().getMethodsCondition().getMethods();
            if (methods.isEmpty()) methods = Set.of(RequestMethod.GET);

            for (String pattern : patterns) {
                PathItem item = paths.computeIfAbsent(pattern, k -> new PathItem());
                for (RequestMethod m : methods) {
                    Operation op = buildOperation(hm, components);
                    switch (m) {
                        case GET -> item.setGet(op);
                        case POST -> item.setPost(op);
                        case PUT -> item.setPut(op);
                        case DELETE -> item.setDelete(op);
                        case PATCH -> item.setPatch(op);
                        default -> { }
                    }
                }
            }
        }

        OpenAPI api = new OpenAPI()
                .info(new Info().title("CRM API").version("1.0"))
                .paths(paths)
                .components(components);
        return Json.pretty(api);
    }

    private Operation buildOperation(HandlerMethod hm, Components components) {
        Operation op = new Operation()
                .operationId(hm.getBeanType().getSimpleName() + "_" + hm.getMethod().getName())
                .summary(hm.getMethod().getName())
                .addTagsItem(hm.getBeanType().getSimpleName());

        for (MethodParameter p : hm.getMethodParameters()) {
            PathVariable pv = p.getParameterAnnotation(PathVariable.class);
            RequestParam rp = p.getParameterAnnotation(RequestParam.class);
            if (pv != null) {
                String name = pv.value().isEmpty() ? p.getParameterName() : pv.value();
                op.addParametersItem(new Parameter().in("path").name(name).required(true)
                        .schema(schemaFor(p.getParameterType(), components)));
            } else if (rp != null) {
                String name = rp.value().isEmpty() ? p.getParameterName() : rp.value();
                op.addParametersItem(new Parameter().in("query").name(name).required(rp.required())
                        .schema(schemaFor(p.getParameterType(), components)));
            } else if (p.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)) {
                op.setRequestBody(new RequestBody().required(true).content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(schemaFor(p.getGenericParameterType(), components)))));
            }
        }

        ApiResponse ok = new ApiResponse().description("OK");
        Class<?> ret = hm.getMethod().getReturnType();
        if (ret != void.class && ret != Void.class) {
            ok.content(new Content().addMediaType("application/json", new MediaType()
                    .schema(schemaFor(hm.getMethod().getGenericReturnType(), components))));
        }
        return op.responses(new ApiResponses().addApiResponse("200", ok));
    }

    private Schema<?> schemaFor(java.lang.reflect.Type type, Components components) {
        ResolvedSchema rs = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(type).resolveAsRef(true));
        if (rs == null || rs.schema == null) return new ObjectSchema();
        if (rs.referencedSchemas != null) rs.referencedSchemas.forEach(components::addSchemas);
        return rs.schema;
    }
}