package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 *
 * Created by 吴昊 on 2019/7/4.
 *
 * @author 吴昊
 * @since 3.0.0
 */
@Mapper
interface ScoreDAO : XmlLessMapper<Score> {

  /**
   *
   * @return
   */
  fun findAll(): List<Score>

  /**
   *
   * @param studentId
   * @return
   */
  fun findByStudentId(@Param("studentId") studentId: String): List<Score>

  /**
   *
   * @param score
   */
  fun save(score: Score)

}
