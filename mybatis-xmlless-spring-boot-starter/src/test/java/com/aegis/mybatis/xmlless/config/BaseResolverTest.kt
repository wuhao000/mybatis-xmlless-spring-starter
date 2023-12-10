package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.aegis.mybatis.xmlless.util.initTableInfo
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.jupiter.api.TestInstance
import java.lang.reflect.Method

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseResolverTest(
    val mapperClass: Class<*> = StudentDAO::class.java,
    val modelClass: Class<*> = Student::class.java,
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


  fun getFunctions(): List<Method> {
    return mapperClass.methods.filter { it.name in methods }
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    initTableInfo(
        builderAssistant, modelClass
    )
    return getTableInfo(modelClass, builderAssistant)!!
  }


  protected fun createQueryForMethod(method: Method): ResolvedQuery {
    return QueryResolver.resolve(method, tableInfo, modelClass, mapperClass, builderAssistant)
  }

  protected fun buildResultMapId(method: Method): String {
    return "$currentNameSpace.${mapperClass.name.replace(".", "_")}_${method.name}"
  }
}
