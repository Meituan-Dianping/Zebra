package com.dianping.zebra.dao.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.springframework.util.Assert;

/**
 * @ClassName: ReflectionUtils <br>
 * @Description: 反射工具类 <br>
 * 
 * @author Freud
 */
public class ReflectionUtils {

    /**
     * 循环向上转型, 获取对象的DeclaredField.
     * 
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getDeclaredField(final Object object,
            final String fieldName) {
        Assert.notNull(object, "object不能为空");
        Assert.hasText(fieldName, "fieldName");
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (final NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     */
    public static Object getFieldValue(final Object object,
            final String fieldName) {
        final Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field ["
                    + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        } catch (final IllegalAccessException e) {
        }
        return result;
    }

    /**
     * 强行设置Field可访问.
     */
    protected static void makeAccessible(final Field field) {
        if (!Modifier.isPublic(field.getModifiers())
                || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
     */
    public static void setFieldValue(final Object object,
            final String fieldName, final Object value) {
        final Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field ["
                    + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (final IllegalAccessException e) {
        }
    }
}