package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Server
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper

/**
 *
 * Created by 吴昊 on 2018/12/17.
 *
 * @author 吴昊
 * @since 0.0.9
 */
@Mapper
interface ServerDAO : XmlLessMapper<Server> {

  /**
   *
   * @return
   * @param id
   */
  fun findById(id: Int): Server

}
