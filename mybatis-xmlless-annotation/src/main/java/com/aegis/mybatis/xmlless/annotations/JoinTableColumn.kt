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
    ]
)
annotation class JoinTableColumn(
    /** 要连接的表名 */
    val table: String,
    /** 映射到注解所在实体类属性的数据库表字段名称 */
    val columnMapTo: String = "",
    /** 用于连接表的条件字段名称 */
    val joinOnColumn: String = "id",
    val joinType: JoinType = JoinType.LEFT,
    /** 当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinOnThisProperty: String = ""
)
