package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.dao.SimpleStudent
import com.aegis.mybatis.dao.StudentBaseMapperDAO
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 * @author 吴昊
 * @date 2023/12/8 0:09
 * @since v4.0.0
 * @version 1.0
 */
class StudentBaseMapperDAOTest : BaseTest() {

  @Resource
  private lateinit var dao: StudentBaseMapperDAO

  @Test
  fun insert() {
    val st = SimpleStudent("1", "2")
    dao.insert(st)
  }

  @Test
  fun deleteById() {
    val st = SimpleStudent("1", "2")
    dao.insert(st)
    assertNotNull(dao.selectById("1"))
    dao.deleteById("1")
    assertNull(dao.selectById("1"))
  }

  @Test
  fun deleteByEntity() {
    val st = SimpleStudent("1", "2")
    dao.insert(st)
    assertNotNull(dao.selectById("1"))
    dao.deleteById(st)
    assertNull(dao.selectById("1"))
  }

  @Test
  fun findByQw() {
    val qw = Wrappers.query<SimpleStudent>()
        .eq("name", "1")
        .eq("gender", 1)
        .groupBy("name")
    qw.expression.normal.forEach {
      println(it.sqlSegment)
    }
    println(qw)
  }


}
