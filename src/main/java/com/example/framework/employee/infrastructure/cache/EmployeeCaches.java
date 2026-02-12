package com.example.framework.employee.infrastructure.cache;

import com.example.framework.employee.domain.model.Employee;
import com.github.benmanes.caffeine.cache.Cache;
  import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;

/**
 * 本地缓存
 */
public class EmployeeCaches {

    public static final Cache<Long, Optional<Employee>> EMPLOYEE_LOCAL =
            Caffeine.newBuilder()
                    .maximumSize(200_000)
                    .expireAfterWrite(Duration.ofSeconds(30))
                    .build();

}
