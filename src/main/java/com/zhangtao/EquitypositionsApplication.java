package com.zhangtao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zhangtao.mapper")
@SpringBootApplication
public class EquitypositionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquitypositionsApplication.class, args);
    }

}
