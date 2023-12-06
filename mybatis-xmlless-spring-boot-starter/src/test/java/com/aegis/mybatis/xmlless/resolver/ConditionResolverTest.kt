package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.QueryCriteria
import com.aegis.mybatis.xmlless.model.QueryType
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

/**
 * 测试条件解析
 *
 * @author 吴昊
 * @since 0.0.8
 */
class ConditionResolverTest : BaseResolverTest(
    Student::class.java, StudentDetailDAO::class.java, "findByFavorites"
) {

  @Test
  fun resolveConditions() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    TestDAO::class.java.methods.forEach { function ->
      val a = function.name.replace("findBy", "")
      val conditions = resolveConditions(a, function, mappings, QueryType.Select)
      println("${function.name} *********************")
      conditions.forEach {
        println(it)
      }
    }
  }

  private fun resolveConditions(
      conditionExpression: String,
      function: Method,
      mappings: FieldMappings,
      queryType: QueryType
  ): List<QueryCriteria> {
    return CriteriaResolver.resolveConditions(
        conditionExpression.toWords(), function, mappings, queryType
    )
  }

}

/**
 *
 * @author 吴昊
 * @since 0.0.8
 */
@Suppress("unused")
class TestDAO {

  fun findByAgeBetweenMinAndMax() {
  }

  fun findByNameEqValueWuhao() {
  }

  fun findByNameLikeKeywords() {
  }

}
