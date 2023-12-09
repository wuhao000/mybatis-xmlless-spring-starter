package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDAOResolverTest : BaseResolverTest(
    Student::class.java, StudentDAO::class.java,
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
    val mappings = MappingResolver.getMappingCache(Student::class.java)
    val cols = ColumnsResolver.resolve(mappings!!, Properties())
    cols.map {
      it.toSql()
    }.forEach {
      println(it)
    }
  }

  @Test
  fun resolveFindAll() {
  }

  @Test
  fun resolveFindBySubjectId() {
    val q = queries.find { it.method == StudentDAO::findByCreateTimeEqDate.javaMethod }
    println(q)
  }


  @Test
  fun resolveFindByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd() {
    val q = queries.find { it.method == StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd.javaMethod }
    println(q)
  }

  @Test
  fun resolveFindByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable() {
    val q = queries.find { it.method == StudentDAO::findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable.javaMethod }
    println(q)
  }

  @Test
  fun resolveFindAllPage() {
    val query = createQueryForMethod(StudentDAO::findAllPageable.javaMethod!!)
    println(query)
  }

  @Test
  fun find() {
    val query = createQueryForMethod(StudentDAO::find.javaMethod!!)
    println(query)
  }

  @Test
  fun findByNameAndAgeAndUserNameOrCreateUserNameLikeKeywords() {
    val query = createQueryForMethod(StudentDAO::findByNameAndAgeAndUserNameLikeKeywordsOrCreateUserNameLikeKeywords.javaMethod!!)
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
    queries.forEach {
      println(it)
    }
    val resultMaps = builderAssistant.configuration.resultMaps
    val resultMap = resultMaps.first {
      it.id == "$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById"
    }
    resultMap.propertyResultMappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}")
    }
  }


}
