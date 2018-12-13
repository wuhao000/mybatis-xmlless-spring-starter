package com.aegis.mybatis.xmlless.annotations


/**
 * 忽略规则
 * @author 吴昊
 * @since 0.0.1
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
annotation class SelectIgnore
