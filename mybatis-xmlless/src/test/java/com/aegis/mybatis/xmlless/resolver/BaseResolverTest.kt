package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.aegis.mybatis.xmlless.util.initTableInfo
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseResolverTest<T>(
    val modelClass: Class<T>,
    vararg methods: String
) {

  var methods: List<String> = methods.toList()

  protected val configuration = MybatisConfiguration().apply {
    this.isMapUnderscoreToCamelCase = true
  }
  protected val currentNameSpace = "np"
  protected val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
  protected val builderAssistant = MapperBuilderAssistant(configuration, resource).apply {
    currentNamespace = currentNameSpace
  }
  protected val tableInfo: TableInfo = createTableInfo(modelClass)
  protected val mappings = MappingResolver.resolve(tableInfo, builderAssistant)

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    initTableInfo(
        builderAssistant, modelClass
    )
    return getTableInfo(modelClass, builderAssistant)
  }

}
