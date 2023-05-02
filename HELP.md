# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.6/maven-plugin/reference/html/#build-image)

---

1. Maven 设置utf8编码格式 方法
   [Maven 设置utf8编码格式 方法](https://blog.csdn.net/laow1314/article/details/108759701)
   [Maven 设置utf8编码格式 方法](https://codeleading.com/article/14374739596/)
2. 打包方法
   经验证，原始的`mvn clean package`方式依然可以打包，但是生产的jar只包含当前源码的类，不包含`Spring-boot`中的相关依赖。
   [Packaging Executable Archives](https://docs.spring.io/spring-boot/docs/2.6.6/maven-plugin/reference/htmlsingle/#packaging)
   ``` xml
   <build>
      <plugins>
         <plugin>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-maven-plugin</artifactId>
               <executions>
                  <execution>
                     <goals>
                           <goal>repackage</goal>
                     </goals>
                  </execution>
               </executions>
         </plugin>
      </plugins>
   </build>
   ```
   即便使用了上面的配置，生产的jar也是不包含`Spring-boot`依赖的。

   修正方法：[spring-boot:repackage](https://docs.spring.io/spring-boot/docs/2.6.6/maven-plugin/reference/htmlsingle/#goals-repackage)
   ``` shell
   mvn clean package spring-boot:repackage
   ```
   注意：必须包含`package`。
   尽管描述没什么区别，但是生成的jar是包含`Spring-boot`的。
   另外，这种方式，也不需要在添加`spring-boot-maven-plugin`插件。或者说，不配置任何`<build>`都可以。
   如果没有包含`package`，例如`mvn clean `，则会提示类似错误：
   ``` log
   Execution default-cli of goal org.springframework.boot:spring-boot-maven-plugin:2.5.12:repackage failed: Source file must not be null
   ```
3. 执行方式
   ``` shell
   java -jar lenovo-driver-downloader-1.0-SNAPSHOT.jar --spring.config.location=application.yml
   ```
   注意：使用`--`来指定spring的属性参数。
   [Running from the Command Line](https://docs.spring.io/spring-boot/docs/2.6.6/reference/htmlsingle/#howto.batch.running-from-the-command-line)
   > Spring Boot converts any command line argument starting with `--` to a property to add to the `Environment`, see [accessing command line properties](https://docs.spring.io/spring-boot/docs/2.6.6/reference/htmlsingle/#features.external-config.command-line-args). This should not be used to pass arguments to batch jobs. To specify batch arguments on the command line, use the regular format (that is without `--`), as shown in the following example:
   > ``` shell
   > $ java -jar myapp.jar someParameter=someValue anotherParameter=anotherValue
   > ```
   > If you specify a property of the Environment on the command line, it is ignored by the job. Consider the following command:
   > ``` shell
   > $ java -jar myapp.jar --server.port=7070 someParameter=someValue
   > ```
   > This provides only one argument to the batch job: `someParameter=someValue`.

   [7.2.1. Accessing Command Line Properties](https://docs.spring.io/spring-boot/docs/2.6.6/reference/htmlsingle/#features.external-config.command-line-args)
   > By default, `SpringApplication` converts any command line option arguments (that is, arguments starting with `--`, such as `--server.port=9000`) to a property and adds them to the Spring Environment. As mentioned previously, command line properties always take precedence over file-based property sources.
   > If you do not want command line properties to be added to the `Environment`, you can disable them by using `SpringApplication.setAddCommandLineProperties(false)`.

