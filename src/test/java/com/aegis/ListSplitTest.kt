package com.aegis

import com.aegis.mybatis.xmlless.kotlin.split
import org.junit.jupiter.api.Test

/**
 * 列表分割测试类
 * @author 吴昊
 * @since 0.0.3
 */
class ListSplitTest {

  private val list = listOf(3, 5, 2, 4, 3, 5, 1, 3, 5, 6, 2, 4, 3,5)

  @Test
  fun splitByList() {
  }

  @Test
  fun splitBySingle() {
    val element = 3
    val result = arrayListOf<List<Int>>()
    var copyList = list.toList()
    while (copyList.indexOf(element) >= 0) {
      copyList.subList(0, copyList.indexOf(element)).apply {
        if (this.isNotEmpty()) {
          result.add(this)
        }
      }
      copyList = copyList.subList(copyList.indexOf(element) + 1, copyList.size)
    }
    if (copyList.isNotEmpty()) {
      result.add(copyList)
    }
    println(result)
    println(list.split(element))
  }

}
