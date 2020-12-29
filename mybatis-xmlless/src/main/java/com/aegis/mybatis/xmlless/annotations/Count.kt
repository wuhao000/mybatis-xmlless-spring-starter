package com.aegis.mybatis.xmlless.annotations

import javax.persistence.criteria.JoinType


/**
 *
 * Created by 吴昊 on 2018/12/18.
 *
 * @author 吴昊
 * @since 0.0.9
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD,
  AnnotationTarget.ANNOTATION_CLASS
])
@ModifyIgnore
annotation class Count(
    val targetTable: String,
    val joinProperty: String = "",
    val targetColumn: String,
    val countColumn: String,
    val joinType: JoinType = JoinType.LEFT
)
