package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.Test
import org.springframework.core.annotation.AnnotationUtils
import kotlin.reflect.full.declaredFunctions


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class UserDAOResolverTest : BaseResolverTest(
    User::class.java, UserDAO::class.java, "findSimpleUserById"
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
  fun resolve2() {
    Student::class.java.declaredFields.forEach {
      println(AnnotationUtils.findAnnotation(it, UpdateIgnore::class.java))
    }
  }

  @Test
  fun resolveColumns() {
    val mappings = MappingResolver.getMappingCache(Student::class.java)
    val cols = ColumnsResolver.resolve(mappings!!, listOf())
    println(cols)
  }

  @Test
  fun resolveFindAll() {
  }

  @Test
  fun resolvePartlyUpdate() {
    val query = createQueryForMethod("updatePartly")
    println(query)
  }

  @Test
  fun resolveResultMap() {
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_UserDAO_findById"))
    val scoreMap = resultMaps.first { it.id == "$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores" }
    assert(scoreMap.autoMapping == true)
    assert(scoreMap.hasNestedResultMaps())
    val resultMappings = scoreMap.propertyResultMappings
    assert(resultMappings.any { it.column == "subject_id" })
    assert(resultMappings.any { it.property == "subject" })
    resultMaps.forEach {
      println(it.id)
    }
    println(resultMaps)
  }

  @Test
  fun resolveSpecValue() {
    mapperClass.kotlin.declaredFunctions
        .first { it.name == "findByGraduatedEqTrue" }.also {
          val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
          println(query.toString())
          assert(query.toString().contains("graduated = TRUE"))
        }
  }

  @Test
  fun resultMapResolve() {
    val resultMaps = builderAssistant.configuration.resultMaps
    resultMaps.forEach {
      println(it.type)
      println(it.mappedColumns)
      println(it.mappedProperties)
    }
  }

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.kotlin.declaredFunctions
        .filter { it.name == name }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }.first()
  }

}
