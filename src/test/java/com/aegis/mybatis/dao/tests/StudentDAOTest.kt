package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.dao.ScoreDAO
import com.aegis.mybatis.dao.StudentDAO
import org.junit.Test
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class StudentDAOTest : BaseTest() {

  val deleteId = "061251173"
  val id = "061251170"
  val mobile = "17705184916"
  val name = "张三"

  @Autowired
  private lateinit var scoreDAO: ScoreDAO

  @Autowired
  private lateinit var studentDAO: StudentDAO

  /**
   * 测试统计全部
   */
  @Test
  fun count() {
    insertStudents()
    assert(studentDAO.count() > 0)
  }

  /**
   * 测试单条删除
   */
  @Test
  fun deleteById() {
    if (!studentDAO.existsById(deleteId)) {
      studentDAO.save(
          Student(
              deleteId,
              "wuhao",
              "18005184916", 1
          )
      )
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
    val name = "张三"
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
    insertStudents()
    assert(!studentDAO.existsById("4321"))
    assert(studentDAO.existsById(id))
  }

  /**
   * 测试查询全部
   */
  @Test
  fun findAll() {
    if (!studentDAO.existsById(id)) {
      studentDAO.save(Student(id, "王五", mobile, 22))
    }
    val score = Score(80, id, 1)
    scoreDAO.save(score)
    val list = studentDAO.findAll()
    assert(scoreDAO.findByStudentId(id).isNotEmpty())
    val spec = list.first { it.id == id }
    assert(spec.scores != null && spec.scores!!.isNotEmpty())
    assert(list.isNotEmpty())
  }

  /**
   * 测试分页查询
   */
  @Test
  fun findAllPageable() {
    insertStudents()
    studentDAO.findAllPageable(
        PageRequest.of(0, 20)
    ).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    studentDAO.findAllPageable(
        PageRequest.of(0, 20, Sort(Sort.Direction.DESC, "name"))
    ).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    studentDAO.findAllPageable(
        PageRequest.of(0, 20, Sort.by("name"))
    ).apply {
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
      studentDAO.save(
          Student(
              id, "wuhao", "18005184916", 1
          )
      )
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
    insertStudents()
    assert(studentDAO.findByPhoneNumberLikeLeft(mobile.substring(0, 4)).isNotEmpty())
    assert(studentDAO.findByPhoneNumberLikeLeft(mobile.substring(2, 6)).isEmpty())
    assert(studentDAO.findByPhoneNumberLikeRight(mobile.substring(mobile.length - 6, mobile.length)).isNotEmpty())
    assert(studentDAO.findByPhoneNumberLikeRight(mobile.substring(2, 6)).isEmpty())
  }

  /**
   * 测试匹配字符串后缀
   */
  @Test
  fun findByPhoneNumberLikeRight() {
    studentDAO.save(
        Student("1", "李四", "17705184916", 22)
    )
    assert(studentDAO.findByPhoneNumberLikeRight("4916").isNotEmpty())
    assert(studentDAO.findByPhoneNumberLikeRight("180").isEmpty())
  }

  /**
   * 测试根据关联表中的字段查询
   */
  @Test
  fun findBySubjectId() {
    insertStudents()
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
        PageRequest.of(0, 20)
    )
    println(page.content.size)
    println(page.totalElements)
  }

  @Test
  fun getJsonObject() {
    val s = studentDAO.findDetail()
    assertEquals(s.size, 1)
    println(s)
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
    ).apply {
      detail = StudentDetail(172)
      favorites = listOf("旅游", "登山")
    })
    val bean = studentDAO.findById(id)
    assertNotNull(bean?.detail)
    assertEquals(bean?.detail?.height, 172)
    assert(studentDAO.existsById(id))
    println(bean?.favorites)
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
            Student(
                id1,
                "zs", "123", 1
            ),
            Student(
                id2,
                "zs", "123", 1
            )
        )
    )
    assert(studentDAO.existsById(id1))
    assert(studentDAO.existsById(id2))
    studentDAO.deleteByIds(listOf("saveAll1", "saveAll2"))
    assert(!studentDAO.existsById(id1))
    assert(!studentDAO.existsById(id2))
  }

  /**
   * 测试新增或更新
   */
  @Test
  fun saveOrUpdate() {
    studentDAO.saveOrUpdate(Student(
        id,
        "wuhao",
        "18005184916", 1
    ).apply {
      detail = StudentDetail(172)
      favorites = listOf("旅游", "登山")
    })
  }

  /**
   * 测试新增或更新
   */
  @Test
  fun saveOrUpdateAll() {
    val student1 = Student(
        id, "李四", "18012345678", 1
    ).apply {
      email = "aaa@a.com"
      detail = StudentDetail(172)
      favorites = listOf("旅游", "登山")
    }
    val student3 = Student(
        "aaa", "王五", "18055555555", 2
    )
    val student2 = Student()
    BeanUtils.copyProperties(student1, student2)
    student2.name = "张三"
    student2.email = null
    studentDAO.save(student1)
    studentDAO.saveOrUpdateAll(listOf(student2, student3))
    val student4 = studentDAO.findById(id)
    assertNotNull(student4)
    assertEquals(student2.name, student4.name)
    assertEquals(student2.name, student4.name)
    println(student4.email)
    val student5 = studentDAO.findById(student3.id)
    assertNotNull(student5)
  }

  /**
   * 测试更新
   */
  @Test
  fun update() {
    insertStudents()
    assert(
        studentDAO.update(
            Student(
                id, "zhangsan",
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

  private fun insertStudents() {
    studentDAO.saveAll(
        (0..10).map {
          Student("${it}00$it", "$it", "$it", 1)
        }
    )
    studentDAO.save(
        Student(id, name, mobile, 1)
    )
    val score = Score(80, id, 1)
    scoreDAO.save(score)
  }

}
