package com.aegis.mybatis.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.aegis.mybatis.bean.Student;
import com.aegis.mybatis.bean.StudentDetail;
import com.aegis.mybatis.bean.StudentState;
import com.aegis.mybatis.xmlless.XmlLessMapper;
import com.aegis.mybatis.xmlless.annotations.ExcludeProperties;
import com.aegis.mybatis.xmlless.annotations.ResolvedName;
import com.aegis.mybatis.xmlless.annotations.SelectedProperties;
import com.aegis.mybatis.xmlless.annotations.TestExpression;
import com.aegis.mybatis.xmlless.enums.TestType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
public interface StudentDAO2 extends XmlLessMapper<Student> {

  int count();

  void deleteById(@Param("id") String id);


  @ResolvedName(name = "deleteByIdInIds")
  void deleteByIds(@Param("ids") List<String> ids);

  void deleteByName(@Param("name") String name);

  boolean existsById(@Param("id") String id);

  boolean existsByName(@Param("name") String name);

  List<Student> findAll();

  @ResolvedName(name = "findAllByNameEqAndSubjectIdEq")
  Page<Student> findAllPage(
      @TestExpression({TestType.NotNull, TestType.NotEmpty}) String name,
      Integer subjectId,
      @Param("pageable") Pageable page
  );

  @ResolvedName(name = "findAll")
  Page<Student> findAllPageable(@Param("pageable") Pageable pageable);

  List<Student> findByAgeBetweenMinAndMaxOrderByBirthday(@Param("min")
                                                         @TestExpression(TestType.NotNull)
                                                         int min,
                                                         @Param("max")
                                                         @TestExpression(TestType.NotNull)
                                                         int max);

  List<Student> findByCreateTimeBetweenStartTimeAndEndTime(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );

  @ResolvedName(name = "findByAge", partNames = "name")
  List<Student> findByAge(@Param("age") int age, @Param("name") String name);

  List<Student> findByAgeGte(@Param("age") int age);

  @SelectedProperties(properties = {"id", "name", "createTime", "birthday"})
  List<Student> findByBirthday(LocalDate date);

  List<Student> findByGraduatedEqTrue();

  Student findById(@Param("id") String id);

  List<Student> findByPhoneNumberLikeLeft(@Param("phoneNumber") String phoneNumber);

  List<Student> findByPhoneNumberLikeRight(@Param("phoneNumber") String phoneNumber);

  List<Student> findByStateIn(@Param("state") List<StudentState> state);

  List<Student> findBySubjectId(@Param("subjectId") int subjectId);

  List<StudentDetail> findDetail();

  List<String> findId();

  void save(Student student);

  void saveAll(List<Student> list);

  void saveOrUpdate(Student student);

  @ExcludeProperties(update = {"name"})
  void saveOrUpdateAll(List<Student> list);

  int update(Student student);

  int updateNameById(@Param("name") String nmme, @Param("id") String id);

  @SelectedProperties(properties = {"name", "phoneNumber"})
  @ResolvedName(name = "update")
  int updatePartly(Student student);

  List<Student> findByNameOrAge(QueryForm form);

  List<Student> findByAgeBetween(Integer min, Integer max);

  List<Student> findByAgeBetweenMinAndMax(Integer min, Integer max);
}
