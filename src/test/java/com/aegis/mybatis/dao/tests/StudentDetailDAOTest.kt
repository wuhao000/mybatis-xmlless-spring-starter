package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.EducationInfo
import com.aegis.mybatis.dao.StudentDetailDAO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
  @DisplayName("数组值等于查询")
  fun findByFavorites() {
    val s = studentDAO.findByFavorites("登山")
    println(s)
  }

  @Test
  fun findNickNamesById() {
    val s = studentDAO.findNickNamesById("a")
    println(s)
  }

  @Test
  @DisplayName("数组值包含查询")
  fun findByFavoritesIn() {
    val s = studentDAO.findByFavoritesIn(listOf("登山"))
    println(s)
  }

  @Test
  @DisplayName("属性为对象数组")
  fun findAll() {
    val list = studentDAO.findAll()
    list.forEach {
      assert(it.education != null)
      it.education?.forEach {
        assert(it is EducationInfo)
      }
    }
  }

  @Test
  @DisplayName("返回数组类型列表")
  fun getJsonArray() {
    val s = studentDAO.findFavorites()
    assertEquals(s.size, 1)
    println(s)
  }

  @Test
  @DisplayName("返回对象列表")
  fun getJsonObject() {
    val s = studentDAO.findDetail()
    assertEquals(s.size, 1)
    println(s)
  }

  @Test
  @DisplayName("返回数组对象列表")
  fun getJsonObjectArrayList() {
    val s = studentDAO.findEducation()
    assertEquals(s.size, 1)
    assert(s.any { it != null && it.isNotEmpty() })
    s.forEach {
      it?.forEach {
        assert(it is EducationInfo)
      }
    }
    println(s)
  }

  @Test
  @DisplayName("返回单个对象")
  fun getSingleObject() {
    val detail = studentDAO.findDetailById("a")
    println(detail)
  }

}
