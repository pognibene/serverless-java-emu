# The Serverless API Java emulator
This project emulates AWS API Gateway and AWS lambda, for Java based Lambdas.
It enables fast and convenient testing of your APIs, both locally on your computer and also remotely after deployment.

# Installation
The Serverless API Java emulator (aka SAJ) is available on the maven central repository, so using it is 
a matter of including 1 dependency in your tests:
```xml
<dependency>
    <groupId>com.agileandmore</groupId>
    <artifactId>com.agileandmore.slsemulator</artifactId>
    <version>1.1</version>
    <scope>test</scope>
</dependency>
``` 
and one dependency in your production code:
```xml
<dependency>
    <groupId>com.agileandmore</groupId>
    <artifactId>com.agileandmore.slsruntime</artifactId>
    <version>1.0</version>
</dependency>
``` 
If you would like to compile SAJ yourself:
```shell script
cd sls-runtime
mvn clean install
cd ../sls-emulator
mvn clean install
```
However, be aware that this will result in snapshot versions, so you'll have to adapt the pom files.
# Usage
As SAJ is meant to be used as part of a testing strategy, the best place to get started is the
[WIKI](https://github.com/pognibene/serverless-java-emu/wiki) and then follow with the [Tutorial](https://github.com/pognibene/serverless-java-emu/wiki/Tutorial).
# Contributing
If you would like to contribute to this project, the best way currently is to post change requests, suggestions or bugs
through [github](https://github.com/pognibene/serverless-java-emu/issues).
# License
The `com.agileandmore.slsruntime` module is covered by the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0), as it's going to be embedded
in your production code and requires a permissive license.

The `com.agileandmore.slsemulator` module is covered by the [GNU GPL V3 license](https://www.gnu.org/licenses/gpl-3.0.en.html), as it's used only in the scope
of your tests and is normally not shipped in production.
