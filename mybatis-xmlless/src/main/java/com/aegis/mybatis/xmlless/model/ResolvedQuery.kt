package com.aegis.mybatis.xmlless.model

import java.lang.reflect.Method


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class ResolvedQuery(
    val query: Query? = null,
    val resultMap: String?,
    /**  sql查询返回的java类型 */
    val returnType: Class<*>?,
    /** 待解析的方法 */
    val method: Method,
    val unresolvedReason: String? = null
) {

  /**  sql语句 */
  val sql: String? = query?.toSql()
  /**  sql类型 */
  val type = query?.type

  override fun toString(): String {
    val sb = StringBuilder()
    sb.append("\t ${if (isValid()) {
      "已解析"
    } else {
      "未成功解析"
    }} 方法:\t$method\n")
    if (isValid()) {
      val prefix = "\t\t- "
      sb.append(prefix).append("类型: $type\n")
      sb.append(prefix).append("SQL: \n${sql!!.trim().lines().joinToString("\n") { "\t".repeat(5) + it }}\n")
      sb.append(prefix).append("返回: $returnType")
    } else {
      sb.append("\t\t - $unresolvedReason")
    }
    sb.append("\n\n")
    return sb.toString()
  }

  private fun isValid(): Boolean {
    return query != null && unresolvedReason == null
  }

}
