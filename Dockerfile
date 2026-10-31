FROM node:22-bookworm-slim AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/angular.json frontend/tsconfig.json frontend/tsconfig.app.json ./
COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/src/public ./src/public
RUN corepack enable && pnpm install --no-frozen-lockfile
RUN pnpm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist/careconnect /tmp/frontend-dist
RUN if [ -d /tmp/frontend-dist/browser ]; then cp -r /tmp/frontend-dist/browser/. /app/backend/src/main/resources/static/; else cp -r /tmp/frontend-dist/. /app/backend/src/main/resources/static/; fi
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/careconnect-backend-1.0.0.jar app.jar
ENV PORT=3000
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
