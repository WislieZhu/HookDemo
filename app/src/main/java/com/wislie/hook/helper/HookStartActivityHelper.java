package com.wislie.hook.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * author : Wislie
 * e-mail : 254457234@qq.comn
 * date   : 2020/3/27 6:37 PM
 * desc   : startActivity拦截
 * version: 1.0
 */
public class HookStartActivityHelper {

    private static String TAG = "HookStartActivityHelper";

    //1.获取到IActivityManagerSingleton对象
    //2.获取到IActivityManager的Field
    //3.改变IActivityManagerSingleton对象中 IActivityManager的属性值

    /**
     * 判断是否登录了
     */
    public static boolean isLogin = false;

    /**
     * 启动Activity
     */
    public static void hookStartActivity(Context context) {
        AndroidBase androidBase = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            androidBase = new AndroidQ();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            androidBase = new AndroidO();
        } else { //小于sdk 26
            androidBase = new AndroidOLower();
        }
        androidBase.build(context);
    }

    abstract static class AndroidBase {

        abstract Field createActivityManagerSingletonField();

        Object createActivityManagerSingleton() {
            //因为是静态变量,可以get直接得到属性值
            Field iActivityManagerSingletonField = createActivityManagerSingletonField();
            Object iActivityManagerSingleton = null;
            try {
                iActivityManagerSingleton = iActivityManagerSingletonField.get("");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return iActivityManagerSingleton;
        }

        abstract Class<?> createActivityManagerClass();

        public void build(final Context context) {
            Object iActivityManagerSingleton = createActivityManagerSingleton();

            Class<?> activityManagerClass = createActivityManagerClass();
            //得到IActivityManager属性所在的class
            Class<?> singletonClass = null;
            try {
                singletonClass = Class.forName("android.util.Singleton");
                //得到IActivityManager属性
                Field mInstanceField = singletonClass.getDeclaredField("mInstance");
                mInstanceField.setAccessible(true);
                //singletonClass获取get()方法
                Method getMethod = singletonClass.getDeclaredMethod("get");
                getMethod.setAccessible(true);
                //获取mInstanceField的值
                final Object mInstance = getMethod.invoke(iActivityManagerSingleton);
                Object proxy = Proxy.newProxyInstance(singletonClass.getClassLoader(), new Class[]{activityManagerClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Log.i(TAG, "invoke: methodName=" + method.getName());
                        if ("startActivity".equals(method.getName())) {
                            if (!isLogin) {
                                Toast.makeText(context.getApplicationContext(), "未登录", Toast.LENGTH_SHORT).show();
                                Intent intent = null;
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        intent = (Intent) args[i];
                                        break;
                                    }
                                }
                                if (intent != null) {
                                    intent.setClassName("com.wislie.hook",
                                            "com.wislie.hook.LoginActivity");
                                }
                            }
                        }
                        return method.invoke(mInstance, args);
                    }
                });
                //将代理对象代替mInstance对象
                mInstanceField.set(iActivityManagerSingleton, proxy);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * android 26以下
     */
    static class AndroidOLower extends AndroidBase {

        @Override
        Field createActivityManagerSingletonField() {
            // 得到ActivityManagerNative的class
            Class<?> activityManagerNativeClass = null;
            try {
                activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
                //获取gDefault属性
                Field iActivityManagerSingletonField = activityManagerNativeClass.getDeclaredField("gDefault");
                iActivityManagerSingletonField.setAccessible(true);
                return iActivityManagerSingletonField;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        Class<?> createActivityManagerClass() {
            //获取IActivityManager接口
            try {
                Class<?> activityManagerClass = Class.forName("android.app.IActivityManager");
                return activityManagerClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Android [26,29)
     */
    static class AndroidO extends AndroidBase {

        @Override
        Field createActivityManagerSingletonField() {
            //得到IActivityManagerSingleton属性
            Field iActivityManagerSingletonField = null;
            try {
                iActivityManagerSingletonField = ActivityManager.class.getDeclaredField("IActivityManagerSingleton");
                iActivityManagerSingletonField.setAccessible(true);
                return iActivityManagerSingletonField;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        Class<?> createActivityManagerClass() {
            //获取IActivityManager接口
            try {
                Class<?> activityManagerClass = Class.forName("android.app.IActivityManager");
                return activityManagerClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Android 29及以上
     */
    static class AndroidQ extends AndroidBase {

        @Override
        Field createActivityManagerSingletonField() {
            // 得到ActivityTaskManager的class
            Class<?> activityTaskManagerClass = null;
            Field iActivityManagerSingletonField = null;
            try {
                activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");
                //得到IActivityTaskManagerSingleton属性
                iActivityManagerSingletonField = activityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
                iActivityManagerSingletonField.setAccessible(true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return iActivityManagerSingletonField;
        }

        @Override
        Class<?> createActivityManagerClass() {
            //获取IActivityTaskManager接口
            try {
                Class<?> activityManagerClass = Class.forName("android.app.IActivityTaskManager");
                return activityManagerClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


