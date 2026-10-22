package com.crm.hibernate.restcontrollers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerUiController {

    @GetMapping(value = "/swagger-ui.html", produces = "text/html;charset=UTF-8")
    public String ui(HttpServletRequest req) {
        String ctx = req.getContextPath();
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"><title>CRM API</title>
          <link rel="stylesheet" href="%s/webjars/swagger-ui/5.11.8/swagger-ui.css">
        </head>
        <body>
          <div id="swagger-ui"></div>
          <script charset="UTF-8" src="%s/webjars/swagger-ui/5.11.8/swagger-ui-bundle.js"></script>
          <script>
            window.ui = SwaggerUIBundle({ url: "%s/v3/api-docs", dom_id: "#swagger-ui" });
          </script>
        </body>
        </html>
        """.formatted(ctx, ctx, ctx);
    }
}