package com.aegis.mybatis.bean

import jakarta.persistence.Column

/**
 * 租户信息VO
 *
 * @author 骆嘉民 2022-12-29
 * @version 0.0.1
 */
data class TenantInfoVO(
    /** 主键  */
    var id: String? = null,

    /** 平台名称（与部门表第二层名称对应）  */
    @Column(name = "dept_name")
    var tenantName: String? = null,

    /** 租户代码  */
    @Column(name = "zhdm")
    var tenantCode: String? = null,

    /** 数据汇聚访问地址  */
    var visitUrl: String? = null
)
