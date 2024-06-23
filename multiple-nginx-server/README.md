# Multiple Nginx Server

- `http -> server -> server_name`의 사용 이유를 찾다가 Nginx 설정만으로 서버를 나눌 수 있다는 것을 확인함
- ✅ 포인트는 `server_name` 설정이다.

### 주요 기능

- 블럭을 나눠 각각의 설정을 나눠서 여러개의 서버로 분리가 가능함

### 예제 코드

- #### Nginx

  - Docker-compose

    ```yaml
    # docker-compose.yml

    services:
    nginx:
      image: nginx:latest
      container_name: proxy
      # ⭐️ 접근하는 포트포워딩을 2개 설정
      ports:
        - "90:90"
        - "91:91"
      volumes:
        - ./nginx.conf:/etc/nginx/nginx.conf
    ```

  - nginx.conf

    ```yaml
    # /etc/nginx/nginx.conf

    # code ...

    http {
        # 서버 블록
        server {
            ## ✨ 서버명 설정
            server_name yoo1;
            # 이 서버가 요청을 수신하는 포트
            listen 90;           # IPv4 주소에서 포트 90 수신 대기
            listen [::]:90;      # IPv6 주소에서 포트 90 수신 대기

            # 위치 블록
            location / { /**code ..*/ }
        }

        server {
            ## ✨ 서버명 설정
            server_name yoo2;
            listen 91;           # IPv4 주소에서 포트 90 수신 대기
            listen [::]:91;      # IPv6 주소에서 포트 90 수신 대기

            # 위치 블록
            location / { /**code ..*/ }
        }
    }
    ```
