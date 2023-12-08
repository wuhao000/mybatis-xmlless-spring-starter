package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.ibatis.annotations.Mapper

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
interface StudentBaseMapperDAO : XmlLessMapper<SimpleStudent>, BaseMapper<SimpleStudent>

@Entity
@Table(name = "t_student")
class SimpleStudent(
    @Id
    var id: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var sex: Int = 1
)
