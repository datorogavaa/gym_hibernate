package com.crm.hibernate.controller;

import com.crm.hibernate.restcontrollers.SwaggerUiController;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwaggerUiControllerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SwaggerUiController swaggerUiController;

    @Test
    void uiReturnsHtmlWithContextPath() {
        // Arrange
        when(request.getContextPath()).thenReturn("/hibernate");

        // Act
        String html = swaggerUiController.ui(request);

        // Assert
        assertNotNull(html);
        assertTrue(html.contains("<title>CRM API</title>"));
        assertTrue(html.contains("href=\"/hibernate/webjars/swagger-ui/5.11.8/swagger-ui.css\""));
        assertTrue(html.contains("src=\"/hibernate/webjars/swagger-ui/5.11.8/swagger-ui-bundle.js\""));
        assertTrue(html.contains("url: \"/hibernate/v3/api-docs\""));
    }
}