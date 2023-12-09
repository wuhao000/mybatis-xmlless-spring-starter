package com.aegis.mybatis.xmlless.resolver

import com.aegis.kotlin.toWords
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.enums.TestType
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.MethodInfo
import com.aegis.mybatis.xmlless.model.QueryCriteria
import com.aegis.mybatis.xmlless.model.QueryType
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.lang.reflect.Method
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals

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

  @Test
  fun resolveConditions() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    TestDAO::class.java.declaredMethods.forEach { method ->
      val exp = method.name.replace("findBy", "")
      println("${method.name} *********************")
      val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
      conditions.forEach {
        println(it)
      }
    }
  }

  @Test
  fun resolveNamedComplexParam() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge2.javaMethod!!
    val exp = "NameAndAge"
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(2, conditions.size)
    assertEquals("name = #{f.name}", conditions[0].toString())
    assertEquals("age = #{f.age}", conditions[1].toString())
  }

  @Test
  fun resolveComplexParam() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(2, conditions.size)
    assertEquals("name = #{name}", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
  }


  @Test
  fun resolveComplexParam9() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::findByNameAndAge3.javaMethod!!
    val exp = "NameAndAge"
    assertEquals("NameAndAge", exp)
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(2, conditions.size)
    assertEquals("name = #{name}", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
  }

  @Test
  fun resolveComplexParam2() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd.javaMethod!!
    val exp = method.name.replace("findBy", "")
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{name},'%')", conditions[0].toString())
    assertEquals("age = #{age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{start} AND #{end}", conditions[2].toString())
  }

  @Test
  fun resolveComplexParam3() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable.javaMethod!!
    val exp = "NameLikeAndAgeAndCreateTimeBetweenStartAndEnd"
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{form.name},'%')", conditions[0].toString())
    assertEquals("age = #{form.age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{form.start} AND #{form.end}", conditions[2].toString())
  }

  @Test
  fun resolveComplexParam4() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable3.javaMethod!!
    val exp = "NameLikeAndAgeAndCreateTimeBetweenStartAndEnd"
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(3, conditions.size)
    assertEquals("name LIKE CONCAT('%', #{form.name},'%')", conditions[0].toString())
    assertEquals("age = #{form.age}", conditions[1].toString())
    assertEquals("createTime BETWEEN #{form.start} AND #{form.end}", conditions[2].toString())
  }

  @Test
  fun resolveBetweenWithoutParamName() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::findByAgeBetween.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("AgeBetween", exp)
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(1, conditions.size)
    assertEquals("age BETWEEN #{minAge} AND #{maxAge}", conditions.first().toString())
  }

  @Test
  fun resolveBetweenWithoutParamName7() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::selectDictTypeList.javaMethod!!
    val exp = "DictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime"
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(4, conditions.size)
    assertEquals("dictName = #{dictName}", conditions[0].toString())
    assertEquals("status = #{status}", conditions[1].toString())
    assertEquals("dictType = #{dictType}", conditions[2].toString())
    assertEquals("createTime BETWEEN #{beginTime} AND #{endTime}", conditions[3].toString())
  }


  @Test
  fun resolveBetweenWithoutParamName11() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::selectDictTypeListPageable.javaMethod!!
    val exp = "DictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime"
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(4, conditions.size)
    assertEquals("dictName = #{form.dictName}", conditions[0].toString())
    assertEquals("status = #{form.status}", conditions[1].toString())
    assertEquals("dictType = #{form.dictType}", conditions[2].toString())
    assertEquals("createTime BETWEEN #{form.beginTime} AND #{form.endTime}", conditions[3].toString())
    println(conditions[0].toSql(mappings))
    assertEquals(
        "<if test=\"form.dictName != null and form.dictName.length() &gt; 0\">\n" +
            "\tt_student.DICT_NAME = #{form.dictName} AND\n" +
            "</if>",
        conditions[0].toSql(mappings)
    )
  }

  @Test
  fun resolveBetween() {
    val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
    val method = TestDAO::findByAgeBetweenMinAndMax.javaMethod!!
    val exp = method.name.replace("findBy", "")
    assertEquals("AgeBetweenMinAndMax", exp)
    val conditions = resolveConditions(exp, method, mappings, QueryType.Select)
    assertEquals(1, conditions.size)
    assertEquals("age BETWEEN #{min} AND #{max}", conditions.first().toString())
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    TableInfoHelper.initTableInfo(
        builderAssistant, modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

  private fun resolveConditions(
      conditionExpression: String,
      method: Method,
      mappings: FieldMappings,
      queryType: QueryType
  ): List<QueryCriteria> {
    return CriteriaResolver.resolveConditions(
        conditionExpression.toWords(), MethodInfo(method, modelClass, mappings), mappings, queryType
    )
  }

}

/**
 *
 * @author 吴昊
 * @since 0.0.8
 */
@Suppress("unused")
interface TestDAO {

  fun findByAgeBetweenMinAndMax(min: Int, max: Int)

  @ResolvedName(name = "findByDictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime")
  @ExcludeProperties(properties = ["remark"])
  fun selectDictTypeList(dictType: SysDictTypeQueryForm?): List<SysDictType>

  @ResolvedName(name = "findByDictNameAndStatusAndDictTypeAndCreateTimeBetweenBeginTimeAndEndTime")
  fun selectDictTypeListPageable(
      @Param("form") form: SysDictTypeQueryForm?,
      @Param("pageable") pageable: Pageable?
  ): @ExcludeProperties(properties = ["remark"]) Page<SysDictType>

  fun findByAgeBetween(minAge: Int, maxAge: Int)

  @ResolvedName(
      name = "findByName",
      values = [
        ValueAssign(param = "name", stringValue = "wuhao")
      ]
  )
  fun findByNameEq()

  fun findByNameLikeKeywords(keywords: String)

  fun findByNameAndAge(form: Form)

  @ResolvedName("findByNameAndAge")
  fun findByNameAndAge3(form: Form2)

  @ResolvedName("findByNameAndAge")
  fun findByNameAndAge2(@Param("f") fffffform: Form)

}

open class Form {

  var name: String? = null
  var age: Int? = null

}

class Form2 : Form() {
  @Criteria(
      test = TestExpression(
          value = [TestType.EqTrue]
      ),
      expression = "1 = 2"
  )
  var active: Boolean = false
}
