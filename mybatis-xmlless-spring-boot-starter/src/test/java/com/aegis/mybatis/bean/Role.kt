package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import com.baomidou.mybatisplus.annotation.TableLogic
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 *
 * @author 吴昊
 * @date 2021/3/30 10:24 下午
 * @since 3.5.1
 * @version 1.0
 */
@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0,
    var name: String = "",
    @field: JsonMappingProperty
    var deps: List<Int> = listOf(),
    @JoinObject(
        targetTable = "t_dep",
        targetColumn = "id",
        joinProperty = "deps",
        associationPrefix = "dep_"
    )
    @SelectedProperties(["id", "name"])
    var depList: MutableList<Dep> = arrayListOf(),
    @TableLogic(value = "0", delval = "2")
    var delFlag: String = "0"
)
