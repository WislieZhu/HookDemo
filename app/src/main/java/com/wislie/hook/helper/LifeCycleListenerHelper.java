package com.wislie.hook.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

/**
 * author : Wislie
 * e-mail : 254457234@qq.comn
 * date   : 2020/3/24 2:39 PM
 * desc   : 生命周期
 * version: 1.0
 */
public class LifeCycleListenerHelper {

    private static String TAG = "LifeCycleListenerHelper";

    public static void hook(final Activity activity) {
        try {
            //获取到Application中的mActivityLifecycleCallbacks字段
            Field activityLifecycleCallbacksField = Application.class.getDeclaredField("mActivityLifecycleCallbacks");
            //保证访问权限
            activityLifecycleCallbacksField.setAccessible(true);
            //获取当前activity的application对象
            Application application = activity.getApplication();

            //和Activity生命周期相关的持有者集合,通过反射机制获得
            ArrayList<Application.ActivityLifecycleCallbacks> callbacksList =
                    (ArrayList<Application.ActivityLifecycleCallbacks>) activityLifecycleCallbacksField.get(application);

            //获取activityLifecycleCallbacksClass类
            Class<?> activityLifecycleCallbacksClass = Class.forName("android.app.Application$ActivityLifecycleCallbacks");
            //创建一个ActivityLifecycleCallbacks的实现对象
            final VirtualActivityLifecycleCallbacks callbacks = new VirtualActivityLifecycleCallbacks();
            //创建一个ActivityLifecycleCallbacks的代理对象
            Object proxyActivityLifecycleCallbacks =
                    Proxy.newProxyInstance(activityLifecycleCallbacksClass.getClassLoader(),
                            new Class[]{activityLifecycleCallbacksClass},
                            new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    Log.i(TAG, activity.getClass().getSimpleName() + "---" + method.getName());
                                    return method.invoke(callbacks, args);
                                }
                            });
            //将代理对象添加到callbacksList集合中
            callbacksList.add((Application.ActivityLifecycleCallbacks) proxyActivityLifecycleCallbacks);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建ActivityLifecycleCallbacks的实现类
     */
    private static class VirtualActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

}
