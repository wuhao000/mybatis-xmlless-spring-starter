package com.aegis.mybatis.xmlless.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

/**
 * 排序条件解析测试
 *
 * @author 吴昊
 * @date 2023/12/11 17:34
 * @since v4.0.0
 * @version 1.0
 */
class SortResolveTest {

  @Test
  fun resolve1() {
    val s1 = QueryResolver.resolveSortFromExpression("OrderByCreateTimeDesc")
    assertEquals(1, s1.size)
    assertEquals(Sort.Direction.DESC, s1.first().direction)
    assertEquals("createTime", s1.first().property)
  }

  @Test
  fun resolve2() {
    val s2 = QueryResolver.resolveSortFromExpression("order by createTime desc")
    assertEquals(1, s2.size)
    assertEquals(Sort.Direction.DESC, s2.first().direction)
    assertEquals("createTime", s2.first().property)
  }

  @Test
  fun resolve3() {
    val s3 = QueryResolver.resolveSortFromExpression("OrderByCreateTimeDescAndUpdateTimeDesc")
    assertEquals(2, s3.size)
    assertEquals(Sort.Direction.DESC, s3[0].direction)
    assertEquals("createTime", s3[0].property)
    assertEquals(Sort.Direction.DESC, s3[1].direction)
    assertEquals("updateTime", s3[1].property)
  }

  @Test
  fun resolve4() {
    val s4 = QueryResolver.resolveSortFromExpression("OrderByCreateTimeAscAndUpdateTimeDesc")
    assertEquals(2, s4.size)
    assertEquals(Sort.Direction.ASC, s4[0].direction)
    assertEquals("createTime", s4[0].property)
    assertEquals(Sort.Direction.DESC, s4[1].direction)
    assertEquals("updateTime", s4[1].property)
  }

}
