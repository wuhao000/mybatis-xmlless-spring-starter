package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.dao.StudentDetailDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDetailDAOTest : BaseTest() {

  @Autowired
  private lateinit var studentDAO: StudentDetailDAO

  @Test
  fun findByFavorites() {
    val s = studentDAO.findByFavorites("登山")
    println(s)
  }

  @Test
  fun findByFavoritesIn() {
    val s = studentDAO.findByFavoritesIn(listOf("登山"))
    println(s)
  }

  @Test
  fun getJsonArray() {
    val s = studentDAO.findFavorites()
    assertEquals(s.size, 1)
    println(s)
  }

  @Test
  fun getJsonObject() {
    val s = studentDAO.findDetail()
    assertEquals(s.size, 1)
    println(s)
  }

}
