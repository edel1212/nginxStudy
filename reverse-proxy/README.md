# ReverseProxy

- 클라이언트의 요청을 서버로 전달하고, 서버의 응답을 클라이언트로 전달하는 중계 역할을 하는 서버입니다.
- 일반적으로, Reverse Proxy는 서버 앞단에 위치하여 여러 가지 중요한 기능을 수행합니다.

## 주요 기능

- ### 로드 밸런싱 (Load Balancing)
  - 여러 대의 서버로 들어오는 트래픽을 분배하여 각 서버의 부하를 고르게 분산
    - 서버 과부하를 방지 및 가용성과 성능을 향상
  - `Default Load Balancing` 설정은 **라운드로빈(Round-robin)**
    - 설정 종류
      - `라운드로빈(Round-robin)`은 기본으로 사용하는 메서드로 모든 서버에 동등하게 요청을 분산한다.
      - `least_conn`은 연결이 가장 작은 서버로 요청을 보낸다.
      - `ip_hash`는 클라이언트 IP주소를 기준으로 요청을 분배한다. IP주소가 같다면, 동일한 서버로 요청을 전송한다.
      - `least_time`는 NginX Plus에서 지원한다. 평균 레이턴시와 연결을 기준으로 검사해서 로드가 적은 서버로 요청을 보낸다.
- ### 보안 강화
  - 접적인 서버 노출을 막을 수 있음
    - 서버의 IP 주소와 구성 정보가 외부에 노출되지 않도록 보호
- ### 캐싱 (Caching)
  - 자주 요청되는 콘텐츠를 캐싱하여, 동일한 요청이 들어올 때 백엔드 서버에 도달하지 않고 빠르게 응답 가능
    - 서버 성능 개선 및 응답 시간 단축 가능

## 작동 방식

- 클라이언트와 서버 사이에서 중개자 열활을 함
- 흐름
  - 1 . 클라이언트는 `Reverse Proxy`를 서버로 인식하고 요청을 보냄
  - 2 . Reverse Proxy는 해당 요청을 적절한 백엔드 서버로 전달하고, 서버로부터 응답을 받은 후 이를 클라이언트에게 반환
  - ex) `[Client] → [Reverse Proxy] → [Backend Server(s)]`

## Nginx 설정

```properties
# ℹ️ worker_processes 설정은 하드웨어의 core 수에 맞추는게 좋다 - auto 설정 시 자동
#
# > 주의 사항
#   💬 연결하려는 upstream(서버들)의 주소 설정
#      - nginx만 Docker일 경우     :: `server host.docker.internal:{지정 포트 번호};`
#      - 동일 Docker Network 경우  ::` server service1:3306;`
```

### Nginx만 Docker 사용

- #### docker compose

```yaml
# docker-compose.yml
services:
  nginx:
    image: nginx:latest
    container_name: reverse-proxy
    ports:
      - "80:80"
    # Volumes 설정을 통해 nginx.conf 설정을 동기화 시킴
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

- #### nginx.conf

```yaml
# /etc/nginx/nginx.conf

# 전역 컨텍스트
# 💬 보안 설정을 위함
user nginx;  # Nginx가 실행되는 사용자
worker_processes auto;  # 자동으로 워커 프로세스 수를 설정
# 프로세스 식별을 위한 파일 생성 위치를 지정
pid /run/nginx.pid;

events {
    worker_connections 1024;  # 최대 연결 수 설정
}

