package com.crm.hibernate.security;

import com.crm.hibernate.service.BruteForceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationEventListenerTest {

    @Mock
    private BruteForceService bruteForceService;

    @Test
    void failedLoginIncrementsUserFailures() {
        AuthenticationEventListener listener = new AuthenticationEventListener(bruteForceService);
        var authentication = new UsernamePasswordAuthenticationToken("bob.taylor", "bad");

        listener.onFailure(new AuthenticationFailureBadCredentialsEvent(
                authentication, new BadCredentialsException("bad credentials")));

        verify(bruteForceService).loginFailed("bob.taylor");
    }

    @Test
    void successfulLoginResetsUserFailures() {
        AuthenticationEventListener listener = new AuthenticationEventListener(bruteForceService);
        var authentication = new UsernamePasswordAuthenticationToken("bob.taylor", "secret");

        listener.onSuccess(new AuthenticationSuccessEvent(authentication));

        verify(bruteForceService).loginSucceeded("bob.taylor");
    }
}
