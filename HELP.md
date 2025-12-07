# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.0/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.0/maven-plugin/build-image.html)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.


---

### JDK版本
JDK: 17

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
   > Spring Boot converts any command line argument starting with `--` to a property to add to the `Environment`,
   see [accessing command line properties](https://docs.spring.io/spring-boot/docs/2.6.6/reference/htmlsingle/#features.external-config.command-line-args).
   This should not be used to pass arguments to batch jobs. To specify batch arguments on the command line, use the
   regular format (that is without `--`), as shown in the following example:
   > ``` shell
   > $ java -jar myapp.jar someParameter=someValue anotherParameter=anotherValue
   > ```
   > If you specify a property of the Environment on the command line, it is ignored by the job. Consider the following
   command:
   > ``` shell
   > $ java -jar myapp.jar --server.port=7070 someParameter=someValue
   > ```
   > This provides only one argument to the batch job: `someParameter=someValue`.

   [7.2.1. Accessing Command Line Properties](https://docs.spring.io/spring-boot/docs/2.6.6/reference/htmlsingle/#features.external-config.command-line-args)
   > By default, `SpringApplication` converts any command line option arguments (that is, arguments starting with `--`,
   such as `--server.port=9000`) to a property and adds them to the Spring Environment. As mentioned previously, command
   line properties always take precedence over file-based property sources.
   > If you do not want command line properties to be added to the `Environment`, you can disable them by
   using `SpringApplication.setAddCommandLineProperties(false)`.
4. Windows的Terminal下中文乱码
   在Windows下，执行`mvn clean spring-boot:run`或`java -jar xxx.jar`的时候，遇到中文会出现乱码。
   可以使用`chcp`指令修正。
   ```shell
   PS C:\Users\admin> chcp
   活动代码页: 936
   PS C:\Users\admin> chcp 65001
   Active code page: 65001
   PS C:\Users\admin> chcp 936
   活动代码页: 936
   ```
   默认是`936`，在Windows下的终端或者命令提示符下运行之前，现改为65001。
   [Windows 修改控制台编码为 UTF-8_](https://mxy.cool/2021052715441/)
   [chcp](https://learn.microsoft.com/zh-cn/windows-server/administration/windows-commands/chcp)
   [CHCP](https://ss64.com/nt/chcp.html)



### 

问题：
``` shell
mvn clean package
...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project lenovo-driver-downloader: Fatal error compiling: 无效的目标发行版: 17 -> [Help 1]
```
解决方案：
手动配置`JAVA_HOME`，注意，不要直接执行`JAVA_HOME=xxx`，需要添加`export`才可以。
实例：
```shell
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.16.8-2.oe2509.x86_64
```
原因和mvn绑定的JAVA版本有关。
执行上述命令之前：
```shell
[hw@192 ~]$ mvn -v
Apache Maven 3.6.3 (openEuler 3.6.3-2)
Maven home: /usr/share/maven
Java version: 1.8.0_462, vendor: BiSheng, runtime: /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.462.b08-3.oe2509.x86_64/jre
Default locale: zh_CN, platform encoding: UTF-8
OS name: "linux", version: "6.6.0-102.0.0.8.oe2509.x86_64", arch: "amd64", family: "unix"
```
执行上述命令之后：
```shell
[hw@192 ~]$ mvn -v
Apache Maven 3.6.3 (openEuler 3.6.3-2)
Maven home: /usr/share/maven
Java version: 17.0.16, vendor: BiSheng, runtime: /usr/lib/jvm/java-17-openjdk-17.0.16.8-2.oe2509.x86_64
Default locale: zh_CN, platform encoding: UTF-8
OS name: "linux", version: "6.6.0-102.0.0.8.oe2509.x86_64", arch: "amd64", family: "unix"
```

### 问题

```shell
mvn clean package
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.682 s
[INFO] Finished at: 2025-12-07T23:00:21+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.4:test (default-test) on project lenovo-driver-downloader: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:3.5.4:test failed: Unable to load the mojo 'test' in the plugin 'org.apache.maven.plugins:maven-surefire-plugin:3.5.4'. A required class is missing: org.apache.maven.plugin.surefire.SurefireMojo
[ERROR] -----------------------------------------------------
[ERROR] realm =    plugin>org.apache.maven.plugins:maven-surefire-plugin:3.5.4
[ERROR] strategy = org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy
[ERROR] urls[0] = file:/C:/Users/czx/.m2/repository/org/apache/maven/plugins/maven-surefire-plugin/3.5.4/maven-surefire-plugin-3.5.4.jar
[ERROR] urls[1] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-api/3.5.4/surefire-api-3.5.4.jar
[ERROR] urls[2] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-logger-api/3.5.4/surefire-logger-api-3.5.4.jar
[ERROR] urls[3] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-shared-utils/3.5.4/surefire-shared-utils-3.5.4.jar
[ERROR] urls[4] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-extensions-api/3.5.4/surefire-extensions-api-3.5.4.jar
[ERROR] urls[5] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/maven-surefire-common/3.5.4/maven-surefire-common-3.5.4.jar
[ERROR] urls[6] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-booter/3.5.4/surefire-booter-3.5.4.jar
[ERROR] urls[7] = file:/C:/Users/czx/.m2/repository/org/apache/maven/surefire/surefire-extensions-spi/3.5.4/surefire-extensions-spi-3.5.4.jar
[ERROR] urls[8] = file:/C:/Users/czx/.m2/repository/org/apache/maven/shared/maven-common-artifact-filters/3.4.0/maven-common-artifact-filters-3.4.0.jar
[ERROR] urls[9] = file:/C:/Users/czx/.m2/repository/org/codehaus/plexus/plexus-java/1.5.0/plexus-java-1.5.0.jar
[ERROR] urls[10] = file:/C:/Users/czx/.m2/repository/org/ow2/asm/asm/9.8/asm-9.8.jar
[ERROR] urls[11] = file:/C:/Users/czx/.m2/repository/com/thoughtworks/qdox/qdox/2.2.0/qdox-2.2.0.jar
[ERROR] Number of foreign imports: 1
[ERROR] import: Entry[import  from realm ClassRealm[maven.api, parent: null]]
[ERROR]
[ERROR] -----------------------------------------------------
[ERROR]
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginContainerException
```
解决方法：
升级到`4.0.0`之后，用到的`maven-surefire-plugin`的版本是`3.5.4`:
```xml
    <maven-surefire-plugin.version>3.5.4</maven-surefire-plugin.version>
```
SpringBoot中导致此文的提交是：[Commit fb078a7](https://github.com/spring-projects/spring-boot/commit/fb078a7b6e623a638acfe6dfd87882b4c6532617#diff-628b1be6256995e7b2c8ca015447f7e3a3569350274a5accc528a37330ecbdb6R1498)
临时方案，使用较低版本替换：`3.5.3`
```xml
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <!-- 切换到稳定版本 3.5.8 -->
                    <version>3.5.3</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
```
该问题，已经提交相关issue进行咨询：[48409](https://github.com/spring-projects/spring-boot/issues/48409)
