package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType
import kotlin.reflect.KClass

/**
 * 用在持久化类的属性上，表示持久化类的属性是一个关联属性
 * @param selectColumn 关联属性对应的连接表中的字段
 * @param selectProperty 关联属性对应的连接表所属实体类的属性
 * @param targetTable 连接的表名称
 * @param targetEntity 连接的表对应的实体类
 * @param joinType 连接的类型
 * @param joinProperty 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键
 * @param targetProperty 连接表对应实体类用于连接的属性名称
 * @param targetColumn 连接表用于连接的表字段名称
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
@MyBatisIgnore(insert = true, update = true)
annotation class JoinProperty(
    val selectColumn: String = "",
    val targetTable: String = "",
    val targetEntity: KClass<*> = Any::class,
    val joinType: JoinType = JoinType.LEFT,
    val joinProperty: String = "",
    val joinColumn: String = "",
    val targetColumn: String,
    val selectProperty: String = "",
    val targetProperty: String = ""
)
