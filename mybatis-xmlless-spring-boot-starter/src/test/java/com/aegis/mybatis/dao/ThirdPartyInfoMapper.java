package com.aegis.mybatis.dao;

import java.util.List;

import com.aegis.mybatis.bean.TenantInfoVO;
import com.aegis.mybatis.bean.ThirdPartyInfo;
import com.aegis.mybatis.bean.ThirdPartyInfoForm;
import com.aegis.mybatis.xmlless.XmlLessMapper;
import com.aegis.mybatis.xmlless.annotations.Deleted;
import com.aegis.mybatis.xmlless.annotations.NotDeleted;
import com.aegis.mybatis.xmlless.annotations.ResolvedName;
import com.aegis.mybatis.xmlless.annotations.SelectedProperties;
import org.apache.ibatis.annotations.Param;

public interface ThirdPartyInfoMapper extends XmlLessMapper<ThirdPartyInfo> {

  /**
   * 根据平台名称获取平台信息
   *
   * @param appId 应用ID
   * @return 第三方登录平台信息数据
   */
  @NotDeleted
  ThirdPartyInfo findByAppId(String appId);

  /**
   * 查询单点登录平台信息数据列表
   *
   * @param form 表单
   * @return 单点登录平台信息数据列表
   */
//  List<ThirdPartyInfo> list(ThirdPartyInfoForm form);

  @ResolvedName(name = "findByDeptNameAndCheckLoginUrlAndPrefixAndIs12368AndRemarkOrderByCreateTimeDesc")
  List<ThirdPartyInfo> list2(ThirdPartyInfoForm form);

  /**
   * 根据appId、appSecret获取单点登录平台信息数据
   *
   * @param appId     应用ID
   * @param appSecret 应用密钥
   * @return 单点登录平台信息数据
   */
  @NotDeleted
  ThirdPartyInfo findByAppIdAndAppSecret(@Param("appId") String appId,
                                         @Param("appSecret") String appSecret);

  /**
   * 单点登录平台信息数据
   *
   * @param deptName 租户名称
   * @return 单点登录平台信息数据
   */
  @NotDeleted
  ThirdPartyInfo findByDeptName(String deptName);

  /**
   * 单点登录平台信息数据
   *
   * @param tenantCode 租户代码
   * @return 单点登录平台信息数据
   */
  @NotDeleted
  ThirdPartyInfo selectByTenantCode(String tenantCode);

  void insert(ThirdPartyInfo entity);

  ThirdPartyInfo selectById(String id);

  void updateById(ThirdPartyInfo entity);

  @Deleted
  void deleteByIdIn(List<String> ids);

  @Deleted
  void deleteById(String id);

  @SelectedProperties(properties = {"id", "tenantName", "tenantCode"})
  @NotDeleted
  @ResolvedName(name = "findAll")
  List<TenantInfoVO> tenantList();

}
