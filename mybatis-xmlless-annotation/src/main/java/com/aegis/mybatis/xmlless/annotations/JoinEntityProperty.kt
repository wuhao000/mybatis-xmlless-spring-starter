package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType
import kotlin.reflect.KClass

/**
 * 用在持久化类的属性上，表示持久化类的属性是一个关联属性
 * @param joinType 连接的类型
 * @param joinOnThisProperty 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键
 */
@Target(
    allowedTargets = [
      AnnotationTarget.FIELD,
    ]
)
annotation class JoinEntityProperty(
    /** 要连接的表对应的实体类 */
    val entity: KClass<*>,
    /** 要连接的表对应的实体类字段名称，用于映射到当前注解所在字段 */
    val propertyMapTo: String = "",
    /** 要连接的表对应的实体类用于连接条件的属性名称, 默认为主键 */
    val joinOnProperty: String = "",
    val joinType: JoinType = JoinType.LEFT,
    /** 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinOnThisProperty: String
)
