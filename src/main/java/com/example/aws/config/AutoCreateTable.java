package com.example.aws.config;

import java.lang.annotation.*;
                                                                  
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface AutoCreateTable {
      String tableName() default "";
  }
