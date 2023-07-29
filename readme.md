Watcher for workers in a distribuited system and create new in case some worker crashes

## To run the auto-healer, which in turn would launch and maintain 10 workers

use java 11

example: java -jar target/auto-healer-1.0-jar-with-dependencies.jar <number-of-instances> "/home/rafael/Workspace/throughput/target/throughput-jar-with-dependencies.jar"

It will start N instances listenting to the port 8081, 8082, 8083... for the service http://localhost:8081/api/converter/dec-to-hex?value=value

If we kill an instance by port like sudo kill -9 $(lsof -t -i:8082) zookeeper will start a new one listening to the last port + 1. 

