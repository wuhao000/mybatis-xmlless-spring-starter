package com.aegis.mybatis.xmlless.generator

/**
 * TODO
 *
 * @author wuhao
 * @date 2023/12/6 18:10
 * @since v0.0.0
 * @version 1.0
 */

fun interface IdGenerator<T> {

  /**
   * 生成id
   *
   * @param table 表名
   * @param column 主键列名
   * @return
   */
  fun generateId(table: String?, column: String?): T?

}
