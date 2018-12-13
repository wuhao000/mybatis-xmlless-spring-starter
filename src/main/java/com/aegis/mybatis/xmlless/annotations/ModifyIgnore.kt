package com.aegis.mybatis.xmlless.annotations

/**
 * 被该注解修饰的字段将不用于数据库插入或更新
 * Created by 吴昊 on 2018/12/12.
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
@MustBeDocumented
@InsertIgnore
@UpdateIgnore
annotation class ModifyIgnore
