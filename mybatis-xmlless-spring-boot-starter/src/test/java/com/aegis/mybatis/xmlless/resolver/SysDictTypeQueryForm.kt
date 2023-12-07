package com.aegis.mybatis.xmlless.resolver

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

open class BaseForm {

  var beginTime: Date? = null

  var endTime: Date? = null

  var orderByColumn: String? = null

  var isDesc: Boolean = false

}

/**
 * 字典类型表 sys_dict_type
 *
 * @author luojiamin
 */
class SysDictTypeQueryForm : BaseForm() {

  @JsonIgnore
  var dataScope: String = ""

  /** 字典名称  */
  var dictName: String? = null

  /** 字典类型  */
  var dictType: String? = null

  /** 状态（0正常 1停用）  */
  var status: String? = null

}
