package com.aegis.mybatis.xmlless.generator

import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.KeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.slf4j.LoggerFactory
import java.sql.Statement
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.inv

/**
 * 雪花算法ID生成器
 * 实现IdentifierGenerator，Configurable，以便加入hibernate自定义的id生成策略
 *
 * @author 吴昊
 * @version 1.0.0
 * @date 2023/12/06
 * @since 1.0.0
 */
class SnowflakeKeyGenerator : KeyGenerator {

  private var sequenceOffset: Byte = 0
  private var sequence: Long = 0
  private var lastMilliseconds: Long = 0

  companion object {
    var epoch: Long = 0
    private val log = LoggerFactory.getLogger(SnowflakeKeyGenerator::class.java)
    private const val SEQUENCE_MASK = 4095L
    private const val WORKER_ID_LEFT_SHIFT_BITS = 12L
    private const val TIMESTAMP_LEFT_SHIFT_BITS = 22L
    private const val maxTolerateTimeDifferenceMilliseconds = 10
    private const val workerId: Long = 0

    init {
      val calendar = Calendar.getInstance()
      calendar[2010, 0] = 1
      epoch = calendar.getTimeInMillis()
    }
  }

  @Synchronized
  fun generateKey(): Number {
    var currentMilliseconds = System.currentTimeMillis()
    if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
      currentMilliseconds = System.currentTimeMillis()
    }
    if (lastMilliseconds == currentMilliseconds) {
      if (0L == (sequence + 1L and SEQUENCE_MASK).also { sequence = it }) {
        currentMilliseconds = waitUntilNextTime(currentMilliseconds)
      }
    } else {
      vibrateSequenceOffset()
      sequence = sequenceOffset.toLong()
    }
    lastMilliseconds = currentMilliseconds
    return currentMilliseconds - epoch shl TIMESTAMP_LEFT_SHIFT_BITS.toInt() or (workerId shl WORKER_ID_LEFT_SHIFT_BITS.toInt()) or sequence
  }

  @Synchronized
  fun generateId(): String {
    return generateKey().toString()
  }

  override fun processBefore(executor: Executor, ms: MappedStatement, stmt: Statement?, parameter: Any) {
    val metaObject = MetaObjectUtil.forObject(parameter)
    if (ms.keyProperties.size != 1) {
      error("主键列数量不正确")
    }
    val property = ms.keyProperties.first()
    if (metaObject.getValue(property) == null) {
      val id = generateId()
      metaObject.setValue(property, id)
    }
  }

  override fun processAfter(executor: Executor, ms: MappedStatement, stmt: Statement, parameter: Any) {
  }

  private fun waitTolerateTimeDifferenceIfNeed(currentMilliseconds: Long): Boolean {
    return try {
      if (lastMilliseconds <= currentMilliseconds) {
        false
      } else {
        val timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds
        try {
          check(timeDifferenceMilliseconds < maxTolerateTimeDifferenceMilliseconds) {
            String.format(
                "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds",
                lastMilliseconds, currentMilliseconds
            )
          }
        } catch (var5: IllegalStateException) {
          log.error("回拨时间大于maxTolerateTimeDifferenceMilliseconds", var5)
        }
        Thread.sleep(timeDifferenceMilliseconds)
        true
      }
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
      log.error("Thread.sleep出错", e)
      true
    }
  }

  private fun waitUntilNextTime(lastTime: Long): Long {
    var result: Long
    result = System.currentTimeMillis()
    while (result <= lastTime) {
      result = System.currentTimeMillis()
    }
    return result
  }

  private fun vibrateSequenceOffset() {
    sequenceOffset = (sequenceOffset.inv() and 1)
  }

}
