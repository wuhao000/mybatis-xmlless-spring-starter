package com.aegis.mybatis.xmlless.config

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 *
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Configuration
@EnableConfigurationProperties(AegisMyBatisConfig::class)
@ConfigurationProperties(prefix = "mybatis.aegis")
class AegisMyBatisConfig {

  @Bean
  @ConditionalOnMissingBean
  fun paginationInterceptor(): PaginationInterceptor {
    return PaginationInterceptor()
  }

//  @Bean
//  fun springDataPaginationInterceptor(): SpringDataPaginationInterceptor {
//    return SpringDataPaginationInterceptor()
//  }

}
