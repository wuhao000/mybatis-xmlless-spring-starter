package com.aegis.mybatis.xmlless.annotations


/**
 * 用于mapper查询方法给查询条件的属性赋值
 *
 * 例如：findById方法添加的@ResolvedName注解中添加values=[@ValueAssign(param="id",nonStringValue="12")]
 *  表示查询条件中 id = 12
 */
annotation class ValueAssign(
    val param: String,
    val stringValue: String = "",
    val nonStringValue: String = ""
)
