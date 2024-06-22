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
        result.append("// 요청 받은 Access Ip is ::: " + httpServletRequest.getRemoteAddr());
        result.append("            ");
        result.append("// 실제 접근하는 사용자 IP  is ::: " + xRealIp);
        result.append("            ");
        result.append("// xForwardedFor Ip is ::: " + xForwardedFor);
        result.append("            ");
        result.append("// Host is ::: " + host);
        result.append("            ");

        /**
         * ℹ️ 단일 서버 Log
         * This domain is ::: http://localhost:8081
         * // Server Port is ::: 8081
         * // authorization is ::: null
         * // Client Access Ip is ::: 0:0:0:0:0:0:0:1
         * // xForwardedFor Ip is ::: null
         * // xRealIp is ::: null
         * // Host is ::: localhost:8081
         *
         * 👉 Nginx 사용 Log
         * This domain is ::: http://localhost
         * // Server Port is ::: 8081
         * // authorization is ::: null
         * // Client Access Ip is ::: 192.168.64.2  << Nginx를 타고 들어와서 실제 사용자 Ip가 아닌 내부 유동IP로 접근되는 것을 확인 할 수 있다
         * // xForwardedFor Ip is ::: 192.168.65.1
         * // xRealIp is ::: 192.168.65.1
         * // Host is ::: localhost
         * */
        return result.toString();
    }
}
