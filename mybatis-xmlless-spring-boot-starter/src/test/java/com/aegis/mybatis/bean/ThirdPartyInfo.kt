package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableLogic
import jakarta.persistence.*
import java.util.*

/**
 * 单点登录平台信息
 *
 * @author 江振朗 2022-06-14
 * @version 0.0.1
 */
@Entity
@Table(name = "sys_third_party_info")
class ThirdPartyInfo {

  /** 主键  */
  @Id
  @GeneratedValue(generator = "snowflake")
  @Column(name = "id", updatable = false)
  var id: String? = null

  /** （坐席端）租户代码  */
  var tenantCode: String? = null

  /** 租户名称/平台名称（与部门表第二层名称对应）  */
  @Column(name = "dept_name")
  var deptName: String? = null

  /** 检测登录状态路径  */
  @Column(name = "check_login_url")
  var checkLoginUrl: String? = null

  /** 登录刷新时间（单位：毫秒）  */
  @Column(name = "refresh_time")
  var refreshTime: Long? = null

  /** 第三方接口请求反向代理前缀  */
  @Column(name = "prefix")
  var prefix: String? = null

  /** 是否12368平台（0 否， 1 是）  */
  @Column(name = "is_12368")
  var is12368: Boolean? = null

  /** 短信签名  */
  @Column(name = "dxqm")
  var sign: String? = null

  /** 短信平台租户标识  */
  @Column(name = "dxptzhbs")
  var smsTenantId: String? = null

  /** 删除标志（0代表存在 2代表删除）  */
  @Column(name = "del_flag")
  @TableLogic(value = "0", delval = "2")
  var delFlag: String = "0"

  /** 创建者  */
  @Column(name = "create_by")
  var createBy: String? = null

  /** 创建时间  */
  @Column(name = "create_time")
  var createTime: Date? = null

  /** 更新者  */
  @Column(name = "update_by")
  var updateBy: String? = null

  /** 更新时间  */
  @Column(name = "update_time")
  var updateTime: Date? = null

  /** 备注  */
  @Column(name = "remark")
  var remark: String? = null

  /** 应用ID  */
  @Column(name = "app_id")
  var appId: String? = null

  /** 应用密钥  */
  @Column(name = "app_secret")
  var appSecret: String? = null

}
