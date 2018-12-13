package com.aegis.mybatis.xmlless.annotations

import org.apache.ibatis.type.TypeHandler
import kotlin.reflect.KClass

/**
 *
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Target(AnnotationTarget.FIELD)
annotation class Handler(
    val value: KClass<out TypeHandler<*>>
)
