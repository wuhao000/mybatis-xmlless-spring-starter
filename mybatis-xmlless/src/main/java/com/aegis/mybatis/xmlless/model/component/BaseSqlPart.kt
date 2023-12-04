package com.aegis.mybatis.xmlless.model.component


/**
 *
 * @author wuhao
 * @date 2023/12/4 22:38
 * @since v0.0.0
 * @version 1.0
 */
interface ISqlPart {

  /**
   * 对象转sql字符串
   */
  fun toSql(): String

}
