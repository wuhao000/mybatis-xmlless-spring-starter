package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.dao.StudentQueryForm
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.util.getTableInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

/**
 * Created by 吴昊 on 2023/12/9.
 */
class QueryCriteriaTest : BaseResolverTest(
    StudentDAO::class.java, Student::class.java
) {

  @Test
  fun getColumns() {
  }

  @Test
  fun setColumns() {
  }

  @Test
  fun hasExpression() {
  }

  @Test
  fun toSql() {
  }

  @Test
  fun toSqlWithoutTest() {
  }

  @Test
  fun testToString() {
  }

  @Test
  fun wrapWithTests() {
  }

  @Test
  fun getCriteriaList() {
  }

  @Test
  fun getTests() {
    MappingResolver.resolve(modelClass, getTableInfo(modelClass, builderAssistant)!!, builderAssistant)
    val mappings = MappingResolver.getMappingCache(modelClass)
    val c = QueryCriteria(
        "name",
        Operations.Like,
        Append.AND,
        listOf("form.name" to StudentQueryForm::name.javaField!!),
        null,
        MethodInfo(StudentDAO::find.javaMethod!!, modelClass, mappings!!, mappings)
    )
    assertEquals("form.name != null and form.name.length() &gt; 0", c.getTests(null))
  }

  @Test
  fun getTests2() {
    MappingResolver.resolve(modelClass, getTableInfo(modelClass, builderAssistant)!!, builderAssistant)
    val mappings = MappingResolver.getMappingCache(modelClass)
    val field = StudentQueryForm::type.javaField!!
    val c = QueryCriteria(
        "type",
        Operations.Like,
        Append.AND,
        listOf("form.type" to field),
        null,
        MethodInfo(StudentDAO::find.javaMethod!!, modelClass, mappings!!, mappings)
    )
    assertEquals(
        "form.type == 5",
        c.getTests(
            TestInfo("== 5", field)
        )
    )
  }

  @Test
  fun getProperty() {
  }

  @Test
  fun getOperator() {
  }

  @Test
  fun getAppend() {
  }

  @Test
  fun setAppend() {
  }

  @Test
  fun getParameters() {
  }

  @Test
  fun setParameters() {
  }

  @Test
  fun getSpecificValue() {
  }

  @Test
  operator fun component1() {
  }

  @Test
  operator fun component2() {
  }

  @Test
  operator fun component3() {
  }

  @Test
  operator fun component4() {
  }

  @Test
  operator fun component5() {
  }

  @Test
  fun copy() {
  }

  @Test
  fun testHashCode() {
  }

  @Test
  fun testEquals() {
  }
}
