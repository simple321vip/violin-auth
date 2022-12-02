FROM openjdk:11-jre-slim

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN echo 'Asia/Shanghai' >/etc/timezone

ADD target/violin-auth-*.jar /violin-auth.jar

# 设置暴露的端口号
EXPOSE 8080

ENTRYPOINT ["java","-jar","violin-auth.jar"]
