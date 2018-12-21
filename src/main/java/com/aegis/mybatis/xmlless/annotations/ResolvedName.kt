package com.aegis.mybatis.xmlless.annotations


/**
 * 用在mapper的方法上面，解析sql时不再使用方法名而使用此别名，这样方法名可以更具语义化
 * Created by 吴昊 on 2018/12/11.
 *
 * @author 吴昊
 * @since 0.0.2
 * @param name 解析使用的名称
 * @param partNames 为了防止条件过多导致name过长，将name中的条件分割成多个字符串，并使用And进行连接，即
 *        name="findBy", partNames=["nameLike","ageGte"] 的情况下最终解析的名称为findByNameLikeAndAgeGte
 * @param values 使用指定值作为解析出的条件中的查询值
 *    例如：findByNameEq, 如果values为 [@ValueAssign(param="name", stringValue="张三")]
 *     则最终sql中的查询条件为 name = '张三'
 */
@Target(allowedTargets = [
  AnnotationTarget.FUNCTION
])
annotation class ResolvedName(val name: String,
                              val partNames: Array<String> = [],
                              val values: Array<ValueAssign> = [],
                              val whereAppend: String = "",
                              val joinAppend: String = "")
