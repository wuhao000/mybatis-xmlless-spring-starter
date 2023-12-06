package com.aegis.mybatis.bean

/**
 * 单点登录平台信息FORM
 *
 * @author 骆嘉民 2022-06-14
 * @version 0.0.1
 */
class ThirdPartyInfoForm {

  /** 平台名称（与部门表第二层名称对应）  */
  var deptName: String? = null

  /** 检测登录状态路径  */
  var checkLoginUrl: String? = null

  /** 第三方接口请求反向代理前缀  */
  var prefix: String? = null

  /** 是否12368平台（0 否， 1 是）  */
  var is12368: Boolean? = null

  /** 备注  */
  var remark: String? = null

}
