package com.aegis.mybatis

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit4.SpringRunner

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@RunWith(SpringRunner::class)
@SpringBootTest
class StudentDAOTest {

  val id = "061251170"
  @Autowired
  private lateinit var studentDAO: StudentDAO

  @Test
  fun count() {
    assert(studentDAO.count() > 0)
  }

  @Test
  fun delete() {
    val id = "061251171"
    studentDAO.save(Student(
        id,
        "wuhao",
        "18005184916", 1
    ))
    assert(studentDAO.existsById(id))
    studentDAO.deleteById(id)
    assert(!studentDAO.existsById(id))
  }

  @Test
  fun existsByClientId() {
    val id = "1234"
    assert(!studentDAO.existsById(id))
  }

  @Test
  fun findAll() {
    assert(studentDAO.findAll().isNotEmpty())
  }

  @Test
  fun findById() {
    println(studentDAO.findById(id))
    assert(studentDAO.findById(id) != null)
  }

  @Test
  fun findPage() {
    val page = studentDAO.findAllPageable(
        PageRequest.of(0, 20))
    println(page.content.first().name.compareTo(page.content.last().name))
  }

  @Test
  fun save() {
    studentDAO.deleteById(id)
    assert(!studentDAO.existsById(id))
    studentDAO.save(Student(
        id,
        "wuhao",
        "18005184916", 1
    ))
    assert(studentDAO.existsById(id))
  }

  @Test
  fun saveAll() {
    val id1 = "saveAll1"
    val id2 = "saveAll2"
    studentDAO.saveAll(
        listOf(
            Student(id1,
                "zs", "123", 1),
            Student(id2,
                "zs", "123", 1)
        )
    )
    assert(studentDAO.existsById(id1))
    assert(studentDAO.existsById(id2))
    studentDAO.deleteAllByIds(listOf("saveAll1", "saveAll2"))
    assert(!studentDAO.existsById(id1))
    assert(!studentDAO.existsById(id2))
  }

  @Test
  fun selectPage() {
    val page = studentDAO.findAllPage(PageRequest.of(0, 20))
    println(page.content.size)
    println(page.totalElements)
  }

  @Test
  fun update() {
    assert(
        studentDAO.update(
            Student(
                "061251170", "zhangsan",
                "17712345678",
                9
            )
        ) == 1
    )
  }

}
