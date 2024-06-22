package com.yoo.duplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.http.HttpRequest;


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
         * This domain is ::: http://backend
         * */
        String authorization =  httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String xRealIp = httpServletRequest.getHeader("X-Real-IP");
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        String host = httpServletRequest.getHeader(HttpHeaders.HOST);


        StringBuilder result = new StringBuilder("This domain is ::: ");
        result.append(ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
        result.append("            ");
        result.append("// Server Port is ::: ");
        result.append(port);
        result.append("            ");
        result.append("// authorization is ::: ");
        result.append(authorization);
        result.append("            ");
        result.append("// Client Access Ip is ::: " + httpServletRequest.getRemoteAddr());
        result.append("            ");
        result.append("// xForwardedFor Ip is ::: " + xForwardedFor);
        result.append("            ");
        result.append("// xRealIp  is ::: " + xRealIp);
        result.append("            ");
        result.append("// Host is ::: " + host);
        result.append("            ");

        return result.toString();
    }
}
