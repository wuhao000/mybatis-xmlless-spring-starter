package com.aegis.mybatis.xmlless.annotations


/**
 * 注解在持久化类的属性上，表明该属性需要进行类型转换
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Target(AnnotationTarget.FUNCTION)
annotation class JsonResult(
    /**
     * 当且仅当满足以下所以条件时需要将此参数设置为true
     * 1. 方法返回值为集合
     * 2. 查询结果为单一结果
     * 3. 查询单个字段
     * 4. 该字段是集合或数组映射为json
     */
    val forceSingleValue: Boolean = false
)
