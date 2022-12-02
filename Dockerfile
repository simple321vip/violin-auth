FROM openjdk:11-jre-slim

MAINTAINER XiangWeiGuan

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN echo 'Asia/Shanghai' >/etc/timezone

ADD target/violin-auth-*.jar /violin-book.jar

# 设置暴露的端口号
EXPOSE 8080

ENTRYPOINT ["java","-jar","violin-book.jar"]
