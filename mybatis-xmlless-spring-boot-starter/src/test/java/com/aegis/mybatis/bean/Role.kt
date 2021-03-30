package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 *
 * @author 吴昊
 * @date 2021/3/30 10:24 下午
 * @since 3.5.1 TODO
 * @version 1.0
 */
@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0,
    var name: String = "",
    @JsonMappingProperty
    var deps: List<Int> = listOf(),
    @JoinObject(
        targetTable = "t_dep",
        targetColumn = "id",
        joinProperty = "deps",
        associationPrefix = "dep_",
        selectProperties = ["id", "name"]
    )
    var depList: MutableList<Dep> = arrayListOf()
)
