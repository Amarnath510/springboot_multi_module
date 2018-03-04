# Spring Boot Multi Module using Gradle

# Requirements
- Gradle
- IntelliJ

# Setup
- IntelliJ --> Create New Project --> Gradle + Java
- GroupId: com; ArtifactId: springboot-multi-module
- Check "use auto-imports"
- Finish

# Porject Structure
- Basic project structure,

![screen shot 2018-03-04 at 8 51 41 pm](https://user-images.githubusercontent.com/4599623/36947121-e26d7dea-1fed-11e8-9304-b029bb075687.png)

- If the root has any src/ folder then delete it. No problem.

# Create Modules
- We will create two modules:
    1. common (is a library which can be used by all application modules)
    2. search (Application module which accepts requests. Search will include common module.)

# Module Creation
- Right click on root project --> New --> Module
- Select Gradle + Java
- Give ArtifactId as "common". 
- Do next ... Finish
- Now you can see in `root/settings.gradle` we have **include 'common'**.
- Similarly we will create search module.
- Final root/settings.gradle will be as follows,
```java
rootProject.name = 'springboot-multi-module'
include 'common'
include 'search'
```

# Setup common module
- Our common module is just a library. This module will be shared among all the modules. We don't include any web related dependencies here. We just need to say that this is a spring boot application.
- `common/build.gradle`
```java
buildscript {
    repositories { mavenCentral() }
}

plugins { id "io.spring.dependency-management" version "1.0.4.RELEASE" }

ext { springBootVersion = '2.0.0.RELEASE' }

apply plugin: 'java'
apply plugin: 'idea'

jar {
    baseName = 'springboot-multi-module-library'
    version = '0.0.1-SNAPSHOT'
}

sourceCompatibility = 1.8

repositories { mavenCentral() }

dependencies {
    compile('org.springframework.boot:spring-boot-starter')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}

dependencyManagement {
    imports { mavenBom("org.springframework.boot:spring-boot-dependencies:${springBootVersion}") }
}

```
- Let's create source directory. Right click on common module --> New --> Directory --> src/main/java
- Right click on common/src/main/java --> New --> Package -->  com.common
- Create class `Product`(id, name, price) under `com.common`.
- That's it. common module is ready to use.
- Do, `./gradlew build` to check the build status.

# Setup search application module
- Search module is an application which serves API request and returns response. Hence we need to include web dependency for this module. **Web dependency will have tomcat as a transitive dependency. No need to explicitly include tomcat.**
- Also search module requires common module. Include it under dependecy as `compile project(':common')`
- `search/build.gradle`
```java
buildscript {
    ext { springBootVersion = '2.0.0.RELEASE' }
    repositories { mavenCentral() }
    dependencies { classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}") }
}

apply plugin: 'java'
apply plugin: 'idea'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

jar {
    baseName = 'springboot-multi-module-search'
    version = '0.0.1-SNAPSHOT'
}
sourceCompatibility = 1.8

repositories { mavenCentral() }

dependencies {
    compile('org.springframework.boot:spring-boot-starter-actuator')
    compile('org.springframework.boot:spring-boot-starter-web')
    compile project(':common')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
```
- Create src/main/java/ directory under search. Create package com.search.
- Create class `SearchApplication` with following code,
```java
package com.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.Product;

@RestController
@SpringBootApplication(scanBasePackages = "com")
@RequestMapping("/search")
public class SearchApplication {

    @RequestMapping("/hello")
    @GetMapping
    public String hello() {
        return "Search: Hello";
    }

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
```
**NOTE:** In the above code `main` method is important. This is where Spring Boot will start this module. `scanBasePackages` is another important annotation, as it says to scan all the components under the package `com`. So ideally all our modules packages are under `com` structure. If you have the code in a different package then include it as  `scanBasePackages = {"com", "another"}`

- We will explicitly mention Tomcat server port(as 8100) else it will by default run in `8080`.
- Create `resources` directory src/main/ and add `application.properties` file. Give `server.port=8100` as property.

# Run (let's see whether we are able to access search API or not)
- Under root (springboot-multi-module) --> run, `./gradlew build` to build and then run `./gradlew :search:bootRun` to run search application.
    1. First `./gradlew build` will make common library to be compiled and ready to use.
    2. Second, `./gradlew :search:bootRun` will make search application to run on port 8100
- Open Postman/Chrome, http://localhost:8100/search/hello

# Create custom class(Product) and use it
- Create another api under search. For now lets call it, "/product"
- Make sure we say this api produces a JSON.
```java
@RequestMapping(value = "/product",
        method = RequestMethod.GET,
        produces = {"application/json"})
public Product product() {
    return new Product(1, "Laptop", 45000d);
}
```
- Build and Run, `./gradlew build && ./gradlew :search:bootRun`
- http://localhost:8100/search/product
```java
{
    id: 1,
    name: "Laptop",
    price: 45000
}
```

# Get particular Product by passing its id using @PathVariable
- NOTE: 
    @PathVariable is to get the value from URL. 
    @RequestBody is used when we pass input as JSON/XML etc. (You will see this usage later)
- Created `com.common.ProductService` for products creation.
    1. To search by id of the product where id is passed as a url param.
    2. To return a list of products as JSON.
- Add few more apis in search,
```java
@RequestMapping(value = "/product/{id}",
        method = RequestMethod.GET,
        produces = {"application/json"})
public Product productWith(@PathVariable("id") long id) {
    Optional<Product> product = ProductService.getProducts().stream().filter(p -> p.getId() == id).findFirst();
    return product.get();
}

@RequestMapping(value = "/products",
        method = RequestMethod.GET,
        produces = {"application/json"})
public List<Product> products() {
    return ProductService.getProducts();
}
```

# Run 
- Build & Run in together, **./gradlew build && ./gradlew :search:bootRun**
- http://localhost:8100/search/product/1 or http://localhost:8100/search/products

# Lets create a POST call to insert a Product
- We can pass a Product as a JSON to api, consume it as JSON and convert it to Product using `@RequestBody` annotation.
- In `ProductService` add a method to `createProduct` and add it to existing products list.
- POST method as follows,
```java
@RequestMapping(value = "/product/create",
                method = RequestMethod.POST,
                consumes = {"application/json"}
                )
public String createProduct(@RequestBody Product product) {
    if (ProductService.createProduct(product)) {
        return "Created Product = " + product.toString();
    }
    return "Creation failed for Product = " + product.toString();
}
```
- Open Postman, 
    1. Select POST
    2. http://localhost:8100/search/product/create
    3. Body --> raw and select type as JSON.
    4. Enter the below input in the body placeholder,
```java
{
	"name": "Product-10",
	"price": 10
}
```
- Run it and access the above url with JSON as params.

# Conclusion
- We have successfully created a Gradle Multi Module Spring Boot application using IntelliJ.
