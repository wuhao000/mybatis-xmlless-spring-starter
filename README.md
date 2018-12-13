## 介绍

**如果你对于写mapper文件非常厌恶，那么这个项目非常适合你** 

本项目是为了较少mybatis中xml配置诞生的，项目依赖于mybatis及mybatis-plus

本项目的主要的设计思想是借鉴spring data jpa的做法，通过方法名称自动推断sql，但比起spring data jpa，支持更为复杂的sql的表达

> 文档中的示例sql均为简化的sql，目的在于表达，并不是最终生成的sql
> 文档中的示例代码均为kotlin代码


## 配置说明

本项目无需任何

## sql推断说明

### select查询推断

- 从方法名称中推断的字段名称均为mapper关联数据对象的属性名称，而非数据库中的表字段名称

#### 例1 findById

解析为 select * from table where id = #{id}

#### 例2 findByName

解析为 select * from table where name = #{name}

#### 例3 findByNameLike

解析为 select * from table where name like concat('%',#{name}, '%')

#### 例4 findByNameLikeKeyword

解析为 select * from table where name like concat('%',#{keyword}, '%')

#### 例5 findByNameEqAndId 

解析为 select * from table where name = #{name} and id = #{id}

#### 例6 findIdAndNameByAge

解析为 select id, name from table where age = #{age}

### sql推断名称与方法名称隔离

在mapper方法上使用@ResolvedName注解，该注解的必选参数name将会代替方法名称作为推断sql的名称，这样可以让方法名称更具语义化

例如 
```kotlin
@ResolvedName("findIdAndNameAndAge")
fun findSimpleInfoList(): List<User>
```
将使用 findIdAndNameAndAge 推断sql，推断的结果为：
select id,name,age from user

### 指定方法获取的属性集合

使用 @SelectedProperties注解

例如
```kotlin
@SelectedProperties(properties=["id", "name", "age"])
fun findSimpleInfoList(): List<User>
```
 
上一个示例中的 @ResolvedName("findIdAndNameAndAge") 便可以用 @SelectedProperties(properties=["id", "name", "age"]) 来代替

- 注：使用@SelectedProperties注解之后，从方法名中推断的查询属性将被忽略


### delete操作推断

支持 deleteAll deleteById deleteByName的写法


### update操作推断

支持 update 一个对象