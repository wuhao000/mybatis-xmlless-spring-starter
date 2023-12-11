package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.MethodInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod

/**
 * Created by 吴昊 on 2023/12/10.
 */
class ColumnsResolverTest : BaseResolverTest() {

  @Test
  fun resolve() {
  }

  @Test
  fun resolveIncludedTables() {
  }

  @Test
  fun wrapColumn() {
  }

  @Test
  fun resolveColumnByPropertyName() {
    val mappings = MappingResolver.resolve(tableInfo, builderAssistant)
    val result = ColumnsResolver.resolveColumnByPropertyName(
        "userName",
        MethodInfo(StudentDAO::findByUserNameLike.javaMethod!!, modelClass, builderAssistant, mappings, mappings),
        true
    )
    assertEquals(1, result.size)
    assertEquals("user_id_t_user.name", result.first().toSql())
  }
}
