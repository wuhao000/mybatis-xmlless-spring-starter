package com.aegis.mybatis.xmlless.resolver.bean

import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 *
 * @author wuhao
 * @date 2023/12/11 18:49
 * @since v0.0.0
 * @version 1.0
 */
@Table(name = "t_student")
class Student {

  @Id
  var id: String? = null
}
