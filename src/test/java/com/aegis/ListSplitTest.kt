package com.aegis

import com.aegis.kotlin.split
import org.junit.Test

class ListSplitTest {

  val list = listOf(3, 5, 2, 4, 3, 5, 1, 3, 5, 6, 2, 4, 3,5)

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
        if (this.size > 0) {
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
