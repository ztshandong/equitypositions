package com.zhangtao.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/*
 * https://localhost:8080/swagger-ui.html
 * */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zhangtao.controller"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("张涛->title")
                        .description("张涛->description")
                        .version("1.0")
                        .contact(new Contact("张涛", "https://ztshandong.github.io/", "17091648421@126.com"))
                        .license("The Apache License")
                        .licenseUrl("https://gitee.com/zhuorui/zhangtao")
                        .build());
    }
}
