package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Project
import com.aegis.mybatis.xmlless.annotations.JsonResult
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * Created by 吴昊 on 2019/2/21.
 */
@Mapper
interface ProjectDAO : XmlLessMapper<Project> {

  /**
   *
   * @return
   */
  @JsonResult
  fun findTapdWorkspaceId(): List<List<Long>>

  /**
   *
   * @param id
   * @return
   */
  @JsonResult(true)
  fun findTapdWorkspaceIdById(@Param("id") id: String): List<String>?

}
