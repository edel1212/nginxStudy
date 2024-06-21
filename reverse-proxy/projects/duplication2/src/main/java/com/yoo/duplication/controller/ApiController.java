package com.yoo.duplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
@RequestMapping("/api")
public class ApiController {
    @Value("${server.port}")
    private Integer port;
    @GetMapping
    public String greeting(HttpServletRequest httpServletRequest) {
        /**
         * ℹ️ 예상 Log
         * "This domain is ::: " + 192.168.1.45:8081
         *
         * 👉 결과
         * This domain is ::: This domain is ::: http://backend /// Port is ::: 8082 ///// authorization :: Bearer test
         * */
        String authorization =  httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return "This domain is ::: " + ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                + " /// Port is ::: " + port + " ///// authorization :: " + authorization;
    }
}
