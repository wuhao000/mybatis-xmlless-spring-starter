package com.aegis

import org.apache.ibatis.jdbc.SQL
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
class SQLTest {

  @Test
  fun a() {
    U::class.memberProperties.forEach {
      println("${it.name} ${it.isConst} ${it.isFinal} ${it.isAccessible}")
    }
  }

  @Test
  fun sql() {
    val sql = object : SQL() {

      init {
        SELECT("id")
        SELECT("name")
        FROM("student")
        FROM("scores")
        LEFT_OUTER_JOIN("t_score s ON s.student_id = student.id")
        INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID")
        WHERE("P.ID = A.ID")
        WHERE("P.FIRST_NAME like ?")
        OR()
        WHERE("P.LAST_NAME like ?")
        GROUP_BY("P.ID")
        HAVING("P.LAST_NAME like ?")
        OR()
        HAVING("P.FIRST_NAME like ?")
        ORDER_BY("P.ID")
        ORDER_BY("P.FULL_NAME")
      }

    }.toString()
    println(sql)
  }

  class U {

    var n: String? = null
    val s: Int = 0
    val a: String
      get() {
        return "ab"
      }

  }

}
