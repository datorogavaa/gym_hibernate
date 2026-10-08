package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthenticationRestController(AuthenticationService authenticationService, UserService userService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password) {

        authenticationService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String username,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            userService.changePassword(username, oldPassword, username,newPassword );
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
