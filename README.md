
NOTE: This is a very prelimary benchmark. Not everything my still work. 

Code is UGLY, still in flux. 

- There are 3 end points: 
  - Java: Which use Jetty 9.x and the raw request handler way. 
  - NodeJS: Using express
  - Netty: A first attempt to use the netty / nio way. Reactive way is not implemented yet. 

For the load testing we are using Apache Benchmark docker image https://github.com/jig/docker-ab

Requirement: 
- nodejs > 10.x
- Java > 8.x

git clone, 

## node.js server 

- install `npm install`
- start server `npm start` (it uses ts-node)
- load test `docker run --rm jordi/ab -k -c 100 -n 10000 http://docker.for.mac.localhost:8080/data` 


## Java - Jetty 

**install**

```
cd servlet/
curl -o jetty-all-uber.jar https://repo1.maven.org/maven2/org/eclipse/jetty/aggregate/jetty-all/9.4.11.v20180605/jetty-all-9.4.11.v20180605-uber.jar
```

**compile**

```
java -cp classes:jetty-all-uber.jar com.britesnow.javabench.HelloWorld
```

**Start server and load test**

(make sure to stop any previous server on :8080)

```
javac -d classes -cp jetty-all-uber.jar src/*.java

docker run --rm jordi/ab -k -c 100 -n 10000 http://docker.for.mac.localhost:8080/data
```


#### notes: 

- http://www.eclipse.org/jetty/documentation/current/advanced-embedding.html


## Netty setup

NOTE: NEEDS TO BE UPDATED. Just keeping notes for record. 

Note: the jetty server does not have the helloworld or even any type of "routing". Just read the ?it and do the loop for any request (this is what we need for now in the benchmark)

Server code from: http://www.seepingmatter.com/2016/03/30/a-simple-standalone-http-server-with-netty.html

```sh
# Setup netty
wget http://dl.bintray.com/netty/downloads/netty-4.1.25.Final.tar.bz2
bzip2 -d netty-4.1.25.Final.tar.bz2
tar -xvf netty-4.1.25.Final.tar


javac -Werror -cp netty-4.1.25.Final/jar/all-in-one/netty-all-4.1.25.Final.jar:. HelloWorldNetty.java 
java -cp netty-4.1.25.Final/jar/all-in-one/netty-all-4.1.25.Final.jar:. HelloWorldNetty

```

## Other resources: 

- JS vs Java Benchmark Game: https://benchmarksgame-team.pages.debian.net/benchmarksgame/faster/javascript.html