package com.aegis

import org.apache.ibatis.jdbc.SQL
import org.junit.Test


/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
class SQLTest {

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

}
