package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.DogDAO
import com.aegis.mybatis.xmlless.annotations.MyBatisIgnore
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.aegis.mybatis.xmlless.util.FieldUtil
import org.junit.jupiter.api.Test
import org.springframework.core.annotation.AnnotationUtils
import kotlin.reflect.jvm.javaField


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class DogDAOResolverTest : BaseResolverTest(
    Dog::class.java, DogDAO::class.java,
    "findById"
) {


  @Test
  fun resolve2() {
    Student::class.java.declaredFields.forEach {
      if (it == Student::scores.javaField) {
        assert(FieldUtil.isInsertIgnore(it))
        assert(FieldUtil.isUpdateIgnore(it))
      }
      if (it == Student::createTime.javaField) {
        assert(!FieldUtil.isInsertIgnore(it))
        assert(FieldUtil.isUpdateIgnore(it))
      }
    }
  }

  @Test
  fun resolveColumns() {
    val mappings = MappingResolver.getMappingCache(Dog::class.java)
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
  fun findById() {
    val q = queries.find { it.method.name == "findById" }
    println(q)
  }

  @Test
  fun resolveResultMap() {
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    println(ids.size)
    ids.forEach {
      println(it)
    }
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_DogDAO_findById"))
    val resultMap = builderAssistant.configuration.getResultMap(
        "$currentNameSpace.com_aegis_mybatis_dao_DogDAO_findById"
    )
    val mappings = resultMap.propertyResultMappings
    mappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}/${it.typeHandler?.javaClass}")
    }
  }

  @Test
  fun resultMaps() {
    queries.forEach {
      println(it)
    }
    val resultMaps = builderAssistant.configuration.resultMaps
    val resultMap = resultMaps.first {
      it.id == "$currentNameSpace.com_aegis_mybatis_dao_DogDAO_findById"
    }
    resultMap.propertyResultMappings.forEach {
      println("${it.property}/${it.column}/${it.javaType}")
    }
  }

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.methods
        .filter { it.name == name }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }.first()
  }

}
