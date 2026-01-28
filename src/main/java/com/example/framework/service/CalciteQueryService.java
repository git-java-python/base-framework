package com.example.framework.service;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CalciteQueryService {

    private final JdbcTemplate calciteJdbcTemplate;

    public CalciteQueryService(DataSource calciteDataSource) {
        this.calciteJdbcTemplate = new JdbcTemplate(calciteDataSource);
    }

    public List<Map<String, Object>> query(String sql) {
        return calciteJdbcTemplate.queryForList("select * from employee");
    }
}
