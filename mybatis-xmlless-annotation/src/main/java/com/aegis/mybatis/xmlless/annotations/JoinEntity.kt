package com.aegis.mybatis.xmlless.annotations

import kotlin.reflect.KClass

/**
 * Created by 吴昊 on 2023/12/8.
 */
@Target(
    allowedTargets = [
      AnnotationTarget.ANNOTATION_CLASS
    ]
)
annotation class JoinEntity(
    /** 要连接的表对应的实体类 */
    val targetEntity: KClass<*>,
    /** 要连接的表对应的实体类字段名称，用于映射到当前注解所在字段 */
    val propertyMapTo: String = "",
    /** 要连接的表对应的实体类用于连接条件的属性名称 */
    val joinOnProperty: String = "id"
)
