package com.aegis.mybatis.xmlless.config.paginition

import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.baomidou.mybatisplus.core.metadata.IPage
import org.apache.ibatis.binding.MapperMethod
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.lang.reflect.Method

/**
 *
 * Created by 吴昊 on 2018/12/13.
 *
 * @author 吴昊
 * @since 0.0.4
 */
class XmlLessPageMapperMethod(mapperInterface: Class<*>,
                              requestMethod: Method,
                              config: Configuration) :
    MapperMethod(mapperInterface, requestMethod, config) {

  private val command = MapperMethod.SqlCommand(config, mapperInterface, requestMethod)
  private val method = MapperMethod.MethodSignature(config, mapperInterface, requestMethod)

  init {
  }

  @Suppress("UNCHECKED_CAST")
  override fun execute(sqlSession: SqlSession, args: Array<out Any?>?): Any? {
    var result: Any? = null
    if (command.type == SqlCommandType.SELECT && args != null
        && Page::class.java.isAssignableFrom(method.returnType)) {
      val list = executeForMany<Any>(sqlSession, args) as List<Any>
      val pageArg = findIPageArg(args) as IPage<*>?
      result = if (pageArg != null) {
        PageImpl(list, PageRequest.of((pageArg.current - 1).toInt(), pageArg.size.toInt()), pageArg.total)
      } else {
        val pageableArg = args.firstOrNull { it is Pageable } as Pageable?
        val total = executeForTotal(sqlSession, args)
        if (pageableArg != null) {
          PageImpl(list, pageableArg, total)
        } else {
          PageImpl(list, PageRequest.of(0, list.size), total)
        }
      }
    }
    if (result == null) {
      try {
        result = super.execute(sqlSession, args)
      } catch (e: Exception) {
        e.printStackTrace()
        throw e
      }
    }
    return result
  }

  private fun <E> executeForMany(sqlSession: SqlSession, args: Array<out Any?>?): Any {
    val result: List<E>
    val param = method.convertArgsToSqlCommandParam(args)
    result = if (method.hasRowBounds()) {
      val rowBounds = method.extractRowBounds(args)
      sqlSession.selectList(command.name, param, rowBounds)
    } else {
      sqlSession.selectList(command.name, param)
    }
    return result
  }

  private fun executeForTotal(sqlSession: SqlSession, args: Array<out Any?>?): Long {
    val param = method.convertArgsToSqlCommandParam(args)
    val commandName = command.name + XmlLessMethods.COUNT_STATEMENT_SUFFIX
    return sqlSession.selectOne(commandName, param)
  }

  private fun findIPageArg(args: Array<out Any?>): Any? {
    return args.filterNotNull().firstOrNull {
      IPage::class.java.isAssignableFrom(it.javaClass)
    }
  }

}
