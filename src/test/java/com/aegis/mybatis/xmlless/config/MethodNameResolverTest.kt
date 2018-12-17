package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.session.Configuration
import org.junit.Before
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
//@RunWith(SpringRunner::class)
//@SpringBootTest
class MethodNameResolverTest {

  private val configuration = Configuration().apply {
    this.isMapUnderscoreToCamelCase = true
  }
  private val currentNameSpace = "np"
  private val mapperClass = StudentDAO::class.java
  private val modelClass = Student::class.java
  private val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
  private val builderAssistant = MapperBuilderAssistant(configuration, resource).apply {
    currentNamespace = currentNameSpace
  }
  private lateinit var queries: List<ResolvedQuery>
  private lateinit var tableInfo: TableInfo

  @Before
  fun init() {
    GlobalConfigUtils.setGlobalConfig(
        configuration,
        GlobalConfig().setDbConfig(GlobalConfig.DbConfig().apply {
          this.tablePrefix = "t_"
        })
    )
    tableInfo = createTableInfo(modelClass)
    queries = mapperClass.kotlin.declaredFunctions
        .filter { it.name.startsWith("findById") }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }
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
  fun resolveColumns(){
    val mappings = MappingResolver.getMappingCache(Student::class.java)
    val cols = ColumnsResolver.resolve(mappings!!, listOf())
    println(cols)
  }

  @Test
  fun resolve() {

    queries.forEach {
      println(it)
    }
    val tableNames = TableInfoHelper.getTableInfos()
        .map { it.tableName }
    println(tableNames.joinToString("\n"))
    val resultMaps = builderAssistant.configuration.resultMaps
    println(resultMaps)
  }

  @Test
  fun resolve2() {
    Student::class.java.declaredFields.forEach {
      println(AnnotationUtils.findAnnotation(it, UpdateIgnore::class.java))
    }
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
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById"))
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores"))
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores_subject"))
    val scoreMap = resultMaps.first { it.id == "$currentNameSpace.com_aegis_mybatis_dao_StudentDAO_findById_scores" }
    assert(scoreMap.autoMapping == true)
    assert(scoreMap.hasNestedResultMaps())
    val resultMappings = scoreMap.propertyResultMappings
    assert(resultMappings.any { it.column == "subject_id" })
    assert(resultMappings.any { it.property == "subject" })
    val subjectMapping = resultMappings.first { it.property == "subject" }
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

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.kotlin.declaredFunctions
        .filter { it.name == name }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }.first()
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    TableInfoHelper.initTableInfo(
        builderAssistant, modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}
