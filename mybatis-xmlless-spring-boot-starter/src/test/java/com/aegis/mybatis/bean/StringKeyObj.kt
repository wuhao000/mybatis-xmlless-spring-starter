package com.aegis.mybatis.bean

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 *
 * @author wuhao
 * @date 2023/12/6 18:24
 * @since v0.0.0
 * @version 1.0
 */
@Table(name = "t_string_key_obj")
class StringKeyObj {

  @Id
  @GeneratedValue(generator = "SNOWFLAKE")
  var id: String? = null
  var name: String? = null

}
