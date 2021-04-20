@file:Suppress("unused")

package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.AppCluster
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Mapper
interface AppClusterDAO : XmlLessMapper<AppCluster> {

  @ResolvedName("findByNameOrUrlLikeKeywords")
  fun findAll(@Param("keywords") keywords: String?,
              @Param("pageable") pageable: Pageable): Page<AppCluster>

}
