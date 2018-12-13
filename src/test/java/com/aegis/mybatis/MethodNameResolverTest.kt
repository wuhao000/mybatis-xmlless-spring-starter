package com.aegis.mybatis

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.xmlless.config.QueryResolver
import com.aegis.mybatis.xmlless.model.ResolvedQueries
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
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

  @Test
  fun resolve() {
    val modelClass = Student::class.java
    val mapperClass = StudentDAO::class.java
    val tableInfo = createTableInfo(modelClass)
    val queries = ResolvedQueries(mapperClass)
    mapperClass.kotlin.declaredFunctions
        .filter { it.name.startsWith("findAllPage") }
        .forEach {
          val query = QueryResolver.resolve(it, tableInfo, modelClass, mapperClass)
          query.query
          queries.add(query)
        }
    queries.log()
  }

  @Test
  fun resolve2() {
    Student::class.java.declaredFields.forEach {

      println(AnnotationUtils.findAnnotation(it,UpdateIgnore::class.java))
    }
  }

  @Test
  fun resolveFindAll() {
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
    TableInfoHelper.initTableInfo(
        MapperBuilderAssistant(MybatisConfiguration(), resource), modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}
