package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.baomidou.mybatisplus.core.toolkit.StringPool
import org.apache.ibatis.mapping.ResultFlag
import org.apache.ibatis.mapping.ResultMap
import org.apache.ibatis.mapping.ResultMapping
import org.apache.ibatis.session.Configuration
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

  fun resolveResultMap(configuration: Configuration): ResultMap {
    val mapperClass = this.query!!.mapperClass
    return ResultMap.Builder(
        configuration,
        mapperClass.name + StringPool.DOT + function.name,
        this.query.mappings.modelClass,
        this.query.mappings.mappings.map {
          val builder = ResultMapping.Builder(
              configuration,
              it.property
          )
          if (it.property == this.query.mappings.tableInfo.keyProperty) {
            builder.flags(listOf(ResultFlag.ID))
          }
          if (it.joinInfo != null) {
            if (it.joinInfo.joinPropertyType == JoinPropertyType.SingleProperty) {
              builder.column(it.joinInfo.selectColumns.first())
            } else if (it.joinInfo.joinPropertyType == JoinPropertyType.Object) {
              if (!it.joinInfo.associationPrefix.isNullOrBlank()) {
                builder.columnPrefix(it.joinInfo.associationPrefix)
              }
              builder.javaType(it.joinInfo.javaType)
            }
          } else {
            builder.column(it.column)
          }
          builder.columnPrefix("score_")
              .nestedResultMapId("")
          builder.build()
        },
        true
    ).build()
  }

  fun type(): QueryType? {
    return query?.type
  }

}
