package com.aegis

import org.junit.jupiter.api.Test
import org.springframework.beans.BeanUtils

data class Data1(var name: String? = null,
                 var age: Int? = null)

data class Data2(var name: String? = null,
                 var age: Int? = null)

/**
 *
 * Created by 吴昊 on 2019-01-08.
 *
 * @author 吴昊
 * @since 0.1.9
 */
class BeanUtilsTest {

  @Test
  fun test() {
    val data1 = Data1("a")
    val data2 = Data2(age = 2)
    BeanUtils.copyProperties(data1, data2)
    println(data1)
    println(data2)
  }

}
