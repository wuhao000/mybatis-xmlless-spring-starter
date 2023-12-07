package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.StringKeyObj
import com.aegis.mybatis.xmlless.XmlLessMapper

/**
 * TODO
 *
 * @author wuhao
 * @date 2023/12/6 18:26
 * @since v0.0.0
 * @version 1.0
 */
interface StringKeyObjDAO : XmlLessMapper<StringKeyObj> {

  /**
   * @param obj
   */
  fun insert(obj: StringKeyObj)

  /**
   * @return
   */
  fun findAll(): List<StringKeyObj>

  fun existsById(id: String): Boolean

}
