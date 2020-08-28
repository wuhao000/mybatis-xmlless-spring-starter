package com.aegis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试程序入口
 * @author 吴昊
 * @since 0.0.1
 */
@SpringBootApplication
@MapperScan("com.aegis")
public class MybatisTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(MybatisTestApplication.class, args);
  }
}
