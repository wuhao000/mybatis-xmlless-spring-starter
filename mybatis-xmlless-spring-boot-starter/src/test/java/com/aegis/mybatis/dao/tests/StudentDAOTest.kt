package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.bean.StudentState
import com.aegis.mybatis.dao.ScoreDAO
import com.aegis.mybatis.dao.StudentDAO
import org.junit.jupiter.api.Test
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
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
  private lateinit var dao: StudentDAO
  @Autowired
  private lateinit var scoreDAO: ScoreDAO

  /**
   * 测试统计全部
   */
  @Test
  fun count() {
    insertStudents()
    assert(dao.count() > 0)
  }

  /**
   * 测试单条删除
   */
  @Test
  fun deleteById() {
    if (!dao.existsById(deleteId)) {
      dao.save(
          Student(
              deleteId,
              "wuhao",
              "18005184916", 1
          )
      )
    }
    assert(dao.existsById(deleteId))
    dao.deleteById(deleteId)
    assert(!dao.existsById(deleteId))
  }

  /**
   * 测试条件删除
   */
  @Test
  fun deleteByName() {
    val id = "testDeleteByName"
    val name = "张三"
    if (!dao.existsById(id)) {
      dao.save(
          Student(id, name, "18005184918", 1)
      )
    }
    assert(dao.existsByName(name))
    dao.deleteByName(name)
    assert(!dao.existsByName(name))
  }

  /**
   * 测试exists
   */
  @Test
  fun existsById() {
    insertStudents()
    assert(!dao.existsById("4321"))
    assert(dao.existsById(id))
  }

  /**
   * 测试查询全部
   */
  @Test
  fun findAll() {
    if (!dao.existsById(id)) {
      dao.save(Student(id, "王五", mobile, 22))
    }
    val score = Score(80, id, 1)
    scoreDAO.save(score)
    val list = dao.findAll()
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
    dao.findAllPageable(
        PageRequest.of(0, 20)
    ).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    dao.findAllPageable(
        PageRequest.of(0, 20, Sort(Sort.Direction.DESC, "name"))
    ).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
    dao.findAllPageable(
        PageRequest.of(0, 20, Sort.by("name"))
    ).apply {
      this.content.map {
        it.name + " / ${it.id}"
      }.forEach { println(it) }
      println(this.content.first().name.compareTo(this.content.last().name))
    }
  }

  @Test
  fun findByAgeBetween() {
    dao.saveAll(
        listOf(
            Student().apply {
              id = "a"
              age = 20
              birthday = LocalDate.of(2020, 11, 3)
            },
            Student().apply {
              id = "b"
              age = 21
              birthday = LocalDate.of(2020, 11, 3)
            },
            Student().apply {
              id = "c"
              age = 16
              birthday = LocalDate.of(2020, 11, 3)
            },
            Student().apply {
              id = "D"
              age = 24
              birthday = LocalDate.of(2020, 11, 3)
            }
        )
    )
    val list = dao.findByAgeBetweenMinAndMax(18, 22)
    assertEquals(2, list.size)
  }

  @Test
  fun findByAgeGte() {
    dao.saveAll(
        listOf(
            Student().apply {
              id = "a"
              age = 20
              birthday = LocalDate.of(2020, 11, 3)
            },
            Student().apply {
              id = "b"
              age = 21
              birthday = LocalDate.of(2020, 11, 3)
            },
            Student().apply {
              id = "c"
              age = 16
              birthday = LocalDate.of(2020, 11, 3)
            }
        )
    )
    val list = dao.findByAgeGte(18)
    assertEquals(2, list.size)
  }

  @Test
  fun findByBirthday() {
    dao.save(
        Student().apply {
          id = "abc"
          birthday = LocalDate.of(2020, 11, 3)
        }
    )
    val students = dao.findByBirthday(
        LocalDate.of(2020, 11, 3)
    )
    assertEquals(1, students.size)
  }

  @Test
  fun findByAge() {
    dao.findByAge(12, "a")
  }

  @Test
  fun findByCreateTimeBetween() {
    dao.saveAll(
        listOf(
            Student().apply {
              id = "a"
              age = 20
              createTime = LocalDateTime.of(2021, 1, 3, 12, 0, 0)
            },
            Student().apply {
              id = "b"
              age = 21
              createTime = LocalDateTime.of(2021, 1, 4, 12, 0, 0)
            },
            Student().apply {
              id = "c"
              age = 16
              createTime = LocalDateTime.of(2021, 1, 5, 12, 0, 0)
            }
        )
    )
    val c1 = dao.findByCreateTimeBetweenStartTimeAndEndTime(LocalDateTime.of(2021,1,3,0,0,0),
        LocalDateTime.of(2021,1,4,13,0,0))
    val c2 = dao.findByCreateTimeBetweenStartTimeAndEndTime(LocalDateTime.of(2021,1,4,0,0,0), null)
    val c3 = dao.findByCreateTimeBetweenStartTimeAndEndTime(null, LocalDateTime.of(2021,1,4,0,0,0))
    val c4 = dao.findByCreateTimeBetweenStartTimeAndEndTime(null, null)
    println(c1.size)
    println(c2.size)
    println(c3.size)
    println(c4.size)
  }

  /**
   * 测试指定值查询
   */
  @Test
  fun findByGraduatedEqTrue() {
    val list = dao.findByGraduatedEqTrue()
    println(list)
  }

  /**
   * 测试根据主键获取
   */
  @Test
  fun findById() {
    if (!dao.existsById(id)) {
      dao.save(
          Student(
              id, "wuhao", "18005184916", 1
          )
      )
    }
    val student = dao.findById(id)
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
    assert(dao.findByPhoneNumberLikeLeft(mobile.substring(0, 4)).isNotEmpty())
    assert(dao.findByPhoneNumberLikeLeft(mobile.substring(2, 6)).isEmpty())
    assert(dao.findByPhoneNumberLikeRight(mobile.substring(mobile.length - 6, mobile.length)).isNotEmpty())
    assert(dao.findByPhoneNumberLikeRight(mobile.substring(2, 6)).isEmpty())
  }

  /**
   * 测试匹配字符串后缀
   */
  @Test
  fun findByPhoneNumberLikeRight() {
    dao.save(
        Student("1", "李四", "17705184916", 22)
    )
    assert(dao.findByPhoneNumberLikeRight("4916").isNotEmpty())
    assert(dao.findByPhoneNumberLikeRight("180").isEmpty())
  }

  @Test
  fun findByState() {
    dao.saveAll(
        listOf(
            Student("a", "张三", "17705184916", 22, StudentState.abnormal),
            Student("b", "李四", "17705184916", 22)
        )
    )
    val list = dao.findByStateIn(listOf(StudentState.normal))
    assertEquals(1, list.size)
  }

  /**
   * 测试根据关联表中的字段查询
   */
  @Test
  fun findBySubjectId() {
    insertStudents()
    val students = dao.findBySubjectId(1)
    println(students)
    assert(students.isNotEmpty())
  }

  /**
   * 测试分页带条件查询
   */
  @Test
  fun findPage() {
    val page = dao.findAllPage(
        null, null,
        PageRequest.of(0, 20)
    )
    println(page.content.size)
    println(page.totalElements)
  }

  @Test
  fun getJsonObject() {
    dao.save(
        Student("1", "李四", "17705184916", 22).apply {
          detail = StudentDetail(170)
        }
    )
    val s = dao.findDetail()
    assertEquals(s.size, 1)
    println(s)
  }

  /**
   * 测试单条插入
   */
  @Test
  fun save() {
    dao.deleteById(id)
    assert(!dao.existsById(id))
    dao.save(Student(
        id,
        "wuhao",
        "18005184916", 1
    ).apply {
      detail = StudentDetail(172)
      favorites = listOf("旅游", "登山")
    })
    val bean = dao.findById(id)
    assertNotNull(bean?.detail)
    assertEquals(bean?.detail?.height, 172)
    assert(dao.existsById(id))
    println(bean?.favorites)
  }

  /**
   * 测试批量插入及批量删除
   */
  @Test
  fun saveAllAndDeleteAll() {
    val id1 = "saveAll1"
    val id2 = "saveAll2"
    if (dao.existsById(id1)) {
      dao.deleteById(id1)
    }
    if (dao.existsById(id2)) {
      dao.deleteById(id2)
    }
    dao.saveAll(
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
    assert(dao.existsById(id1))
    assert(dao.existsById(id2))
    dao.deleteByIds(listOf("saveAll1", "saveAll2"))
    assert(!dao.existsById(id1))
    assert(!dao.existsById(id2))
  }

  /**
   * 测试新增或更新
   */
  @Test
  fun saveOrUpdate() {
    dao.saveOrUpdate(Student(
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
    dao.save(student1)
    dao.saveOrUpdateAll(listOf(student2, student3))
    val student4 = dao.findById(id)
    assertNotNull(student4)
    assertEquals(student1.name, student4.name)
    println(student4.email)
    val student5 = dao.findById(student3.id)
    assertNotNull(student5)
  }

  /**
   * 测试更新
   */
  @Test
  fun update() {
    insertStudents()
    assert(
        dao.update(
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
    if (dao.existsById(id)) {
      dao.deleteById(id)
    }
    dao.save(
        Student(
            id,
            oldName,
            "18005184916", 1
        )
    )
    println(dao.findById(id))
    assert(dao.findById(id)?.name == oldName)
    assert(dao.updateNameById(newName, id) == 1)
    assert(dao.findById(id)?.name == newName)
    dao.deleteById(id)
  }

  private fun insertStudents() {
    dao.saveAll(
        (0..10).map {
          Student("${it}00$it", "$it", "$it", 1)
        }
    )
    dao.save(
        Student(id, name, mobile, 1)
    )
    val score = Score(80, id, 1)
    scoreDAO.save(score)
  }

}
