FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ARG APP_PORT=6000

RUN useradd --create-home --shell /usr/sbin/nologin appuser

COPY target/classes/ /app/classes/
COPY target/dependency/ /app/dependency/

ENV PORT=${APP_PORT} \
    THREAD_POOL_SIZE=4

EXPOSE ${APP_PORT}

USER appuser

CMD ["java", "-cp", "/app/classes:/app/dependency/*", "edu.eci.arem.app.FrameworkApplication"]
