package com.crm.hibernate.controller;

import com.crm.hibernate.controllers.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    private final HomeController controller = new HomeController();

    @Test
    void homePageAddsAuthenticatedUsername() {
        var model = new ExtendedModelMap();
        var authentication = new UsernamePasswordAuthenticationToken(
                "bob.taylor", "secret", AuthorityUtils.createAuthorityList("ROLE_TRAINEE"));

        assertEquals("index", controller.homePage(model, authentication));
        assertEquals("bob.taylor", model.get("username"));
    }

    @Test
    void homePageWorksWithoutAuthentication() {
        var model = new ExtendedModelMap();

        assertEquals("index", controller.homePage(model, null));
        org.junit.jupiter.api.Assertions.assertNull(model.get("username"));
    }
}
