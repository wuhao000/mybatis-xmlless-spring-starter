package com.aegis.mybatis.xmlless.kotlin


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
/**
 * 将列表使用指定元素进行分割
 * @param e 指定的元素
 * @return 分割后的多个列表形成的列表
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

/**
 * 将一个列表按指定子列表进行分割，例如 1,2,3,5,7,9,3,5,9,4,2
 * 被子列表3,5分割的结果为 1,2   7,9   9,4,2 三个列表
 * 分割后的结果中不包含空的列表
 *
 * @param els 子列表
 * @return 分割后的列表形成的列表
 */
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
