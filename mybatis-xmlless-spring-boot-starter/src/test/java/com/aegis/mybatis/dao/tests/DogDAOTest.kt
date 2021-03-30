package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.dao.AppClusterDAO
import com.aegis.mybatis.dao.DogDAO
import com.baomidou.mybatisplus.core.config.GlobalConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * Created by 吴昊 on 2018/12/17.
 */
open class DogDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: DogDAO

  @Test
  fun findById() {
    val dog = dao.findById(1)
    assertNotNull(dog)
  }

  @Test
  fun findAll(){
    val list = dao.findAll()
    assertEquals(1, list.size)
  }

  @Test
  fun delete() {
    dao.deleteById(1)
    assert(!dao.existsById(1))
  }

  @Test
  fun deleteLogic() {
    val res = dao.deleteLogicById(1)
    println(res)
    val dog = dao.findById(1)
    assertNotNull(dog)
    assert(dog.deleteFlag)
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
