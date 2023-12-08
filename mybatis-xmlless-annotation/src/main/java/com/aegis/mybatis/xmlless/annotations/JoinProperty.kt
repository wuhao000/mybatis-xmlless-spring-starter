package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType

/**
 * 用在持久化类的属性上，表示持久化类的属性是一个关联属性
 * @param joinType 连接的类型
 * @param joinOnThisProperty 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键
 */
@Target(
    allowedTargets = [
      AnnotationTarget.FIELD,
      AnnotationTarget.ANNOTATION_CLASS
    ]
)
annotation class JoinProperty(
    val toEntity: JoinEntity = JoinEntity(Any::class),
    val toTable: JoinTable = JoinTable("", ""),
    val joinType: JoinType = JoinType.LEFT,
    /** 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinOnThisProperty: String
)
