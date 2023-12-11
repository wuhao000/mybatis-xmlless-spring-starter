package com.aegis.mybatis.xmlless.annotations


/**
 * 注解在mapper方法上表示该方法需要获取的对象属性名称
 * 注解在对象类上表示默认获取的对象属性名称
 *
 * @author 吴昊
 * @since 0.0.3
 */
@Target(
    allowedTargets = [
      AnnotationTarget.ANNOTATION_CLASS
    ]
)
annotation class PropertyMapping(
    /** 查询方法返回对象中的字段名称 */
    val property: String,
    /** 方法返回对应中属性对应的sql字段查询表达式，例如: sum(age), 其中age为对象属性名称（非数据库表字段名称） */
    val value: String
)
