package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.BaseStudentDAO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class BaseStudentDAOTest : BaseTest() {

  val deleteId = "061251173"
  val id = "061251170"
  val mobile = "17705184916"
  val name = "张三"


  @Autowired
  private lateinit var studentDAO: BaseStudentDAO

  @Test
  fun selectById() {
    if (studentDAO.selectById(id) == null) {
      studentDAO.insert(
          Student(
              id, "wuhao", "18005184916", 1
          )
      )
    }
    assertNotNull(studentDAO.selectById(id))
  }

}
