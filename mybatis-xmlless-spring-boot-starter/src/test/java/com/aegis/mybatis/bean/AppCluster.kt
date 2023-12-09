package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinProperty
import com.aegis.mybatis.xmlless.annotations.JoinTable
import com.aegis.mybatis.xmlless.annotations.JoinTableColumn
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
    @JoinTableColumn(
        table = TABLE_PREFIX + "app_instance",
        columnMapTo = "id",
        joinOnColumn = "cluster_id",
        joinOnThisProperty = "id"
    )
    var instanceIds: ArrayList<Int> = arrayListOf()
) {

  @JoinTableColumn(
      table = TABLE_PREFIX + "app",
      columnMapTo = "name",
      joinOnThisProperty = "appId"
  )
  var appName: String? = null

}
