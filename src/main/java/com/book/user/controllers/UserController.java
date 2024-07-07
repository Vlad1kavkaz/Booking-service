package com.book.user.controllers;

import com.book.user.dto.SignUpRequest;
import com.book.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/update")
    public String updateUser(@AuthenticationPrincipal UserDetails currentUser, @RequestBody SignUpRequest userUpdate) {
        userService.updateUser(currentUser.getUsername(), userUpdate);
        return "User details updated successfully";
    }
}
