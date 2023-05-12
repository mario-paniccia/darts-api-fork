FROM openjdk:17-jdk-slim-bullseye AS build-env
WORKDIR /usr/local/bin
# Linux Static Builds (http://www.ffmpeg.org/download.html#build-linux)
# https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz
ADD lib/ffmpeg-release-amd64-static.tar.xz /usr/local/bin
RUN cp -p ffmpeg*/ffmpeg /usr/bin

 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.11
FROM hmctspublic.azurecr.io/base/java:17-distroless
COPY --from=build-env /usr/bin/ffmpeg /usr/bin
COPY build/libs/darts-api.jar /opt/app/

EXPOSE 4550
CMD [ "darts-api.jar" ]
