package com.aliya.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ViewCreatorAnnotation
 *
 * @author a_liYa
 * @date 2020/9/23 16:06.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ViewCreatorAnnotation {
}
