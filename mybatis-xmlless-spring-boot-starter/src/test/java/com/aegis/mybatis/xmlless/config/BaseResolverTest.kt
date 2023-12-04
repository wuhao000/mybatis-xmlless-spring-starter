package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseResolverTest(
    val modelClass: Class<*>,
    val mapperClass: Class<*>,
    vararg methods: String
) {

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


  @BeforeAll
  fun init() {
    tableInfo = createTableInfo(modelClass)
    queries = getFunctions()
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }
  }

  fun getFunctions(): List<KFunction<*>> {
    return mapperClass.kotlin.functions.filter { it.name in methods }
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    TableInfoHelper.initTableInfo(
        builderAssistant, modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}
