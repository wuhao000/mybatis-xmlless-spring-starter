package com.aegis.mybatis.xmlless.annotations

/**
 * Created by 吴昊 on 2023/12/6.
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
annotation class MyBatisIgnore(
    val insert: Boolean = false,
    val update: Boolean = false,
    val select: Boolean = false
)
