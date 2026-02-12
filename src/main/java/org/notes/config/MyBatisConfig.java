package org.notes.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("org.notes.mapper")
@EnableTransactionManagement
public class MyBatisConfig {
}