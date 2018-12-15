package com.aegis.mybatis.xmlless.annotations

import javax.persistence.criteria.JoinType

/**
 * 用在持久化类的属性上，表示持久化类的属性是一个关联属性
 * @param selectColumn 关联属性对应的连接表中的字段
 * @param targetTable 连接的表名称
 * @param joinType 连接的类型
 * @param joinProperty 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键
 * @param targetColumn 连接表用于连接的表字段名称
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
annotation class JoinProperty(
    val selectColumn: String,
    val targetTable: String,
    val joinType: JoinType = JoinType.LEFT,
    val joinProperty: String = "",
    val targetColumn: String
)
