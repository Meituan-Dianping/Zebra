package com.dianping.zebra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the method is for internal use only.
 * 
 * @author damonzhu
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Internal {
}
