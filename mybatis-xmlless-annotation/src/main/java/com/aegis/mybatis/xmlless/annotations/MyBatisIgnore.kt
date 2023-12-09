package com.aegis.mybatis.xmlless.annotations

/**
 * Created by 吴昊 on 2023/12/6.
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
@Deprecated("use @Column(insertable = false, updatable = false) " +
    "or @JoinColumn(insertable = false, updatable = false) or @Transient instead")
annotation class MyBatisIgnore(
    @Deprecated("use @Column(insertable = false) or @JoinColumn(insertable = false) instead")
    val insert: Boolean = false,
    @Deprecated("use @Column(updatable = false) or @JoinColumn(updatable = false) instead")
    val update: Boolean = false,
    @Deprecated("use @Transient instead")
    val select: Boolean = false
)
