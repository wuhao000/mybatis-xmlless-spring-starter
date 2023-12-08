package com.aegis.mybatis.xmlless.annotations

/**
 * 注解在持久化类的属性上，表明该属性需要进行类型转换
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonMappingProperty
