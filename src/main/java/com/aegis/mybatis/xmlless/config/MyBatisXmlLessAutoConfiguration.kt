package com.aegis.mybatis.xmlless.config

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator
import com.baomidou.mybatisplus.core.injector.ISqlInjector
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import org.apache.ibatis.mapping.DatabaseIdProvider
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.util.CollectionUtils
import org.springframework.util.ObjectUtils
import org.springframework.util.StringUtils
import javax.sql.DataSource

/**
 *
 * Created by 吴昊 on 2018/12/13.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@ComponentScan("com.aegis.mybatis.xmlless")
@Configuration
@ConditionalOnClass(SqlSessionFactory::class, SqlSessionFactoryBean::class)
@EnableConfigurationProperties(MybatisPlusProperties::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
@AutoConfigureBefore(MybatisPlusAutoConfiguration::class)
class MyBatisXmlLessAutoConfiguration(private var properties: MybatisPlusProperties,
                                      interceptorsProvider: ObjectProvider<Array<Interceptor>>,
                                      private var resourceLoader: ResourceLoader,
                                      databaseIdProvider: ObjectProvider<DatabaseIdProvider>,
                                      configurationCustomizersProvider: ObjectProvider<List<ConfigurationCustomizer>>,
                                      private var applicationContext: ApplicationContext) {

  private var configurationCustomizers: List<ConfigurationCustomizer>? = configurationCustomizersProvider.ifAvailable
  private var databaseIdProvider: DatabaseIdProvider? = databaseIdProvider.ifAvailable
  private var interceptors: Array<Interceptor>? = interceptorsProvider.ifAvailable
//  private val logger = LoggerFactory.getLogger(MybatisPlusAutoConfiguration::class.java)

  @Bean
  @Throws(Exception::class)
  @ConditionalOnMissingBean
  fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory? {
    val factory = MybatisSqlSessionFactoryBean()
    factory.setDataSource(dataSource)
    factory.vfs = SpringBootVFS::class.java
    if (StringUtils.hasText(this.properties.configLocation)) {
      factory.setConfigLocation(this.resourceLoader.getResource(this.properties.configLocation))
    }
    applyConfiguration(factory)
    if (this.properties.configurationProperties != null) {
      factory.setConfigurationProperties(this.properties.configurationProperties)
    }
    if (!ObjectUtils.isEmpty(this.interceptors)) {
      factory.setPlugins(this.interceptors)
    }
    if (this.databaseIdProvider != null) {
      factory.databaseIdProvider = this.databaseIdProvider
    }
    if (StringUtils.hasLength(this.properties.typeAliasesPackage)) {
      factory.setTypeAliasesPackage(this.properties.typeAliasesPackage)
    }
    // TODO 自定义枚举包
    if (StringUtils.hasLength(this.properties.typeEnumsPackage)) {
      factory.setTypeEnumsPackage(this.properties.typeEnumsPackage)
    }
    if (this.properties.typeAliasesSuperType != null) {
      factory.setTypeAliasesSuperType(this.properties.typeAliasesSuperType)
    }
    if (StringUtils.hasLength(this.properties.typeHandlersPackage)) {
      factory.setTypeHandlersPackage(this.properties.typeHandlersPackage)
    }
    if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
      factory.setMapperLocations(this.properties.resolveMapperLocations())
    }
    // TODO 此处必为非 NULL
    val globalConfig = this.properties.globalConfig
    //注入填充器
    if (this.applicationContext.getBeanNamesForType(MetaObjectHandler::class.java,
            false, false).isNotEmpty()) {
      val metaObjectHandler = this.applicationContext.getBean(MetaObjectHandler::class.java)
      globalConfig.metaObjectHandler = metaObjectHandler
    }
    //注入主键生成器
    if (this.applicationContext.getBeanNamesForType(IKeyGenerator::class.java, false,
            false).isNotEmpty()) {
      val keyGenerator = this.applicationContext.getBean(IKeyGenerator::class.java)
      globalConfig.dbConfig.keyGenerator = keyGenerator
    }
    //注入sql注入器
    if (this.applicationContext.getBeanNamesForType(ISqlInjector::class.java, false,
            false).isNotEmpty()) {
      val iSqlInjector = this.applicationContext.getBean(ISqlInjector::class.java)
      globalConfig.sqlInjector = iSqlInjector
    }
    factory.setGlobalConfig(globalConfig)
    return factory.getObject()
  }

  @Bean
  @ConditionalOnMissingBean
  fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
    val executorType = this.properties.executorType
    return if (executorType != null) {
      SqlSessionTemplate(sqlSessionFactory, executorType)
    } else {
      SqlSessionTemplate(sqlSessionFactory)
    }
  }

  private fun applyConfiguration(factory: MybatisSqlSessionFactoryBean) {
    var configuration: MybatisConfiguration? = this.properties.configuration
    if (configuration == null && !StringUtils.hasText(this.properties.configLocation)) {
      configuration = MybatisConfiguration()
    }
    val xmlLessConfiguration = MybatisXmlLessConfiguration()
    if (configuration != null) {
      if (!CollectionUtils.isEmpty(this.configurationCustomizers)) {
        for (customizer in this.configurationCustomizers!!) {
          customizer.customize(configuration)
        }
      }
      BeanUtils.copyProperties(configuration, xmlLessConfiguration)
    }
    factory.setConfiguration(xmlLessConfiguration)
  }

}
