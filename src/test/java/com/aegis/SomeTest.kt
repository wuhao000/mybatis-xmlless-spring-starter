package com.aegis

import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

/**
 * Created by 吴昊 on 2018/12/13.
 */
class SomeTest {

  @Test
  fun test() {
    val pageable = PageRequest.of(0, 20, Sort.by("name"))
    println(pageable.sort
        .get()
        .toArray())
  }

  @Test
  fun test2() {

  }

}
