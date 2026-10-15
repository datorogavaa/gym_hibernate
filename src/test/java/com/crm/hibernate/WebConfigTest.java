package com.crm.hibernate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @InjectMocks
    private WebConfig webConfig;

    @Test
    void configuresSwaggerResourceHandlers() {
        when(resourceHandlerRegistry.addResourceHandler("/swagger-ui/**"))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistry.addResourceHandler("/v3/api-docs/**"))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.addResourceLocations(
                "classpath:/META-INF/resources/webjars/swagger-ui/5.11.8/"))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.addResourceLocations("classpath:/META-INF/resources/"))
                .thenReturn(resourceHandlerRegistration);

        webConfig.addResourceHandlers(resourceHandlerRegistry);

        verify(resourceHandlerRegistry).addResourceHandler("/swagger-ui/**");
        verify(resourceHandlerRegistration).addResourceLocations(
                "classpath:/META-INF/resources/webjars/swagger-ui/5.11.8/");
        verify(resourceHandlerRegistry).addResourceHandler("/v3/api-docs/**");
        verify(resourceHandlerRegistration).addResourceLocations("classpath:/META-INF/resources/");
    }
}
