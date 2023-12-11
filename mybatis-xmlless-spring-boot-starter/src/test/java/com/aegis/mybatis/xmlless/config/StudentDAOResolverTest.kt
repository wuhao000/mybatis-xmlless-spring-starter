package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentVO
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.model.MethodInfo
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDAOResolverTest : BaseResolverTest(
    StudentDAO::class.java,
    Student::class.java,
    "find",
    "findByCreateTimeEqDate",
    "findByNameOrAge",
    "findByAgeBetween",
    "findByAgeBetweenMinAndMax",
    "findAllPageable",
    "findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd",
    "findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable",
    "findAllPage",
    "findById"
) {

  @Test
  fun mappingResolve() {
    val allMappings = MappingResolver.getAllMappings()
    val scoreMapping = allMappings.first {
      it.modelClass == Score::class.java
    }
    scoreMapping.mappings.forEach {
      println(it.property + "/" + it.column)
    }
  }

  @Test
  fun resolveColumns() {
    val c2 = QueryResolver.resolveReturnType(StudentDAO::findVOPage.javaMethod!!, modelClass)
    assertEquals(StudentVO::class.java, c2)
    val mappings = MappingResolver.resolveNonEntityClass(
        StudentVO::class.java,
        Student::class.java,
        tableInfo,
        builderAssistant
    )
    val methodInfo = MethodInfo(
        StudentDAO::findVO.javaMethod!!, Student::class.java,
        builderAssistant, mappings, mappings
    )
    val cols = ColumnsResolver.resolve(Properties(), methodInfo)
    assert(cols.size > 4)
  }

  @Test
  fun resolveFindAll() {
  }

  @Test
  fun resolveFindBySubjectId() {
    val q = createQueryForMethod(StudentDAO::findByCreateTimeEqDate.javaMethod!!)
    println(q)
  }

  @Test
  fun resolveFindByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd() {
    val q = createQueryForMethod(StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd.javaMethod!!)
    println(q)
  }

  @Test
  fun resolveFindByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable() {
    val q = createQueryForMethod(StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable.javaMethod!!)
    println(q)
  }

  @Test
  fun resolveFindAllPage() {
    val query = createQueryForMethod(StudentDAO::findAllPageable.javaMethod!!)
    println(query)
  }

  @Test
  fun findByUserNameLike() {
    val query = createQueryForMethod(StudentDAO::findByUserNameLike.javaMethod!!)
    assertNotNull(query.query)
    val sql = query.sql
    println(sql)
    assertNotNull(sql)
    assert(sql.contains("user.name"))
    assertContains(sql, "del_flag = 0")
    assertEquals(sql.indexOf("del_flag = 0"), sql.lastIndexOf("del_flag = 0"))
    println(query)
  }

  @Test
  fun find() {
    val query = createQueryForMethod(StudentDAO::find.javaMethod!!)
    assertNotNull(query.query)
    val sql = query.sql
    println(sql)
    assertNotNull(sql)
    assert(sql.contains("user.name"))
    assertContains(sql, "del_flag = 0")
    assertEquals(sql.indexOf("del_flag = 0"), sql.lastIndexOf("del_flag = 0"))
    println(query)
  }

  @Test
  fun statistics() {
    val query = createQueryForMethod(StudentDAO::statistics.javaMethod!!)
    assertNotNull(query.query)
    val sql = query.sql
    assertNotNull(sql)
    assert(sql.contains("AS avg_age"))
    assert(sql.contains("AS sum_age"))
    assert(sql.contains("sum(t_student.age)"))
    assert(sql.contains("avg(t_student.age)"))
  }

  @Test
  @DisplayName("返回vo类，vo类中包含关联字段")
  fun findVO() {
    val c = QueryResolver.resolveReturnType(StudentDAO::findVO.javaMethod!!, modelClass)
    assertEquals(StudentVO::class.java, c)
    val query = createQueryForMethod(StudentDAO::findVO.javaMethod!!)
    assertNotNull(query.query)
    val sql = query.sql
    println(sql)
    assertNotNull(sql)
    assert(
        sql.contains(
            """LEFT JOIN
    xx AS"""
        )
    )
    assert(sql.contains("xx.wz LIKE"))
    assert(!sql.contains("create_time"))
    assertContains(sql, "del_flag = 0")
    assertEquals(sql.indexOf("del_flag = 0"), sql.lastIndexOf("del_flag = 0"))
  }

  @Test
  fun findByNameAndAgeAndUserNameOrCreateUserNameLikeKeywords() {
    val query =
        createQueryForMethod(StudentDAO::findByNameAndAgeAndUserNameLikeKeywordsOrCreateUserNameLikeKeywords.javaMethod!!)
    println(query)
  }

  @Test
  fun resolveParameter() {
    val query = createQueryForMethod(StudentDAO::findByNameOrAge.javaMethod!!)
    println(query)
  }

  @Test
  fun findByAgeBetween() {
    val query = createQueryForMethod(StudentDAO::findByAgeBetween.javaMethod!!)
    println(query)
  }

  @Test
  fun findByAgeBetweenMinAndMax() {
    val query = createQueryForMethod(StudentDAO::findByAgeBetweenMinAndMax.javaMethod!!)
    println(query)
  }

  @Test
  fun resolveFindByPhoneNumberLikeLeft() {
    val query = createQueryForMethod(StudentDAO::findByPhoneNumberLikeLeft.javaMethod!!)
    println(query)
  }

  @Test
  fun resolvePartlyUpdate() {
    val query = createQueryForMethod(StudentDAO::updatePartly.javaMethod!!)
    println(query)
  }

  @Test
  fun resolveResultMap() {
    val methods = listOf(
        StudentDAO::findById.javaMethod!!
    )
    methods.forEach {
      createQueryForMethod(it)
    }
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    println(ids.size)
    ids.forEach {
      println(it)
    }
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById"))
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores"))
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores_subject"))
    val scoreMap = resultMaps.first { it.id == "$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores" }
    assert(scoreMap.autoMapping == true)
    assert(scoreMap.hasNestedResultMaps())
    val resultMappings = scoreMap.propertyResultMappings
    assert(resultMappings.any { it.column == "subject_id" })
    assert(resultMappings.any { it.property == "subject" })
    val resultMap = builderAssistant.configuration.getResultMap(
        "$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById"
    )
    val mappings = resultMap.propertyResultMappings
    mappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}/${it.typeHandler?.javaClass}")
    }
  }

  @Test
  fun resolveSpecValue() {
    mapperClass.methods
        .first { it.name == "findByGraduatedEqTrue" }.also {
          val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
          println(query.toString())
          assert(query.toString().contains("graduated = TRUE"))
        }
  }

  @Test
  fun resultMaps() {
    val method = StudentDAO::findById.javaMethod!!
    createQueryForMethod(method)
    val resultMaps = builderAssistant.configuration.resultMaps
    val resultMap = resultMaps.first {
      it.id == buildResultMapId(method)
    }
    resultMap.propertyResultMappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}")
    }
  }

}