# HTTP 컨텍스트
http {
    # ℹ️ 원한다면 아래에 로드밸런싱에 사용될 메서드를 지정 가능
    ## {Load Balancing Method} # 로드 밸런싱 메서드 지정 ex) ip_hash;

    # Upstream 블록
    upstream backend {
        ## ℹ️ 로드 밸런싱 도메인 정보
        server 192.168.1.45:8081;
        server 192.168.1.45:8082;
    }

    # 서버 블록
    server {
        listen 80;  # 이 서버가 요청을 수신하는 포트

        # 위치 블록
        location / {
            proxy_pass http://backend;  # 'backend' 업스트림 그룹으로 요청 전달
        }
    }
}
```

### Nginx, Server Dokcer 사용

```properties
# ℹ️ 주의 사항
#    - nginx, servers 는 모두 같은 네트워크를 사용해서 연결 되어야 한다.
#       ㄴ> 그렇지 않을 경우 nginxrk upstream을 찾지 못함
#    - sever들은 꼭 포트 포워딩을 해줘야 한다.
```

- #### docker compose

```yaml
# docker-compose.yml
services:
  nginx:
    image: nginx:latest
    container_name: nginx_load_balancer
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - app1
      - app2
    networks:
      - {지정 값}

  app1:
    image: openjdk:17
    container_name: spring_app1
    ports:
      - "8080:8080"
    volumes:
      - ./{내가 지정한 파일명.jar}:/app/{내가 지정한 파일명.jar}
    command:
      ["java", "-jar", "/app/{내가 지정한 파일명.jar}", "--spring.profiles.active={구분 값}"]
    networks:
      - {지정 값}

  app2:
    image: openjdk:17
    container_name: spring_app2
    ports:
      - "8081:8081"
    volumes:
      - ./{내가 지정한 파일명.jar}:/app/{내가 지정한 파일명.jar}
    ["java", "-jar", "/app/{내가 지정한 파일명.jar}", "--spring.profiles.active={구분 값}"]
    networks:
      - {지정 값}

networks:
  {지정 값}:
    driver: bridge
```

- #### nginx.conf

```yaml
# /etc/nginx/nginx.conf

user nginx;
worker_processes auto;

pid /run/nginx.pid;

events {
  worker_connections 1024;
}

http {
  upstream myapp {
    server {같은 네트워크 내 컨테이너명}:8080;
    server {같은 네트워크 내 컨테이너명}:8081;
  }

  server {
    listen 80;

    location / {
      proxy_pass http://myapp;  # Forward to the upstream group
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
    }
  }
}
```

- ### Servers(2개 적용)

  - 간단하게 요청 -> 해당 포트를 응답하는 서버를 구성
    - ℹ️ 해당 서버의 도메인 정보를 확인하는 코드를 확인해보니 Nginx에서 설정한 `upstream`의 주소로 반환하는 것을 확인 `upstream {{지정명}}`
      - 하단 `proxy_pass http://backend;`가 실질적으로 `revers proxy` 해줌
  - Controller
    ```java
    @RestController
    @RequestMapping("/api")
    public class ApiController {
        @Value("${server.port}")
        private Integer port;
        @GetMapping
        public String greeting() {
            /**
             * ℹ️ 예상 Log
             * "This domain is ::: " + 192.168.1.45:8081
             *
             * 👉 결과
             * This domain is ::: http://backend
             * */
            return "This domain is ::: " + ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                    + " /// Port is ::: " + port;
        }
    }
    ```

- #### Hedaer 값 적용 확인

  - 문제 없이 Header값이 잘 전달 된다.
  - Controller

    ```java
    @RestController
    @RequestMapping("/api")
    public class ApiController {

        @Value("${server.port}")
        private Integer port;

        @GetMapping
        public String greeting(HttpServletRequest httpServletRequest) {

            String authorization =  httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            // This domain is ::: This domain is ::: http://backend /// Port is ::: 8082 ///// authorization :: Bearer test
            return "This domain is ::: " + ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                    + " /// Port is ::: " + port + " ///// authorization :: " + authorization;
        }
    }
    ```

## 접근자 확인 설정

- `nginx.conf`파일 내 설정으로 접근을 요청하는 실제 상용자의 IP 및 기존의 연결 전 IP 및 호스트 정보를 `Header`를 통해 확인 가능하다
- ### 설정 옵션 및 설명
  - `X-Real-IP`
    - 사용자의 실제 IP를 확인 가능함
      - 필요성 : 프록시 서버(예: NGINX)가 클라이언트와 서버 간의 중간 역할을 할 때, 백엔드 서버는 클라이언트의 원래 IP 주소를 알지 못함 내부에서 요청하기 때문임
  - `X-Forwarded-For`
    - 실제 IP 주소와 함께 기존의 X-Forwarded-For 헤더 값을 추가
      - 필요성 : 여러 프록시 서버를 거치는 경우, 각 서버는 요청을 전달하면서 원래 클라이언트 IP와 이전 프록시 서버의 IP를 X-Forwarded-For 헤더에 추가합니다. 이를 통해 백엔드 서버는 요청이 거쳐온 전체 IP 주소 체인 확인 가능
  - `Host`
    - 클라이언트가 원래 보낸 Server의 도메인 값 확인.
      - 필요성 : 클라이언트가 보낸 Host 헤더는 원래 요청의 대상 도메인 이름을 나타냅니다.
