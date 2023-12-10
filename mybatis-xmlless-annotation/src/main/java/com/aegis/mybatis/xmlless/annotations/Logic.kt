package com.aegis.mybatis.xmlless.annotations


/**
 * @author 吴昊
 * @date 2021/03/27
 * @version 1.0
 * @since 3.5.1
 */
@Target(AnnotationTarget.FUNCTION)
@Deprecated(
    message = "使用@Deleted 或 @NotDeleted 替代"
)
annotation class Logic(
    /**
     * 1. 当作用于删除方法时，flag表示要更新的状态：Deleted-将数据标记为已删除，NotDeleted-将数据标记为未删除
     * 2. 当作用于查询方法时，flag表示要更新的状态：Deleted-过滤掉已删除的数据，NotDeleted-过滤掉未删除的数据，All-不过滤
     */
    val flag: DeleteValue
)

/**
 * @author 吴昊
 * @date 2021/03/27
 * @version 1.0
 * @since 3.5.1
 */
enum class DeleteValue {

  Deleted,
  NotDeleted;

}
