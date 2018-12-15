package com.aegis.mybatis.xmlless.annotations


/**
 * 用在持久化类的属性上，表示该属性不参与数据库的select查询
 * @author 吴昊
 * @since 0.0.1
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
annotation class SelectIgnore
