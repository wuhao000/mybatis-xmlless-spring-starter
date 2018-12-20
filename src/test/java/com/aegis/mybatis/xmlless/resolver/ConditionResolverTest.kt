package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.Condition
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.junit.Test
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod

/**
 * 测试条件解析
 *
 * @author 吴昊
 * @since 0.0.8
 */
class ConditionResolverTest {

  @Test
  fun resolveConditions() {
    TestDAO::class.declaredFunctions.forEach { function ->
      val a = function.name.replace("findBy", "")
      val conditions = resolveConditions(a, function)
      println("${function.name} *********************")
      conditions.forEach {
        println(it)
      }
    }
  }

  private fun resolveConditions(conditionExpression: String, function: KFunction<*>): List<Condition> {
    val paramNames = ParamNameResolver(
        Configuration().apply {
          this.isUseActualParamName = true
        }, function.javaMethod
    ).names
    return ConditionResolver.resolveConditions(
        conditionExpression.toWords(), function
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
