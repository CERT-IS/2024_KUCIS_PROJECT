FROM openjdk:17-slim

ENV TZ=Asia/Seoul
EXPOSE 8080

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
LABEL authors="ns"

ENTRYPOINT ["java", "-jar", "/app.jar"]

## 베이스 이미지 (Spring 애플리케이션)
#FROM openjdk:17-slim AS spring
#
#ENV TZ=Asia/Seoul
#EXPOSE 8080
#
#ARG JAR_FILE
#COPY ${JAR_FILE} app.jar
#LABEL authors="ns"

## Flask 애플리케이션을 위한 이미지
#FROM python:3.9 AS flask
#
## Flask 애플리케이션 복사 및 설치
#WORKDIR /app
#COPY ./requirements.txt /app/requirements.txt
#RUN pip install -r requirements.txt
#COPY ./app.py /app/app.py  # app.py 파일 복사
#
## 최종 이미지
#FROM ubuntu:20.04
#
## Spring 애플리케이션과 Flask 애플리케이션 복사
#COPY --from=spring /app.jar /app/spring-app.jar
#COPY --from=flask /app /app
#
## 필요한 패키지 설치
#RUN apt-get update && apt-get install -y \
#    python3 \
#    python3-pip \
#    && rm -rf /var/lib/apt/lists/*
#
## Flask 애플리케이션 실행을 위한 포트 설정
#EXPOSE 5000
## Spring 애플리케이션 실행을 위한 포트 설정
#EXPOSE 8080
#
## Flask와 Spring 애플리케이션 동시에 실행
#CMD ["sh", "-c", "java -jar /app/spring-app.jar & python3 /app/app.py"]

