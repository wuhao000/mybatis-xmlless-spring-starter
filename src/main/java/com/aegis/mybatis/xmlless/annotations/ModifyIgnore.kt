package com.aegis.mybatis.xmlless.annotations

/**
 * 被该注解修饰的字段不参与数据库插入或更新操作
 * Created by 吴昊 on 2018/12/12.
 * @author 吴昊
 * @since 0.0.1
 */
@Target(allowedTargets = [
  AnnotationTarget.FIELD
])
@MustBeDocumented
@InsertIgnore
@UpdateIgnore
annotation class ModifyIgnore
