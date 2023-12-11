package com.aegis.mybatis.xmlless.util

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.TypeAliasRegistry
import org.mybatis.logging.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.io.IOException

/**
 * Created by 吴昊 on 2023/12/11.
 */
object TypeUtil {

  private val LOGGER = LoggerFactory.getLogger(
      MybatisSqlSessionFactoryBean::class.java
  )

  private lateinit var registry: TypeAliasRegistry

  fun <T> resolve(str: String): Class<T> {
    return registry.resolveAlias<T>(str)
  }

  fun init(packagePatterns: String, assignableType: Class<*>?) {
    registry = TypeAliasRegistry()
    scanClasses(packagePatterns, assignableType).forEach {
      registry.registerAlias(it)
    }
  }

  fun init(factory: SqlSessionFactory) {
    registry = factory.configuration.typeAliasRegistry
  }

  @Throws(IOException::class)
  private fun scanClasses(packagePatterns: String, assignableType: Class<*>?): Set<Class<*>> {
    val classes: MutableSet<Class<*>> = HashSet()
    val packagePatternArray = StringUtils.tokenizeToStringArray(
        packagePatterns,
        ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS
    )
    for (packagePattern: String in packagePatternArray) {
      val resources = PathMatchingResourcePatternResolver().getResources(
          ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
              + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class"
      )
      for (resource: Resource in resources) {
        try {
          val classMetadata = CachingMetadataReaderFactory().getMetadataReader(resource).classMetadata
          val clazz = Resources.classForName(classMetadata.className)
          if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
            classes.add(clazz)
          }
        } catch (e: Throwable) {
          LOGGER.warn { "Cannot load the '" + resource + "'. Cause by " + e.toString() }
        }
      }
    }
    return classes
  }

}