- ### 설정 (`nginx.conf`)

  ```yaml
  # /etc/nginx/nginx.conf

  user nginx;  # Nginx가 실행되는 사용자
  worker_processes auto;  # 자동으로 워커 프로세스 수를 설정
  pid /run/nginx.pid;  # PID 파일의 위치

  events {
      worker_connections 1024;  # 최대 연결 수 설정
  }

  # HTTP 컨텍스트
  http {
      # Upstream 블록
      upstream backend {
          server 192.168.64.1:8081;
          server 192.168.64.1:8082;
      }

      # 서버 블록
      server {
          listen 80;  # 이 서버가 요청을 수신하는 포트

          # 위치 블록
          location / {
              #  ℹ️ 'backend' 업스트림 그룹으로 요청 전달 - Nginx가 전달 받은 요청을 해당 프록시로 전달
              #
              # 예시
              # Alice's Request --> Nginx (http://nginx.example.com/api/data)
              #          |
              #          V
              #       Backend Server (http://localhost:8080/api/data)
              proxy_pass http://backend;
              # ℹ️ 사용자의 실제 IP를 확인 가능함
              #  ㄴ> 필요성 : 프록시 서버(예: NGINX)가 클라이언트와 서버 간의 중간 역할을 할 때, 백엔드 서버는 클라이언트의 원래 IP 주소를 알지 못함 내부에서 요청하기 때문임
              #     ㄴ> ex) 단일 서버일 경우 127.0.0.1로 오던 IP가 Nginx로 구동 후 확인 결과 Client Access Ip is ::: 192.168.64.2
              proxy_set_header X-Real-IP $remote_addr;
              # ℹ️ 실제 IP 주소와 함께 기존의 X-Forwarded-For 헤더 값을 추가
              #  ㄴ> 필요성 : 여러 프록시 서버를 거치는 경우, 각 서버는 요청을 전달하면서 원래 클라이언트 IP와 이전 프록시 서버의 IP를 X-Forwarded-For 헤더에 추가합니다. 이를 통해 백엔드 서버는 요청이 거쳐온 전체 IP 주소 체인 확인 가능
              proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
              #  ℹ️ 클라이언트가 원래 보낸 Server의 도메인 값 확인.
              #  ㄴ> 필요성 : 클라이언트가 보낸 Host 헤더는 원래 요청의 대상 도메인 이름을 나타냅니다.
              proxy_set_header Host $http_host;
              # 요청이 들어온 포트를 백엔드에 전달
              # ㄴ> Spring 내 "server.forward-headers-strategy=framework" 설정이 필요함
              proxy_set_header X-Forwarded-Port $server_port;
          }
      }
  }
  ```

- ### Servers

  ```java
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
           * // 요청 받은 Access Ip is ::: 0:0:0:0:0:0:0:1
           * // 실제 접근하는 사용자 IP is ::: null
           * // xForwardedFor Ip is ::: null
           * // Host is ::: localhost:8081
           *
           * 👉 Nginx 사용 Log
           * This domain is ::: http://localhost
           * // Server Port is ::: 8081
           * // authorization is ::: null
           * // 요청 받은 Access Ip is ::: 192.168.64.2
           *    ㄴ> 👎 Nginx를 통해 들어왔기에 실제 사용자 IP가 아닌 서버의 내부 유동 IP가 나옴
           * // 실제 접근하는 사용자 IP is ::: 192.168.65.1
           * // xForwardedFor Ip is ::: 192.168.65.1
           * // Host is ::: localhost
           * */
          return result.toString();
      }
  }
  ```
