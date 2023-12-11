package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.model.HiddenJoinInfo
import com.aegis.mybatis.xmlless.model.TableName
import com.aegis.mybatis.xmlless.resolver.bean.Student
import com.aegis.mybatis.xmlless.util.TypeUtil
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.baomidou.mybatisplus.core.metadata.TableResolver
import jakarta.persistence.criteria.JoinType
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExpressionResolverTest : BaseResolverTest<Student>(
    Student::class.java,
) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(ExpressionResolverTest::class.java)
  }

  @Test
  fun parseJoinExpression() {
    val exp = """LEFT JOIN Student s2
                     on date_format(t1.CJSJ, '%Y-%m-%d') = date_format(t2.gzrrq, '%Y-%m-%d')"""
    val result = ExpressionResolver.parseExpression(exp).toList().filter { it.isNotBlank() }
    var joinType: JoinType = JoinType.LEFT
    var joinTable = ""
    var alias = ""
    var beginOnExpression = false
    var beginRight = false
    var leftExp = ""
    var rightExp = ""
    result.forEachIndexed { index, s ->
      if (index == 0 && s.uppercase() in JoinType.entries.map { it.name }) {
        joinType = JoinType.valueOf(s.uppercase())
      } else if (index > 1 || s.uppercase() != "JOIN") {
        if (joinTable.isBlank()) {
          joinTable = s
        } else if (s.uppercase() == "ON") {
          beginOnExpression = true
        } else if (!beginOnExpression) {
          alias = s
        } else if (s == "=") {
          beginRight = true
        } else if (!beginRight) {
          leftExp += s
        } else {
          rightExp += s
        }
      }
    }
    TypeUtil.init("com.aegis.mybatis.**.bean", null)
    val type = TypeUtil.resolve<Any>(joinTable)
    println(type)
    val tableInfo = getTableInfo(type, builderAssistant)
    val tableName = TableName.resolve(TableResolver.getTableName(tableInfo), null)
    HiddenJoinInfo(
        tableName,
        joinType,
        "schoolId",
        "id",
        type
    )
    println(joinType)
    println(joinTable)
    println(alias)
    println(leftExp)
    println(rightExp)
  }

  @Test
  fun parseExpression() {
    val exp = "TRUNCATE(sum(t1.HHZSC)/sum(t1.HHZS), 0)"
    val result = ExpressionResolver.parseExpression(exp)
    println(result.toString())
    println(ExpressionResolver.parseExpression("date_format(t1.TJRQ, '%Y-%m-%d')").toString())
  }

}
