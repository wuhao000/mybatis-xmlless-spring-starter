package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.EducationInfo
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ResolvableType
import kotlin.test.assertEquals


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDetailDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: StudentDAO
  @Autowired
  private lateinit var studentDAO: StudentDetailDAO

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
  @DisplayName("数组值等于查询")
  fun findByFavorites() {
    val s = studentDAO.findByFavorites("登山")
    println(s)
  }

  @Test
  @DisplayName("数组值包含查询")
  fun findByFavoritesIn() {
    val s = studentDAO.findByFavoritesIn(listOf("登山"))
    println(s)
  }

  @Test
  fun findNickNamesById() {
    val s = studentDAO.findNickNamesById("a")
    println(s)
  }

  @Test
  @DisplayName("返回数组类型列表")
  fun getJsonArray() {
    dao.save(
        Student().apply {
          id = "aaa"
          favorites = listOf("登山", "电影")
        }
    )
    val s = studentDAO.findFavorites()
    assertEquals(1, s.size)
    println(s)
  }

  @Test
  @DisplayName("返回对象列表")
  fun getJsonObject() {
    dao.save(Student().apply {
      id = "aaa"
      detail = StudentDetail().apply {
        height = 180
      }
    })
    val s = studentDAO.findDetail()
    assertEquals(1, s.size)
    println(s)
  }

  @Test
  @DisplayName("返回数组对象列表")
  fun getJsonObjectArrayList() {
    dao.save(Student().apply {
      id = "abc"
      education = listOf(
          EducationInfo("xx大学")
      )
    })
    val s = studentDAO.findEducation()
    assertEquals(1, s.size)
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
