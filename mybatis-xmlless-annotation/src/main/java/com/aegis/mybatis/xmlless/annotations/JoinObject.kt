package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType


/**
 * 多表连接信息
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
@ModifyIgnore
annotation class JoinObject(
    /**  链接表需要查询的字段 */
    val selectProperties: Array<String> = [],
    /**  连接的表名称 */
    val targetTable: String = "",
    val joinType: JoinType = JoinType.LEFT,
    /**  当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinProperty: String = "",
    /**  连接表用于连接的字段 */
    val targetColumn: String = "",
    val associationPrefix: String
)

