# Proxy - Server

- 간단하게 설명하면 Client에서 들어온 요청을 중간에서 다른 도메인으로 넘겨준 후 응답해주는 것이다.

## 1 ) 주요 기능

- 보안 강화
  - 접적인 서버 노출을 막아 보안을 강화할 수 있습니다.
  - 서버의 IP 주소와 구성 정보가 외부에 노출되지 않도록 보호할 수 있습니다.
- 캐싱 (Caching)
  - 자주 요청되는 콘텐츠를 캐싱하여, 동일한 요청이 들어올 때 백엔드 서버에 도달하지 않고 빠르게 응답할 수 있습니다.
  - 서버 성능 개선 및 응답 시간 단축 가능

## 2 ) 작동 방식

- 클라이언트와 서버 사이에서 중개자 역할을 합니다.
- 흐름
  - 1 . 클라이언트는 Proxy를 서버로 인식하고 요청을 보냄
  - 2 . Proxy는 해당 요청을 적절한 백엔드 서버로 전달하고, 서버로부터 응답을 받은 후 이를 클라이언트에게 반환
  - ex) `[Client] → [Proxy - Server] → [Backend Server]`

### 2 - 1 ) 예제 코드

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

## 3 ) SPA App 배포 

### 3 - 1 ) nginx.conf
```conf
server {
  listen 80;
  server_name localhost;

  root /usr/share/nginx/html;
  index index.html;

  # 🔁 MIME 타입 자동 설정을 위해 types 포함
  include       /etc/nginx/mime.types;
  default_type  application/octet-stream;

  # SPA 라우팅 fallback
  location / {
    try_files $uri $uri/ /index.html;
  }

  # 정적 자원 캐싱 및 MIME 대응
  location ~* \.(js|css|json|svg|png|jpg|jpeg|gif|woff2?)$ {
    expires 1y;
    access_log off;
    add_header Cache-Control "public";
    add_header Content-Type $content_type;  # ⛳ MIME 타입 자동 지정 (기본 보완)
  }
}

```


### 3 - 2 ) 주요 설정
- SPA 라우팅 fallback 설정
  - 주요 라인 : `try_files $uri $uri/ /index.html;`
  - 이유 :
    - SPA는 클라이언트 사이드에서 라우팅을 처리하기에 `/dashboard` 같은 경로가 있어도 실제 서버에 해당 경로에 대응하는 파일이나 디렉터리가 없음
    - 기본적으로 Nginx는 요청한 경로에 대응하는 파일이 없으면 404 에러를 반환
  - 방식 : 해당 설정 시 요청한 파일이나 폴더가 없으면 /index.html을 반환하는 설정함

- 정적 자원 캐싱 설정
  - 주요 라인 : `location ~* \.(js|css|json|svg|png|jpg|jpeg|gif|woff2?)$` -  expires 1y;
  - 사용 이유 : 정적 사원 캐싱 설정을 하여 로딩 속도 개선    




