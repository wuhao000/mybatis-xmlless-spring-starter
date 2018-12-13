package com.aegis.mybatis.xmlless.annotations

import java.lang.annotation.Inherited


/**
 * 忽略规则
 * @author 吴昊
 * @since 0.0.1
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
@Inherited
annotation class UpdateIgnore
