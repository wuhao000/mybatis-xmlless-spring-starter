package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentJavaDAO
import com.aegis.mybatis.xmlless.model.MethodInfo
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
class StudentJavaDAOResolverTest : BaseResolverTest(
    StudentJavaDAO::class.java,
    Student::class.java,
    "findById",
    "findAllPage"
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
    val methodInfo = MethodInfo(StudentJavaDAO::findById.javaMethod!!, Student::class.java, mappings!!, mappings!!)
    val cols = ColumnsResolver.resolve(Properties(), methodInfo)
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
    val q = createQueryForMethod(StudentJavaDAO::findBySubjectId.javaMethod!!)
    println(q)
  }

  @Test
  fun resolveFindAllPage() {
    val query = createQueryForMethod(StudentJavaDAO::findAllPage.javaMethod!!)
    println(query)
  }

  @Test
  fun resolveResultMap() {
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }

    assert(ids.contains(buildResultMapId(StudentJavaDAO::findById.javaMethod!!)))
    assert(ids.contains("${buildResultMapId(StudentJavaDAO::findById.javaMethod!!)}_scores"))
    assert(ids.contains("${buildResultMapId(StudentJavaDAO::findById.javaMethod!!)}_scores_subject"))
    val scoreMap = resultMaps.first { it.id == "${buildResultMapId(StudentJavaDAO::findById.javaMethod!!)}_scores" }
    assert(scoreMap.hasNestedResultMaps())
    val resultMappings = scoreMap.propertyResultMappings
    assert(resultMappings.any { it.column == "subject_id" })
    assert(resultMappings.any { it.property == "subject" })

    val resultMap = builderAssistant.configuration.getResultMap(
        buildResultMapId(StudentJavaDAO::findById.javaMethod!!)
    )
    val mappings = resultMap.propertyResultMappings
    mappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}/${it.typeHandler?.javaClass}")
    }
  }

  @Test
  fun resolveSpecValue() {
    mapperClass.methods.first { it.name == "findByGraduatedEqTrue" }.also {
      val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
      println(query.toString())
      assert(query.toString().contains("graduated = TRUE"))
    }
  }

  @Test
  fun resultMaps() {
    createQueryForMethod(StudentJavaDAO::findById.javaMethod!!)
    val resultMaps = builderAssistant.configuration.resultMaps
    val resultMap = resultMaps.first {
      it.id == buildResultMapId(StudentJavaDAO::findById.javaMethod!!)
    }
    resultMap.propertyResultMappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}")
    }
  }

}
