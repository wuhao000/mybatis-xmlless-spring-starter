package com.aegis.mybatis.xmlless.annotations

import org.intellij.lang.annotations.Language

/**
 *
 * Created by 吴昊 on 2018/12/21.
 *
 * @author 吴昊
 * @since 1.4.3
 * @param expression 条件表达式（不包含if）
 * @param testExpression 条件生效的判断
 */
@Target(
    allowedTargets = [
      AnnotationTarget.VALUE_PARAMETER,
      AnnotationTarget.FIELD
    ]
)
@Repeatable
annotation class TestCriteria(
    /**
     * 条件表达式包裹的sql查询条件，例如：age gt 5 或 name like keywords, 和Mapper方法名称中的表达方式一致
     */
    @Language("GenericSQL")
    val expression: String,
    /**
     * test表达式，例如：> 5, 表示注解的参数值大于5， 如果表达式包含非注解参数的判断，则显式的写明参数名称,
     *
     * 例如，注解在type参数上的表达式为：== 1 or == 2, 则最终解析为 type == 1 or type == 2
     */
    @Language("GenericSQL")
    val testExpression: String,
)
