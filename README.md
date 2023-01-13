## プログラム紹介
violin-auth, a backend program to support for violin system auth.

### kubernetes
we use kubernetes to deployment 
> the violin-auth is built on kubernetes.
> service, storageclass, pvc, deployment

### framework
> springboot, jedis and so on. for more, we can see pom.xml
 
### about security

> baidu cloud oauth -> token -> redis + token
> redis as a kubernetes service opened only for violin-auth application that to save token as a cache one day. 

### server

> the backend server tomcat embedded in the springboot used.