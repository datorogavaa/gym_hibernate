package com.crm.hibernate.security;

import com.crm.hibernate.service.BruteForceService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final BruteForceService bruteForceService;

    public AuthenticationEventListener(BruteForceService bruteForceService) {
        this.bruteForceService = bruteForceService;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        bruteForceService.loginFailed(username);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        bruteForceService.loginSucceeded(username);
    }
}