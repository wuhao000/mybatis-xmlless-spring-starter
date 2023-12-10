package com.aegis.mybatis.xmlless.annotations

import jakarta.persistence.criteria.JoinType
import kotlin.reflect.KClass


/**
 * 多表连接信息
 */
@Target(
    allowedTargets = [
      AnnotationTarget.FIELD
    ]
)
annotation class JoinObject(
    /** 要连接的表对应的实体类 */
    val entity: KClass<*>,
    /** 要连接的表对应的实体类用于连接条件的属性名称 */
    val joinOnProperty: String = "id",
    /**
     * 连接类型
     */
    val joinType: JoinType = JoinType.LEFT,
    /**  当前对象用于连接的属性名称（非表字段名称），如果为空则默认为主键 */
    val joinOnThisProperty: String = ""
)
