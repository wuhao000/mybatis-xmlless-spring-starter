package com.aegis.mybatis.xmlless.annotations

/**
 * Created by 吴昊 on 2018/12/12.
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
@MustBeDocumented
@InsertIgnore
@UpdateIgnore
annotation class ModifyIgnore
