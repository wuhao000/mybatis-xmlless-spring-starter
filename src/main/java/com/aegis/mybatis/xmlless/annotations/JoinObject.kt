package com.aegis.mybatis.xmlless.annotations

import com.aegis.mybatis.xmlless.enums.JoinType


/**
 * 多表连接信息
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
annotation class JoinObject(
    /**  链接表需要查询的字段 */
    val selectColumns: Array<String> = [],
    /**  连接的表名称 */
    val targetTable: String = "",
    val joinType: JoinType = JoinType.Left,
    /**  主表用于连接的字段名称 */
    val joinProperty: String = "",
    /**
     * 是否使用主键连接，为true时，当joinColumn为空，使用主表的主键，当targetColumn为空，使用连接表的主键
     * 为false时，joinColumn和targetColumn必须不为空
     */
//    val joinOnKeyColumn: Boolean = true,
    /**  连接表用于连接的字段 */
    val targetColumn: String = "",
    val associationPrefix: String = ""
)

