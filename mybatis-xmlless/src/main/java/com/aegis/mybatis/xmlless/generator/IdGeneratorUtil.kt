package com.aegis.mybatis.xmlless.generator

import org.apache.ibatis.executor.keygen.KeyGenerator
import java.util.concurrent.ConcurrentHashMap

/**
 * id生成工具
 */
object IdGeneratorUtil {

  private val CACHE: MutableMap<String, KeyGenerator> = ConcurrentHashMap()

  fun registerGenerator(name: String, generator: KeyGenerator) {
    CACHE[name.uppercase()] = generator
  }

  fun getGenerator(name: String): KeyGenerator {
    return CACHE[name.uppercase()] ?: error("不存在名为 $name 的 ID 生成器")
  }

}
