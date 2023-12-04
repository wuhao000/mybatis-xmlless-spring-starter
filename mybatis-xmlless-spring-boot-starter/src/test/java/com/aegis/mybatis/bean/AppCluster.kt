package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinProperty
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * 应用集群
 * @author 吴昊
 * @since 1.0
 */
@Entity
class AppCluster(
    @Id
    @GeneratedValue
    var id: Int = 0,
    /**  关联的应用id */
    var appId: Int = 0,
    /**  关联的应用实例id集合 */
    @JoinProperty(
        targetTable = TABLE_PREFIX + "app_instance",
        joinProperty = "id",
        targetColumn = "cluster_id",
        selectColumn = "id"
    )
    var instanceIds: ArrayList<Int> = arrayListOf()
) {

    /**  临时字段 */
  @JoinProperty(
      selectColumn = "name",
      targetColumn = "id",
      joinProperty = "appId",
      targetTable = TABLE_PREFIX + "app"
  )
  var appName: String? = null

}
