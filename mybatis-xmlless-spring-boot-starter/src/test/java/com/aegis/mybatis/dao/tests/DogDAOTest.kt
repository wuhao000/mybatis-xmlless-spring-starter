package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.dao.DogDAO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * Created by 吴昊 on 2018/12/17.
 */
open class DogDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: DogDAO

  @Test
  @DisplayName("物理删除")
  fun delete() {
    dao.deleteById(1)
    assert(!dao.existsById(1))
  }

  @Test
  @DisplayName("逻辑删除")
  fun deleteLogic() {
    val res = dao.deleteLogicById(1)
    println(res)
    val dog = dao.findById(1)
    assertNotNull(dog)
    assert(dog.deleteFlag)
  }

  @Test
  @DisplayName("查询所有数据")
  fun findAll() {
    val list = dao.findAll()
    assert(list.isNotEmpty())
  }

  @Test
  @DisplayName("查询未逻辑删除的数据")
  fun findAllExcludeLogicDeleted() {
    val list = dao.findNonDeleted()
    assertEquals(2, list.size)
  }

  @Test
  fun findByAgesIn() {
    val result = dao.findByAgesIn(listOf(2, 3))
    assert(result.isNotEmpty())
  }

  @Test
  fun findById() {
    val dog = dao.findById(1)
    assertNotNull(dog)
  }

  @Test
  fun findByNamesIn() {
    val result = dao.findByNamesIn(listOf("a"))
    result.forEach {
      println(it.names)
    }
  }

  @Test
  @DisplayName("查询已删除的数据")
  fun findDeleted() {
    val list = dao.findDeleted()
    list.forEach {
      assert(it.deleteFlag)
    }
    assert(list.isNotEmpty())
  }

  @Test
  @DisplayName("查询未删除的数据")
  fun findNotDeleted() {
    val list = dao.findNonDeleted()
    assertEquals(2, list.size)
  }

  @Test
  fun save() {
    dao.save(Dog().apply {
      name = "df"
    })
    val s2 = dao.findByName("df")
    println(s2?.createTime)
    assertNotNull(s2)
    assertNotNull(s2.createTime)
  }

}
