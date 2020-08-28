package com.aegis.mybatis

import com.aegis.MybatisTestApplication
import com.aegis.mybatis.xmlless.starter.MyBatisXmlLessAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import org.junit.Ignore
import org.junit.runner.RunWith
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner

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
@RunWith(SpringRunner::class)
@Import(
    MybatisTestApplication::class,
    MyBatisXmlLessAutoConfiguration::class,
    MybatisPlusAutoConfiguration::class)
@Ignore
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
open class BaseTest
