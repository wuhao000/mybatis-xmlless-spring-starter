package com.aegis.kotlin


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
/**
 *
 * @param e
 * @return
 */
fun <E> List<E>.split(e: E): List<List<E>> {
  val result = arrayListOf<List<E>>()
  var copyList = this.toList()
  while (copyList.indexOf(e) >= 0) {
    result.add(copyList.subList(0, copyList.indexOf(e)))
    copyList = copyList.subList(copyList.indexOf(e) + 1, copyList.size)
  }
  result.add(copyList)
  return result.filter { it.isNotEmpty() }
}

fun <E> List<E>.split(els: List<E>): List<List<E>> {
  val windows = this.windowed(els.size, 1)
  val indices = windows.mapIndexedNotNull { index, list ->
    if (list.containsAll(els) && els.containsAll(list)) {
      index
    } else {
      null
    }
  }
  return (indices.mapIndexed { index, start ->
    when (index) {
      0    -> this.subList(0, start)
      else -> this.subList(indices[index - 1] + els.size, start)
    }
  } + listOf(this.subList(indices.last() + els.size, this.size))).filter { it.isNotEmpty() }
}
