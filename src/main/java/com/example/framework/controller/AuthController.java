package com.example.framework.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.framework.shared.exception.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<String> login(@NotBlank @RequestParam("userId")  String userId) {
        StpUtil.login(userId);
        return ApiResponse.success(StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.success(null);
    }
}
