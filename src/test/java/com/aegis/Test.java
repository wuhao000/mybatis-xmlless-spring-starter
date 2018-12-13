package com.aegis;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Created by 吴昊 on 2018/12/13.
 */
public class Test {

  @org.junit.Test
  public void test() {
    Pageable pageable = PageRequest.of(0, 20,     Sort.by("name"));
    System.out.println(pageable.getSort()
                           .get()
    .toArray());
  }


}
