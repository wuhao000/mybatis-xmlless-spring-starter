package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
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

  private val mapperClass = StudentDAO::class.java
  private val modelClass = Student::class.java
  private lateinit var tableInfo: TableInfo

  @Before
  fun init() {
    tableInfo = createTableInfo(modelClass)
  }

  @Test
  fun resolve() {
    mapperClass.kotlin.declaredFunctions
        .filter { it.name.startsWith("findBySubjectId") }
        .forEach {
          val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass)
          println(query)
        }
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
  fun resolveSpecValue() {
    mapperClass.kotlin.declaredFunctions
        .first { it.name == "findByGraduatedEqTrue" }.also {
          val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass)
          println(query.toString())
          assert(query.toString().contains("graduated = TRUE"))
        }
  }

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.kotlin.declaredFunctions
        .filter { it.name == name }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass)
        }.first()
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
    TableInfoHelper.initTableInfo(
        MapperBuilderAssistant(MybatisConfiguration(), resource), modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}
