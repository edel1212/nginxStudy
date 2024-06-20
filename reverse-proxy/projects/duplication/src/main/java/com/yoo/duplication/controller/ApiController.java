package com.yoo.duplication.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class ApiController {
    @Value("${server.port}")
    private Integer port;
    @GetMapping("/greeting")
    public String greeting() {
        return "This port is ::: " + port;
    }
}
