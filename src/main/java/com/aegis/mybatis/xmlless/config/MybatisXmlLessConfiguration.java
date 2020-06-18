package com.aegis.mybatis.xmlless.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by 吴昊 on 2018/12/13.
 *
 * @author wuhao
 * @date 2018
 * @version 0.1
 * @since 0.1
 */
public class MybatisXmlLessConfiguration extends MybatisConfiguration {


  public MybatisXmlLessConfiguration() {
    super();
  }

  private MybatisXmlLessMapperRegistry mybatisXmlLessMapperRegistry = new MybatisXmlLessMapperRegistry(this);

  public <T> void addMapper(Class<T> type ) {
    mybatisXmlLessMapperRegistry.addXMlLessMapper(type);
  }

  public void addMappers(String packageName, Class superType) {
    mybatisXmlLessMapperRegistry.addMappers(packageName, superType);
  }

  public void addMappers( String packageName) {
    mybatisXmlLessMapperRegistry.addMappers(packageName);
  }

  public <T>  T getMapper(Class<T> type, SqlSession sqlSession) {
    return mybatisXmlLessMapperRegistry.getXmlLessMapper(type, sqlSession);
  }

  public MapperRegistry getMapperRegistry() {
    return mybatisXmlLessMapperRegistry;
  }

  public boolean hasMapper(Class type) {
    return mybatisXmlLessMapperRegistry.hasXmlLessMapper(type);
  }

}
