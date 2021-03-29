package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
interface DogDAO : XmlLessMapper<Dog>, BaseDAO<Dog, Int> {



}
