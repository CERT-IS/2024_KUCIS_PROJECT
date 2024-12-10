FROM openjdk:17-slim AS siem
ENV TZ=Asia/Seoul
ARG JAR_FILE
COPY ${JAR_FILE} /app/siem-app.jar

FROM custiya/certis_ai:v1 AS slm

FROM ubuntu:24.04
ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
        tzdata \
        python3-pip \
        python3-venv \
        openjdk-17-jre && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=slm /ai/requirements.txt /app/ai/requirements.txt
RUN python3 -m venv /app/venv && \
    /app/venv/bin/pip install --no-cache-dir -r /app/ai/requirements.txt

ENV PATH="/app/venv/bin:$PATH"

COPY --from=siem /app/siem-app.jar /app/siem-app.jar
COPY --from=slm /ai /app/ai

EXPOSE 5000 8080

LABEL authors="ns"

COPY docker-endpoint.sh /usr/local/bin/docker-endpoint.sh
RUN chmod +x /usr/local/bin/docker-endpoint.sh

CMD ["sh", "/usr/local/bin/docker-endpoint.sh"]
