package com.aegis.mybatis.xmlless.generator

import org.apache.ibatis.executor.keygen.KeyGenerator
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * TODO
 *
 * @author wuhao
 * @date 2023/12/6 18:10
 * @since v0.0.0
 * @version 1.0
 */

object IdGeneratorUtil {

  private val CACHE: MutableMap<String, KeyGenerator> = ConcurrentHashMap()
  private val log = LoggerFactory.getLogger(IdGeneratorUtil::class.java)

  fun registerGenerator(name: String, generator: KeyGenerator) {
    if (CACHE.containsKey(name)) {
      log.warn("已经存在名为 $name 的 ID 生成器")
      return
    }
    CACHE[name] = generator
  }

  fun getGenerator(name: String): KeyGenerator {
    return CACHE[name] ?: error("不存在名为 $name 的 ID 生成器")
  }


}
