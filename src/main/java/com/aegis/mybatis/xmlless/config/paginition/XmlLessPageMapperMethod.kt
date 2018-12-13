package com.aegis.mybatis.xmlless.config.paginition

import com.aegis.mybatis.xmlless.methods.UnknownMethods
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.override.PageMapperMethod
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
                              val requestMethod: Method,
                              config: Configuration) :
    PageMapperMethod(mapperInterface, requestMethod, config) {

  val command = PageMapperMethod.SqlCommand(config, mapperInterface, requestMethod)
  val method = PageMapperMethod.MethodSignature(config, mapperInterface, requestMethod)

  init {
  }

  override fun execute(sqlSession: SqlSession, args: Array<out Any>?): Any? {
    var result: Any? = null
    if (command.type == SqlCommandType.SELECT && args != null
        && Page::class.java.isAssignableFrom(method.returnType)) {
      val list = executeForMany<Any>(sqlSession, args) as List<Any>
      result = if (IPage::class.java.isAssignableFrom(args[0].javaClass)) {
        val pageArg = (args[0] as IPage<*>)
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
      result = super.execute(sqlSession, args)
    }
    return result
  }

  private fun <E> executeForMany(sqlSession: SqlSession, args: Array<out Any>?): Any {
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

  private fun executeForTotal(sqlSession: SqlSession, args: Array<out Any>?): Long {
    val param = method.convertArgsToSqlCommandParam(args)
    val commandName = command.name + UnknownMethods.COUNT_STATEMENT_SUFFIX
    return sqlSession.selectOne<Long>(commandName, param)
  }

}
