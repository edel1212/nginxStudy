# Proxy - Server

- 간단하게 설명하면 Client에서 들어온 요청을 중간에서 다른 도메인으로 넘겨준 후 응답해주는 것이다.

### 주요 기능

- 보안 강화
  - 접적인 서버 노출을 막아 보안을 강화할 수 있습니다.
  - 서버의 IP 주소와 구성 정보가 외부에 노출되지 않도록 보호할 수 있습니다.
- 캐싱 (Caching)
  - 자주 요청되는 콘텐츠를 캐싱하여, 동일한 요청이 들어올 때 백엔드 서버에 도달하지 않고 빠르게 응답할 수 있습니다.
  - 서버 성능 개선 및 응답 시간 단축 가능

### 작동 방식

- 클라이언트와 서버 사이에서 중개자 역할을 합니다.
- 흐름
  - 1 . 클라이언트는 Proxy를 서버로 인식하고 요청을 보냄
  - 2 . Proxy는 해당 요청을 적절한 백엔드 서버로 전달하고, 서버로부터 응답을 받은 후 이를 클라이언트에게 반환
  - ex) `[Client] → [Proxy - Server] → [Backend Server]`

### 예제 코드

- `localhost:90` 호출 시 -> `localhost:8080`의 응답을 반환해 줌

- #### Nginx

  - Docker를 사용 하여 구동하였다.
  - 설정은 `nginx.conf` 내에서 사용한다.
  - Docker-compose

    ```yaml
    # docker-compose.yml

    services:
    nginx:
      image: nginx:latest
      container_name: proxy
      ports:
        - "90:90"
      volumes:
        - ./nginx.conf:/etc/nginx/nginx.conf
    ```

  - nginx.conf

    ```yaml
    # /etc/nginx/nginx.conf

    # 전역 컨텍스트
    user nginx;  # Nginx가 실행되는 사용자 >> 시스템의 다른 서비스와 리소스에 대한 무단 접근을 방지를 위함 [ ✅ 필수 적으로 지정해주자]
    worker_processes auto;  # 자동으로 워커 프로세스 수를 설정  >>  Nginx가 서버의 하드웨어에 맞게 최적화된 상태로 실행되도록 보장함
    pid /run/nginx.pid;  # PID 파일의 위치

    events {
        worker_connections 1024;  # 최대 연결 수 설정
    }

    # HTTP 컨텍스트
    http {
        # 서버 블록
        server {
            # 이 서버가 요청을 수신하는 포트
            listen 90;           # IPv4 주소에서 포트 90 수신 대기
            listen [::]:90;      # IPv6 주소에서 포트 90 수신 대기

            # 위치 블록
            location / {
                # proxy로 전달할 주소
                proxy_pass http://192.168.64.1:8080;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header Host $http_host;
            }
        }
    }

    ```

- #### Server

  - Controller
    ```java
    @RestController
    @RequestMapping("/api")
    public class FooController {
        @GetMapping
        public String helloWorld(){
            return "Hi";
        }
    }
    ```
