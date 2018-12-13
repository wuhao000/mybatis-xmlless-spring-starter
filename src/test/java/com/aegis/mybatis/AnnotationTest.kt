package com.aegis.mybatis

import com.aegis.mybatis.bean.SecurityStrategy
import org.junit.Test

/**
 *
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
class AnnotationTest {

  @Test
  fun test() {
    SecurityStrategy::class.java.declaredFields.forEach {
      println(it.name)
      println(it.annotations.toList())
      println(it.declaredAnnotations.toList())
    }
  }

}
