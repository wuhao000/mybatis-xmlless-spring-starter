package com.aegis.mybatis.xmlless.model

import kotlin.reflect.KFunction


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
    val function: KFunction<*>,
    var unresolvedReasons: MutableList<String> = arrayListOf()) {

  /**  sql语句 */
  var sql: String?

  init {
    val sqlResult = query?.toSql()
    sql = sqlResult?.sql
    unresolvedReasons.addAll(sqlResult?.reasons?.toMutableList() ?: listOf())
  }

  fun countSql(): String? {
    return query?.toCountSql()?.sql
  }

  fun isValid(): Boolean {
    return query != null && unresolvedReasons.isEmpty()
  }

  fun type(): QueryType? {
    return query?.type
  }

}
