[TOC]

**package问题**

---

# 问题现象

当执行`mvn package`的时候，出现了如下问题：

    ``` log
    [WARNING] Error injecting: org.springframework.boot.maven.RepackageMojo
    java.lang.TypeNotPresentException: Type org.springframework.boot.maven.RepackageMojo not present
    
    Caused by: java.lang.UnsupportedClassVersionError: org/springframework/boot/maven/RepackageMojo has been compiled by a more recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only recognizes class file versions up to 52.0
    
    [ERROR] Failed to execute goal org.springframework.boot:spring-boot-maven-plugin:3.0.1:repackage (default) on project lenovo-driver-downloader: Execution default of goal org.springframework.boot:spring-boot-maven-plugin:3.0.1:repackage failed: Unable to load the mojo 'repackage' in the plugin 'org.springframework.boot:spring-boot-maven-plugin:3.0.1' due to an API incompatibility: org.codehaus.plexus.component.repository.exception.ComponentLookupException: org/springframework/boot/maven/RepackageMojo has been compiled by a more recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only recognizes class file versions up to 52.0
    ```

# 问题分析

从问题日志上来看，是插件的版本不正确导致的。
所以，处理方案可以是直接给插件添加`version`信息即可。

1. 方案1：直接添加`version`信息
   添加`<version>`。
   注意和`spring-boot-dependencies`的版本保持一致。
    ```xml
        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>2.5.12</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>repackage</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
    ```
2. 添加依赖配置
   添加`<pluginManagement>`。
   注意和`spring-boot-dependencies`的版本保持一致。
   ```xml
            <build>
                <pluginManagement>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                            <version>2.5.12</version>
                        </plugin>
                    </plugins>
                </pluginManagement>
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
    ```
3. 通过直接继承父项目
   父项目可选：`spring-boot-dependencies`或`spring-boot-starter-parent`。
   查看`spring-boot-dependencies`的pom文件可知，在其文件中已经定义了所有相关plugin的版本信息。
   *注意：直接通过[Using Spring Boot without the Parent POM](`https://docs.spring.io/spring-boot/docs/2.5.12/maven-plugin/reference/htmlsingle/ #using.import`)
   的方式是行不通的。可能的原因是，这种方式只是引入了依赖，而不是插件。*
    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <!-- Import dependency management from Spring Boot -->
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.12</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```
   所以，这里通过指定父项目的方式，也可以通过pom继承实现版本自动配置。
    1. `spring-boot-dependencies`
        ```xml
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.5.12</version>
        </parent>
        ```
    2. `spring-boot-starter-parent`

       ```xml
       <!-- Inherit defaults from Spring Boot -->
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>2.5.12</version>
       </parent>
       ```
       注：`spring-boot-starter-parent`的父项目是`spring-boot-dependencies`，并在其基础上添加了其它配置。

   注：由于`spring-boot-dependencies`和`spring-boot-starter-parent`都配置了`maven-compiler-plugin`
   ，前者配置了版本，后者又配置了参数。所以，如果使用这两个pom作为parent，则可以在`<build>`中省略`maven-compiler-plugin`
   的配置。除非有其它需求，例如除去对默认配置文件的打包等。
