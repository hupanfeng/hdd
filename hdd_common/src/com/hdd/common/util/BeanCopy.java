package com.hdd.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class BeanCopy {
    /**
     * 
     * 描述信息:
     * 
     * @param dest
     * @param source
     * @throws Exception
     * @author David
     * 
     */
    public static void copyProperties(Object dest, Object source) throws Exception {
        Field[] destFields = dest.getClass().getDeclaredFields();
        Field[] sourceFields = source.getClass().getDeclaredFields();
        Method[] destMethods = source.getClass().getDeclaredMethods();
        Method[] sourceMethods = source.getClass().getDeclaredMethods();

        HashMap fieldMap = new HashMap();
        HashMap destMethodMap = new HashMap();
        HashMap sourceMethodMap = new HashMap();

        if (null != sourceMethods && null != destMethods) {
            for (Method m : sourceMethods) {
                sourceMethodMap.put(m.getName(), m);
            }
            for (Method m : destMethods) {
                destMethodMap.put(m.getName(), m);
            }
        } else {
            return;
        }

        if (null != destFields && null != sourceFields) {
            for (Field f : destFields) {
                String fieldName = f.getName();
                String firstLetter = fieldName.substring(0, 1).toUpperCase();
                // 获得和属性对应的getXXX()方法的名字
                String getMethodName = "get" + firstLetter + fieldName.substring(1);
                // 获得和属性对应的setXXX()方法的名字
                String setMethodName = "set" + firstLetter + fieldName.substring(1);

                if (null != destMethodMap.get(setMethodName) && null != sourceMethodMap.get(getMethodName)) {
                    Method destMethod = (Method) destMethodMap.get(setMethodName);
                    Method sourceMethod = (Method) sourceMethodMap.get(getMethodName);
                    Object value = sourceMethod.invoke(source, null);
                    if (null != value) {
                        destMethod.invoke(dest, value);
                    }
                }
            }
        }
    }
}
