package com.example.framework.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.framework.common.ApiResponse;
import com.example.framework.service.CalciteQueryService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<List<Map<String, Object>>> query(@RequestParam @NotBlank String sql) {
        return ApiResponse.success(calciteQueryService.query(sql));
    }
}
