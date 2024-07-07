package com.book.user.controllers;

import com.book.notifacations.messages.Messages;
import com.book.notifacations.services.EmailService;
import com.book.user.dto.JwtAuthenticationResponse;
import com.book.user.dto.SignInRequest;
import com.book.user.dto.SignUpRequest;
import com.book.user.services.AuthenticationService;
import com.book.user.services.RandomSixDigitNumber;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final RandomSixDigitNumber randomSixDigitNumber;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpRequest request) {
        final String CODE = randomSixDigitNumber.getCode();
        emailService.sendSimpleEmail(
                request.getEmail(),
                Messages.CHECK_AUTORIZATIONS.getMessage(),
                Messages.CODE.getMessage() + CODE);
        authenticationService.setCode(CODE);
        authenticationService.setUsername(request.getUsername());
        authenticationService.setPassword(request.getPassword());
        authenticationService.setEmail(request.getEmail());
        return ResponseEntity.ok("Please check your email and send code");
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/sign-up-email")
    public JwtAuthenticationResponse signUpEmail(@RequestBody String code) {
        return authenticationService.signUp(code);
    }
}
