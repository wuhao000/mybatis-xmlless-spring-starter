package com.aegis.mybatis

import com.aegis.MybatisTestApplication
import com.aegis.mybatis.xmlless.starter.MyBatisXmlLessAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

/**
 *
 * Created by 吴昊 on 2018/12/14.
 *
 * @author 吴昊
 * @since 0.0.7
 */
@MybatisTest(
    excludeAutoConfiguration = [CacheAutoConfiguration::class]
)
@Import(
    MybatisTestApplication::class,
    MyBatisXmlLessAutoConfiguration::class,
    MybatisPlusAutoConfiguration::class)
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
open class BaseTest
