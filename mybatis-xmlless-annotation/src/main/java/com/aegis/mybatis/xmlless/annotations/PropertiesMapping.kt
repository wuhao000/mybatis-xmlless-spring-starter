package com.aegis.mybatis.xmlless.annotations


/**
 * 注解在mapper方法上表示该方法需要获取的对象属性名称
 * 注解在对象类上表示默认获取的对象属性名称
 *
 * @author 吴昊
 * @since 0.0.3
 * @param properties 需要获取的对象属性名称
 */
@Target(
    allowedTargets = [
      AnnotationTarget.FUNCTION
    ]
)
annotation class PropertiesMapping(
    val value: Array<PropertyMapping> = []
)
