package com.aegis.mybatis.xmlless.annotations.criteria

import com.aegis.mybatis.xmlless.enums.Operations

/**
 *
 * @author 吴昊
 * @date 2023/12/11 20:20
 * @since v4.0.0
 * @version 1.0
 *
 * @param value 条件比较的类型
 * @param properties 条件比较的持久化对象属性（一项或多项）,如果为空则默认和注解所在字段或参数名称相同
 */
@Target(
    allowedTargets = [
      AnnotationTarget.FIELD
    ]
)
@Repeatable
annotation class Criteria(
    val value: Operations = Operations.Eq,
    val properties: Array<String> = []
)
