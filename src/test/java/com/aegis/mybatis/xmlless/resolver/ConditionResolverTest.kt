package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.QueryCriteria
import org.junit.Test
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

/**
 * 测试条件解析
 *
 * @author 吴昊
 * @since 0.0.8
 */
class ConditionResolverTest:BaseResolverTest(
    Student::class.java,StudentDAO::class.java,"findById"
) {

  @Test
  fun resolveConditions() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    TestDAO::class.declaredFunctions.forEach { function ->
      val a = function.name.replace("findBy", "")
      val conditions = resolveConditions(a, function, mappings)
      println("${function.name} *********************")
      conditions.forEach {
        println(it)
      }
    }
  }

  private fun resolveConditions(conditionExpression: String, function: KFunction<*>, mappings: FieldMappings): List<QueryCriteria> {
    return CriteriaResolver.resolveConditions(
        conditionExpression.toWords(), function, mappings
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
