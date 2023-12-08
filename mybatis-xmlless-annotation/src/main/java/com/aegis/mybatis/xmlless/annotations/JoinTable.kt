package com.aegis.mybatis.xmlless.annotations


/**
 * Created by 吴昊 on 2023/12/8.
 */
@Target(
    allowedTargets = [
      AnnotationTarget.ANNOTATION_CLASS
    ]
)
annotation class JoinTable(
    /** 要连接的表名 */
    val targetTable: String,
    /** 映射到注解所在实体类属性的数据库表字段名称 */
    val columnMapTo: String = "",
    /** 用于连接表的条件字段名称 */
    val joinOnColumn: String = "id",
)
