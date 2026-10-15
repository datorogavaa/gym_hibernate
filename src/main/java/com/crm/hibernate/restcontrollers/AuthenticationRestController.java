package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Authentication and password management")
public class AuthenticationRestController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationRestController.class);

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthenticationRestController(AuthenticationService authenticationService, UserService userService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Login", description = "Validates username and password")
    @ApiResponse(responseCode = "200", description = "Authentication succeeded")
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        authenticationService.authenticate(username, password);
        log.info("Successful login for user {}", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Changes the user's password after validating the current one")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam("username") String username,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        try {
            userService.changePassword(username, oldPassword, username,newPassword );
            log.info("Password changed for user {}", username);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.warn("Password change rejected for user {}", username);
            return ResponseEntity.status(401).build();
        }
    }
}
