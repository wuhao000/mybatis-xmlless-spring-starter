package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.assertNotNull

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDAOTest : BaseTest() {

  val deleteId = "061251173"
  val id = "061251170"
  @Autowired
  private lateinit var studentDAO: StudentDAO

  /**
   * 测试统计全部
   */
  @Test
  fun count() {
    assert(studentDAO.count() > 0)
  }

  /**
   * 测试单条删除
   */
  @Test
  fun deleteById() {
    if (!studentDAO.existsById(deleteId)) {
      studentDAO.save(Student(
          deleteId,
          "wuhao",
          "18005184916", 1
      ))
    }
    assert(studentDAO.existsById(deleteId))
    studentDAO.deleteById(deleteId)
    assert(!studentDAO.existsById(deleteId))
  }

  /**
   * 测试条件删除
   */
  @Test
  fun deleteByName() {
    val id = "testDeleteByName"
    val name = "nameOfTestDeleteByName"
    if (!studentDAO.existsById(id)) {
      studentDAO.save(
          Student(id, name, "18005184918", 1)
      )
    }
    assert(studentDAO.existsByName(name))
    studentDAO.deleteByName(name)
    assert(!studentDAO.existsByName(name))
  }

  /**
   * 测试exists
   */
  @Test
  fun existsById() {
    assert(!studentDAO.existsById("1234"))
    assert(studentDAO.existsById(id))
  }

  /**
   * 测试查询全部
   */
  @Test
  fun findAll() {
    val list = studentDAO.findAll()
    val spec = list.first { it.id == id }
    assert(spec.scores != null && spec.scores!!.isNotEmpty())
    assert(list.isNotEmpty())
  }

  /**
   * 测试分页查询
   */
  @Test
  fun findAllPageable() {
    studentDAO.findAllPageable(
        PageRequest.of(0, 20, Sort.Direction.DESC, "name")).apply {
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
        PageRequest.of(0, 20, Sort.by("name"))).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
  }

  /**
   * 测试指定值查询
   */
  @Test
  fun findByGraduatedEqTrue() {
    val list = studentDAO.findByGraduatedEqTrue()
    println(list)
  }

  /**
   * 测试根据主键获取
   */
  @Test
  fun findById() {
    if (!studentDAO.existsById(id)) {
      studentDAO.save(Student(
          id, "wuhao", "18005184916", 1
      ))
    }
    val student = studentDAO.findById(id)
    assertNotNull(student)
    println(student.count)
    student.scores?.forEach {
      println(it.subject)
      println(it.subject?.name + "/" + it.score)
    }
  }

  /**
   * 测试匹配字符串前缀
   */
  @Test
  fun findByPhoneNumberLikeLeft() {
    assert(studentDAO.findByPhoneNumberLikeLeft("180").isNotEmpty())
    assert(studentDAO.findByPhoneNumberLikeLeft("4916").isEmpty())
  }

  /**
   * 测试匹配字符串后缀
   */
  @Test
  fun findByPhoneNumberLikeRight() {
    assert(studentDAO.findByPhoneNumberLikeRight("4916").isNotEmpty())
    assert(studentDAO.findByPhoneNumberLikeRight("180").isEmpty())
  }

  /**
   * 测试根据关联表中的字段查询
   */
  @Test
  fun findBySubjectId() {
    val students = studentDAO.findBySubjectId(1)
    println(students)
    assert(students.isNotEmpty())
  }

  /**
   * 测试分页带条件查询
   */
  @Test
  fun findPage() {
    val page = studentDAO.findAllPage(
        null, null,
        PageRequest.of(0, 20))
    println(page.content.size)
    println(page.totalElements)
  }

  /**
   * 测试单条插入
   */
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

  /**
   * 测试批量插入及批量删除
   */
  @Test
  fun saveAllAndDeleteAll() {
    val id1 = "saveAll1"
    val id2 = "saveAll2"
    if (studentDAO.existsById(id1)) {
      studentDAO.deleteById(id1)
    }
    if (studentDAO.existsById(id2)) {
      studentDAO.deleteById(id2)
    }
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

  /**
   * 测试更新
   */
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

  /**
   * 测试更新单个属性
   */
  @Test
  fun updateNameById() {
    val id = "testUpdateNameById"
    val oldName = "oldName"
    val newName = "newName"
    if (studentDAO.existsById(id)) {
      studentDAO.deleteById(id)
    }
    studentDAO.save(
        Student(
            id,
            oldName,
            "18005184916", 1
        )
    )
    println(studentDAO.findById(id))
    assert(studentDAO.findById(id)?.name == oldName)
    assert(studentDAO.updateNameById(newName, id) == 1)
    assert(studentDAO.findById(id)?.name == newName)
    studentDAO.deleteById(id)
  }

}
