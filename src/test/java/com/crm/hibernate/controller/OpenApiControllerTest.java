package com.crm.hibernate.controller;

import com.crm.hibernate.restcontrollers.OpenApiController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenApiControllerTest {

    @Mock
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @InjectMocks
    private OpenApiController openApiController;

    // სატესტო კონტროლერი Swagger/OpenAPI გენერაციის შესამოწმებლად
    static class TestController {
        public String sampleEndpoint(String id) {
            return "ok";
        }
    }

    @Test
    void docsGeneratesOpenApiJson() throws NoSuchMethodException {
        // Arrange
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();

        Method method = TestController.class.getMethod("sampleEndpoint", String.class);
        TestController testBean = new TestController();
        HandlerMethod handlerMethod = new HandlerMethod(testBean, method);

        PathPatternsRequestCondition pathCondition = mock(PathPatternsRequestCondition.class);
        when(pathCondition.getPatternValues()).thenReturn(java.util.Set.of("/api/test"));

        RequestMethodsRequestCondition methodsCondition = mock(RequestMethodsRequestCondition.class);
        when(methodsCondition.getMethods()).thenReturn(java.util.Set.of(RequestMethod.GET));

        RequestMappingInfo mappingInfo = mock(RequestMappingInfo.class);
        when(mappingInfo.getPathPatternsCondition()).thenReturn(pathCondition);
        when(mappingInfo.getMethodsCondition()).thenReturn(methodsCondition);

        handlerMethods.put(mappingInfo, handlerMethod);

        when(requestMappingHandlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        // Act
        String jsonResult = openApiController.docs();

        // Assert
        assertNotNull(jsonResult);
        assertTrue(jsonResult.contains("\"title\" : \"CRM API\""));
        assertTrue(jsonResult.contains("\"/api/test\""));
        assertTrue(jsonResult.contains("\"operationId\" : \"TestController_sampleEndpoint\""));
    }
}