package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import javax.persistence.Entity
import javax.persistence.Id


/**
 * TODO
 *
 * @author Administrator
 * @version 1.0
 * @date 2020/7/18 14:12
 * @since TODO
 */
@Entity
class Project {

  @Id
  var id: String = ""

  @JsonMappingProperty
  var tapdWorkspaceId: Set<Long>? = null

}
