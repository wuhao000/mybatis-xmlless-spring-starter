package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.Before
import kotlin.reflect.full.declaredFunctions

open class BaseResolverTest(val modelClass: Class<*>,
                            val mapperClass: Class<*>,
                            vararg methods: String) {

  var methods: List<String> = methods.toList()

  protected val configuration = MybatisConfiguration().apply {
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
    tableInfo = createTableInfo(modelClass)
    queries = mapperClass.kotlin.declaredFunctions
        .filter { it.name in methods }
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
