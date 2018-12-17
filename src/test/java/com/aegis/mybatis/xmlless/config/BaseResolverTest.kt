package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.session.Configuration
import org.junit.Before
import kotlin.reflect.full.declaredFunctions

open class BaseResolverTest(val modelClass: Class<*>,
                            val mapperClass: Class<*>,
                            val method: String) {

  protected val configuration = Configuration().apply {
    this.isMapUnderscoreToCamelCase = true
  }
  protected val currentNameSpace = "np"
  protected lateinit var queries: List<ResolvedQuery>
  protected val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
  protected val builderAssistant = MapperBuilderAssistant(configuration, resource).apply {
    currentNamespace = currentNameSpace
  }
  protected lateinit var tableInfo: TableInfo

  @Before
  fun init() {
    GlobalConfigUtils.setGlobalConfig(
        configuration,
        GlobalConfig().setDbConfig(GlobalConfig.DbConfig().apply {
          this.tablePrefix = "t_"
        })
    )
    tableInfo = createTableInfo(modelClass)
    queries = mapperClass.kotlin.declaredFunctions
        .filter { it.name == method }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    TableInfoHelper.initTableInfo(
        builderAssistant, modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}
