package com.np.ioc;


import android.app.Activity;
import android.view.View;

public class ViewInjector {

    private static final String PROXY = "$$ViewInject";

    public static void injectView(Activity activity) {
        ViewInject proxyActivity = findProxyActivity(activity);
        proxyActivity.inject(activity, activity);
    }

    public static void injectView(Object host, View view) {
        ViewInject proxyActivity = findProxyActivity(host);
        proxyActivity.inject(host, view);
    }

    /** 生成代理 ViewInject 类. */
    private static ViewInject findProxyActivity(Object activity) {
        Class<?> aClass = activity.getClass();
        try {
            Class<?> injectClass = Class.forName(aClass.getName() + PROXY);
            return (ViewInject) injectClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.",
                activity.getClass().getSimpleName() + PROXY));
    }

}
