# Nginx

### Nginx란?
```properties
ℹ️ 초기에는 웹 서버로 주로 사용되었지만, 현재는 리버스 프록시 서버 및 HTTP 캐시로서도 널리 사용되고 있다.
   - Nginx는 많은 양의 동시 접속을 처리할 수 있다
   - CPU 및 메모리 사용이 적어 서버 리소스를 효율적으로 관리할 수 있습니다
   - 활용 예시
     - 정적파일을 serving 하는 web server
     - 요청을 다른 서버로 전달하는 reverse proxy server 로 활용되어 was 의 부하를 줄이는 로드 밸런서로 사용할 수도 있다.    
```

### Apache 웹 서버 차이

측면              | Apache                                  | Nginx
------------------|-----------------------------------------|-----------------------------------------
처리 모델         | 멀티 프로세스, 멀티 스레드 방식         | 이벤트 기반, 단일 또는 제한된 수의 프로세스 사용
요청 처리         | 각 요청마다 새로운 스레드 또는 프로세스 생성 | 동시에 요청을 처리하여 효율적인 동시성 관리
동시성             | 동시 접속에서 자원 소모가 큼         | 효율적인 동시 처리로 자원 절약
자원 사용         | 높은 부하에서 CPU와 메모리 사용이 많음  | 효율적인 동시성으로 CPU와 메모리 절약
적합성             | 전통적인 웹 애플리케이션에 적합         | 고트래픽 및 동시 접속이 많은 애플리케이션에 중간 로드 밸렁싱에 사용
