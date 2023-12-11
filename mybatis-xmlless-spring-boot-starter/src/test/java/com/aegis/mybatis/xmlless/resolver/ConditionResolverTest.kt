package com.aegis.mybatis.xmlless.resolver

import com.aegis.kotlin.toWords
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.annotations.TestCriteria
import com.aegis.mybatis.xmlless.annotations.ExcludeProperties
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.ValueAssign
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.MethodInfo
import com.aegis.mybatis.xmlless.model.QueryCriteria
import com.aegis.mybatis.xmlless.util.initTableInfo
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals

/**
 *
 * @author 吴昊
 * @since 0.0.8
 */
@Suppress("unused")
interface TestDAO {

  @ResolvedName(
      name = "findBy",
      conditions = [
        "name EQ 0"
      ]
  )
  fun findByNameEq0()

  /**
   * @param min
   * @param max
   */
  fun findByAgeBetweenMinAndMax(min: Int, max: Int)

  /**
   * @param dictType
   * @return
   */
  @ResolvedName(name = "findByDictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime")
  @ExcludeProperties(properties = ["remark"])
  fun selectDictTypeList(dictType: SysDictTypeQueryForm?): List<SysDictType>

  /**
   * @param form
   * @param pageable
   * @return
   */
  @ResolvedName(name = "findByDictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime")
  fun selectDictTypeListPageable(
      @Param("form") form: SysDictTypeQueryForm?,
      @Param("pageable") pageable: Pageable?
  ): @ExcludeProperties(properties = ["remark"]) Page<SysDictType>

  /**
   * @param minAge
   * @param maxAge
   */
  fun findByAgeBetween(minAge: Int, maxAge: Int)

  /**
   */
  @ResolvedName(
      name = "findByName",
      values = [
        ValueAssign(param = "name", stringValue = "wuhao")
      ]
  )
  fun findByNameEq()

  /**
   * @param keywords
   */
  fun findByNameLikeKeywords(keywords: String)

  /**
   * @param form
   */
  fun findByNameAndAge(form: Form)

  /**
   * @param form
   */
  @ResolvedName("findByNameAndAge")
  fun findByNameAndAge3(form: Form2)

  /**
   * @param fffffform
   */
  @ResolvedName("findByNameAndAge")
  fun findByNameAndAge2(@Param("f") fffffform: Form)

}

/**
 * 测试条件解析
 *
 * @author 吴昊
 * @since 0.0.8
 */
class ConditionResolverTest {

  protected val currentNameSpace = "np"
  protected val configuration = MybatisConfiguration().apply {
    this.isMapUnderscoreToCamelCase = true
  }
  private val modelClass = Student::class.java
  protected val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
  protected val builderAssistant = MapperBuilderAssistant(configuration, resource).apply {
    currentNamespace = currentNameSpace
  }
  private val tableInfo = createTableInfo(modelClass)
  protected val mappings = MappingResolver.resolve(tableInfo, builderAssistant)


  @Test
  fun toWords() {
    println("aEq0".toWords())
    println("aEqB".toWords())
    println("UserName Eq B".toWords())
  }

  @Test
  fun resolveConditions() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    TestDAO::class.java.declaredMethods.forEach { method ->
      val exp = method.name.replace("findBy", "")
      println("${method.name} *********************")
      val conditions = resolveConditions(exp, method, mappings)
      conditions.forEach {
        println(it)
      }
    }
  }

  @Test
  fun resolveNamedComplexParam() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge2.javaMethod!!
    val exp = "NameAndAge"
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(2, conditions.size)
    assertEquals("name = #{f.name}", conditions[0].toString())
    assertEquals("age = #{f.age}", conditions[1].toString())
  }

  @Test
  fun resolveComplexParam() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(2, conditions.size)
    assertEquals("name = #{name}", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
  }

  @Test
  fun resolveComplexParam9() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge3.javaMethod!!
    val exp = "NameAndAge"
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(2, conditions.size)
    assertEquals("name = #{name}", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
  }

  @Test
  fun resolveComplexParam2() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd.javaMethod!!
    val exp = method.name.replace("findBy", "")
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{name},'%')", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{start} AND #{end}", conditions[2].toString())
  }

  @Test
  fun resolveComplexParam3() {
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable.javaMethod!!
    val exp = "NameLikeAndAgeAndCreateTimeBetweenStartAndEnd"
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{form.name},'%')", conditions[0].toString())
    assertEquals("age = #{form.age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{form.start} AND #{form.end}", conditions[2].toString())
  }

  @Test
  fun resolveComplexParam4() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable3.javaMethod!!
    val exp = "NameLikeAndAgeAndCreateTimeBetweenStartAndEnd"
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{form.name},'%')", conditions[0].toString())
    assertEquals("age = #{form.age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{form.start} AND #{form.end}", conditions[2].toString())
  }

  @Test
  fun resolveBetweenWithoutParamName() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::findByAgeBetween.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("AgeBetween", exp)
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(1, conditions.size)
    assertEquals("age BETWEEN #{minAge} AND #{maxAge}", conditions.first().toString())
  }

  @Test
  fun resolveBetweenWithoutParamName7() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::selectDictTypeList.javaMethod!!
    val exp = "DictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime"
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(4, conditions.size)
    assertEquals("dictName = #{dictName}", conditions[0].toString())
    assertEquals("status = #{status}", conditions[1].toString())
    assertEquals("dictType = #{dictType}", conditions[2].toString())
    assertEquals("createTime BETWEEN #{beginTime} AND #{endTime}", conditions[3].toString())
  }

  @Test
  fun resolveBetweenWithoutParamName11() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val method = TestDAO::selectDictTypeListPageable.javaMethod!!
    val exp = "DictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime"
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(4, conditions.size)
    assertEquals("dictName = #{form.dictName}", conditions[0].toString())
    assertEquals("status = #{form.status}", conditions[1].toString())
    assertEquals("dictType = #{form.dictType}", conditions[2].toString())
    assertEquals("createTime BETWEEN #{form.beginTime} AND #{form.endTime}", conditions[3].toString())
    println(conditions[0].toSql())
    assertEquals(
        "<if test=\"form.dictName != null and form.dictName.length() &gt; 0\">\n" +
            "\tt_student.DICT_NAME = #{form.dictName} AND\n" +
            "</if>",
        conditions[0].toSql()
    )
  }

  @Test
  fun resolveBetween() {
    val method = TestDAO::findByAgeBetweenMinAndMax.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("AgeBetweenMinAndMax", exp)
    val conditions = resolveConditions(exp, method, mappings)
    assertEquals(1, conditions.size)
    assertEquals("age BETWEEN #{min} AND #{max}", conditions.first().toString())
  }

  @Test
  fun resolveEq0() {
    val method = TestDAO::findByNameEq0.javaMethod!!
    val conditions = resolveConditions("name eq 0", method, mappings)
    assertEquals(1, conditions.size)
    assertEquals("name = 0", conditions.first().toString())
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    return initTableInfo(
        builderAssistant, modelClass
    )
  }

  private fun resolveConditions(
      conditionExpression: String,
      method: Method,
      mappings: FieldMappings
  ): List<QueryCriteria> {
    return CriteriaResolver.resolveConditions(
        conditionExpression.toWords(), MethodInfo(method, modelClass, builderAssistant, mappings, mappings)
    )
  }

}

open class Form {

  var name: String? = null
  var age: Int? = null

}

class Form2 : Form() {

  @TestCriteria(
      expression = "1 = 2",
      testExpression = "= true"
  )
  var active: Boolean = false

}
