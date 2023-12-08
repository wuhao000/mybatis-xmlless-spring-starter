package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType


/**
 * 多表连接信息
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
annotation class JoinObject(
    val toEntity: JoinEntity = JoinEntity(Any::class),
    val toTable: JoinTable = JoinTable("", ""),
    val joinType: JoinType = JoinType.LEFT,
    /**  当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinOnProperty: String = ""
)
