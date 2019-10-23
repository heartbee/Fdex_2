package com.example.fdex_20;

import android.app.Application;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

public class ClassUtils {
    public static final String TAG1="fdex20";

    private static Object getObjectField(Object object, String fieldName) {
        Class clazz = object.getClass();
        while (!clazz.getName().equals(Object.class.getName())) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static ArrayList<String> getClassNameList(ClassLoader classLoader) {
        ArrayList<String> classNameList = new ArrayList<>();
        try {
            Object pathList = getObjectField(classLoader, "pathList");
            Object dexElements = getObjectField(pathList, "dexElements");
            int dexElementsLength = Array.getLength(dexElements);
            for (int i = 0; i < dexElementsLength; i++) {
                Object dexElement = Array.get(dexElements, i);
                DexFile dexFile = (DexFile) getObjectField(dexElement, "dexFile");
                Enumeration<String> enumerations = dexFile.entries();
                while (enumerations.hasMoreElements()) {
                    String classname = enumerations.nextElement();
                    classNameList.add(classname);
                }
            }
        } catch (Exception e) {
            Log.e(TAG1, e.getMessage());
        }
        Collections.sort(classNameList);
        return classNameList;
    }
    public static boolean loadAllClass(Application mInitialApplication){
        Log.d(TAG1, "开始获取所有的类列表");
        List<String> classNameList = getClassNameList(mInitialApplication.getBaseContext().getClassLoader());
        Log.d(TAG1, "获取类列表结束，有" + classNameList.size() + "个类");
        for(String name : classNameList) {
            try {
                if(name.startsWith("android") || name.startsWith("org"))
                    continue;
                Class dclazz = mInitialApplication.getBaseContext().getClassLoader().loadClass(name);
            } catch (Throwable e) {

            }
            System.gc();
        }
        Log.d(TAG1, "已经完成类的加载");
        return true;

    }

    public static void loadAllClass2(ClassLoader classLoader){
        Log.d(TAG1, "开始获取所有的类列表");
        List<String> classNameList = getClassNameList(classLoader);
        Log.d(TAG1, "获取类列表结束，有" + classNameList.size() + "个类");
        for(String name : classNameList) {
            try {
                if(name.startsWith("android") || name.startsWith("org"))
                    continue;
                Class dclazz = classLoader.loadClass(name);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            System.gc();
        }
        Log.d(TAG1, "已经完成类的加载");

    }

    public static boolean dexinlist(List<byte[]> list,byte[] dexs){
        for(byte[] dex:list){
            if(dex.equals(dexs)){
                return true;
            }
        }
        return false;

    }
}
