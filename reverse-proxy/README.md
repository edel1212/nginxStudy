# ReverseProxy
- 클라이언트의 요청을 서버로 전달하고, 서버의 응답을 클라이언트로 전달하는 중계 역할을 하는 서버입니다.
- 일반적으로, Reverse Proxy는 서버 앞단에 위치하여 여러 가지 중요한 기능을 수행합니다.


### 주요 기능
  - 로드 밸런싱 (Load Balancing)
    -   여러 대의 서버로 들어오는 트래픽을 분배하여 각 서버의 부하를 고르게 분산시킵니다. 이를 통해 서버 과부하를 방지하고, 서비스의 가용성과 성능을 향상시킬 수 있습니다.
      - 기본적으로 Nginx의 Load Balancing 설정은 라운드 로빈이다.
    - 로드 밸런싱 알고리즘은 라운드 로빈(Round Robin), 최소 연결 수(Least Connections), 해시(Hash) 등을 사용할 수 있습니다.
  - 보안 강화
    -  접적인 서버 노출을 막아 보안을 강화할 수 있습니다.
      -  서버의 IP 주소와 구성 정보가 외부에 노출되지 않도록 보호할 수 있습니다.
  - 캐싱 (Caching)
    -  자주 요청되는 콘텐츠를 캐싱하여, 동일한 요청이 들어올 때 백엔드 서버에 도달하지 않고 빠르게 응답할 수 있습니다.
      - 서버 성능 개선 및 응답 시간 단축 가능

### 작동 방식
- 클라이언트와 서버 사이에서 중개자 역할을 합니다.
- 흐름
  - 1 . 클라이언트는 Reverse Proxy를 서버로 인식하고 요청을 보냄
  - 2 . Reverse Proxy는 해당 요청을 적절한 백엔드 서버로 전달하고, 서버로부터 응답을 받은 후 이를 클라이언트에게 반환
  - ex) `[Client] → [Reverse Proxy] → [Backend Server(s)]`

  
### 예제 코드

- #### Nginx
  - Docker를 사용 하여 구동하였다.
  - 설정은 `nginx.conf` 내에서 사용한다.
  - Docker-compose
    ```yaml
    # docker-compose.yml
    
    version: '3'
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
  - nginx.conf
    - 주의 사항은 Docker-compose를 사용해서 구동하였기에 연결하려는 upstream(서버들)의 주소는 **호스트 서버의 주소** 또는 **Docker-Network** 주소해야한다.
    ```yaml
    # /etc/nginx/nginx.conf
    
    # 전역 컨텍스트
    user nginx;  # Nginx가 실행되는 사용자
    worker_processes auto;  # 자동으로 워커 프로세스 수를 설정
    pid /run/nginx.pid;  # PID 파일의 위치
    
    events {
        worker_connections 1024;  # 최대 연결 수 설정
    }
    
    # HTTP 컨텍스트
    http {
        # ℹ️ 원한다면 아래에 로드밸런싱에 사용될 메서드를 지정 가능
        ## {Load Balancing Method} # 로드 밸런싱 메서드 지정 ex) ip_hash;
    
        # Upstream 블록
        upstream backend {
            ## ℹ️ localhost로 접근하면 127.0.0.1로 접근한다 하지만 Docker를 사용했기에 그러함
            server 192.168.1.45:8081;  # 첫 번째 백엔드 서버
            server 192.168.1.45:8082;  # 두 번째 백엔드 서버
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

- #### Servers(2개 적용)
  - 간단하게 요청 -> 해당 포트를 응답하는 서버를 구성
  - Controller
    ```java
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
    ```
