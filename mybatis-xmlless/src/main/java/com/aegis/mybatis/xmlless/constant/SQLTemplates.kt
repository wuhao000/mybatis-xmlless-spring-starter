package com.aegis.mybatis.xmlless.constant


/**
 *
 * Created by 吴昊 on 2018-12-14.
 *
 * @author 吴昊
 * @since 0.0.8
 */
/**  insert 语句模板 */
internal const val BATCH_INSERT = """<script>
INSERT INTO
  %s(%s)
VALUES
  <foreach collection="list" item="item" separator=",">
    (%s)
  </foreach>
</script>
"""

internal const val BATCH_INSERT_OR_UPDATE = """<script>
INSERT INTO
  %s(%s)
VALUES
  <foreach collection="list" item="item" separator=",">
    (%s)
  </foreach>
ON DUPLICATE KEY UPDATE
  %s 
</script>
"""


/**  删除语句模板 */
internal const val DELETE = """<script>
DELETE FROM
  %s
%s
</script>"""

/**  单条数据插入语句模板 */
internal const val INSERT = """<script>INSERT INTO
  %s(%s)
VALUES
  (%s)</script>"""

/**  单条数据插入或更新语句模板 */
internal const val INSERT_OR_UPDATE = """<script>INSERT INTO
  %s(%s)
VALUES 
  (%s)
ON DUPLICATE KEY UPDATE
  %s</script>"""

/**  join表达式模板 */
internal const val JOIN = """
  %S JOIN
    %s
  ON
    %s.%s = %s.%s
"""

/**  limit 语句模板 */
internal const val LIMIT = "LIMIT #{%s}, #{%s}"
internal const val LIMIT_H2 = "LIMIT #{%s}-#{%s}"

/**  Pageable参数中的排序 */
internal const val PAGEABLE_SORT = """<if test="%s.sort.isSorted">
  <foreach collection="%s.sort.get().toArray()" item="item" separator=",">
    ${'$'}{item.property} <if test="item.isAscending">ASC</if><if test="item.isDescending">DESC</if>
  </foreach>
</if>"""

/**  查询语句模板 */
internal const val SELECT = """SELECT
  %s
FROM
  %s %s
%s %s %s
%s"""

/**  count语句模板 */
internal const val SELECT_COUNT = """<script>
SELECT
  COUNT(*)
FROM
  %s
%s
</script>"""

/**  子查询构建模板 */
internal const val SUB_QUERY = """(SELECT
  *
FROM
  %s
%s
%s) AS %s"""

/**  更新语句模板 */
internal const val UPDATE = """<script>
UPDATE
  %s
  %s
%s
</script>"""

/**  where条件模板 */
internal const val WHERE = """<where>
  <trim suffixOverrides=" AND">
    <trim suffixOverrides=" OR">
%s
    </trim>
  </trim>
</where>"""


internal const val ORDER_BY = """<trim suffixOverrides="ORDER BY">
  ORDER BY
  <trim suffixOverrides=",">
    <trim>
      %s
    </trim>
  </trim>
</trim>
"""
