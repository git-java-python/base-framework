package com.example.framework.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.framework.shared.exception.ApiResponse;
import com.example.framework.service.CalciteQueryService;

import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calcite")
@Validated
public class CalciteController {

    private final CalciteQueryService calciteQueryService;

    public CalciteController(CalciteQueryService calciteQueryService) {
        this.calciteQueryService = calciteQueryService;
    }

    @SaCheckLogin
    @GetMapping("/query")
    public ApiResponse<List<Map<String, Object>>> queryUsers(  ) {
        return ApiResponse.success(calciteQueryService.queryUsers( ));
    }
}
