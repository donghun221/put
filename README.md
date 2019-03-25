# 简单测试Linux 到COS的 PUT 时间

## 怎么跑？
- git clone
- mvn clean
- mvn package
- java -jar java -jar put-jar-with-dependencies.jar 循环次数 SecretId SecretKey Region Bucket

例子：
java -jar put-jar-with-dependencies.jar 10 XXXX XXXX ap-shanghai dongxuny-sh-125600000

