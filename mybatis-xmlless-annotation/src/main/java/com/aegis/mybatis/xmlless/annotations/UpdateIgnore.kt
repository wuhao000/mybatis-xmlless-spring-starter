package com.aegis.mybatis.xmlless.annotations

import java.lang.annotation.Inherited


/**
 * 用在持久化类的属性上，表示该属性不参与update操作
 * @author 吴昊
 * @since 0.0.1
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
@Inherited
annotation class UpdateIgnore
