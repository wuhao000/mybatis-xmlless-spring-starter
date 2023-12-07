package com.aegis.mybatis.xmlless.generator

import org.apache.ibatis.executor.keygen.KeyGenerator
import java.util.concurrent.ConcurrentHashMap

/**
 * id生成工具
 */
object GenIdUtil {

  private val CACHE: MutableMap<String, KeyGenerator> = ConcurrentHashMap()

  fun registerGenerator(name: String, generator: KeyGenerator) {
    if (CACHE.containsKey(name)) {
      error("已经存在名为 $name 的 ID 生成器")
    }
    CACHE[name] = generator
  }

  fun getGenerator(name: String): KeyGenerator {
    return CACHE[name] ?: error("不存在名为 $name 的 ID 生成器")
  }

}
