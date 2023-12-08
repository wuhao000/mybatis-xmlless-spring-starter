package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinProperty
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

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
        toTable = com.aegis.mybatis.xmlless.annotations.JoinTable(
            targetTable = TABLE_PREFIX + "app_instance",
            columnMapTo = "id",
            joinOnColumn = "cluster_id"
        ),
        joinOnThisProperty = "id"
    )
    var instanceIds: ArrayList<Int> = arrayListOf()
) {

    /**  临时字段 */
  @JoinProperty(
      joinOnThisProperty = "appId",
      toTable = com.aegis.mybatis.xmlless.annotations.JoinTable(
          targetTable = TABLE_PREFIX + "app",
          columnMapTo = "name"
      )
  )
  var appName: String? = null

}
