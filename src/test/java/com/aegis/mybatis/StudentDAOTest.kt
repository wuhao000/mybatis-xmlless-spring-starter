package com.aegis.mybatis

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
  fun deleteByName() {
    val id = "testDeleteByName"
    val name = "nameOfTestDeleteByName"
    studentDAO.save(
        Student(
            id,
            name,
            "18005184916", 1
        )
    )
    assert(studentDAO.existsByName(name))
    studentDAO.deleteByName(name)
    assert(!studentDAO.existsByName(name))
  }

  @Test
  fun existsByClientId() {
    val id = "1234"
    assert(!studentDAO.existsById(id))
  }

  @Test
  fun findAll() {
    val list = studentDAO.findAll()
    val spec = list.first { it.id == id }
    assert(spec.scores != null && spec.scores!!.isNotEmpty())
    assert(list.isNotEmpty())
  }

  @Test
  fun findById() {
    val student = studentDAO.findById(id)
    println(student?.scores)
    assert(studentDAO.findById(id) != null)
  }

  @Test
  fun findPage() {
    studentDAO.findAllPageable(
        PageRequest.of(0, 20)).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    studentDAO.findAllPageable(
        PageRequest.of(0, 20, Sort(Sort.Direction.DESC, "name"))).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    studentDAO.findAllPageable(
        PageRequest.of(0, 20, Sort("name"))).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
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
    studentDAO.deleteByIds(listOf("saveAll1", "saveAll2"))
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

  @Test
  fun updateNameById() {
    val id = "testUpdateNameById"
    val oldName = "oldName"
    val newName = "newName"
    studentDAO.save(
        Student(
            id,
            oldName,
            "18005184916", 1
        )
    )
    assert(studentDAO.findById(id)?.name == oldName)
    assert(studentDAO.updateNameById(newName, id) == 1)
    assert(studentDAO.findById(id)?.name == newName)
    studentDAO.deleteById(id)
  }

}
