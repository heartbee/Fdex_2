package com.example.fdex_20;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.example.fdex_20.ClassUtils.dexinlist;

public class DexHook implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		// TODO Auto-generated method stub
		XSharedPreferences preferences = new XSharedPreferences("com.example.fdex_20");
		final String packageName = preferences.getString("packageName", null);
		String appName = preferences.getString("appName", null);
		if(TextUtils.isEmpty(packageName))
			return;
		if(packageName.equals(lpparam.packageName)){
			System.out.println("app " + appName + " launch");

            /**
             * 第一种获取并加载所有的类
             */
//            Class<?> activityThread=Class.forName("android.app.ActivityThread");
//			if(activityThread!=null){
//			    Log.e("fdex20","class is found");
//                final Field mInitialApplication=activityThread.getDeclaredField("mInitialApplication");
//                mInitialApplication.setAccessible(true);
//                XposedHelpers.findAndHookMethod(activityThread, "currentActivityThread", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Object object=param.getResult();
//                        Application application=(Application) (mInitialApplication.get(object));
//                        ClassUtils.loadAllClass(application);
//                    }
//                });
//            }

            /**
             * 第二种获取并加载所有的类
             */
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final ClassLoader classLoader=((Context)param.args[0]).getClassLoader();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ClassUtils.loadAllClass2(classLoader);
                            dumpdex(lpparam);
                        }
                    }).start();

                }
            });


		}
	}

	public static void dumpdex(final LoadPackageParam lpparam){
	    try{
            Class<?> dexClazz = Class.forName("com.android.dex.Dex");
            Class<?> clazz = Class.forName("java.lang.Class");
            final Method getDexMd = clazz.getDeclaredMethod("getDex");
            final Method getBytesMd = dexClazz.getDeclaredMethod("getBytes");
            XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // TODO Auto-generated method stub
                    if(param.hasThrowable())
                        return;
                    Class<?> clazz = (Class<?>) param.getResult();
                    if(clazz != null){
                        System.out.println(clazz.getName());
                        Object dex = getDexMd.invoke(clazz);
                        List<byte[]> list=new ArrayList<byte[]>();
                        list.add("wocao".getBytes());
                        byte[] dexBytes = (byte[]) getBytesMd.invoke(dex);
                        if(dexBytes != null&& !dexinlist(list,dexBytes)){
                            File dexFile = new File("/data/data/"+lpparam.packageName, "dump_dex_"+dexBytes.length+".dex");
                            list.add(dexBytes);
                            if(dexFile.exists()){
                                return;
                            }else{
                                FileOutputStream fos = new FileOutputStream(dexFile);
                                fos.write(dexBytes);
                                fos.close();
                            }
                        }
                    }
                }
            });

        }catch (Exception e){

        }
    }

}
