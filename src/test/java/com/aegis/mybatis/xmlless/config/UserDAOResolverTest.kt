package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.xmlless.model.Properties
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
    User::class.java, UserDAO::class.java, method
) {

  companion object {
    val method = "findById"
  }
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
    val cols = ColumnsResolver.resolve(mappings!!, Properties())
    println(cols)
  }

  @Test
  fun resolveFindAll() {
  }

  @Test
  fun resolvePartlyUpdate() {
    val query = createQueryForMethod("update")
    println(query)
  }

  @Test
  fun resolveResultMap() {
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    println(builderAssistant.hashCode())
    println(ids)
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_UserDAO_$method"))
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
