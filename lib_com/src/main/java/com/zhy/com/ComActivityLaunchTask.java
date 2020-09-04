package com.zhy.com;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 只有目标Activity已经在栈中才执行的任务，如果目标Activity不在栈中，则会一直等到目标Activity入栈的时候执行
 */
public abstract class ComActivityLaunchTask
{
    private WeakReference<Activity> mActivity;

    /**
     * 初始化，需要在Application创建的时候初始化
     *
     * @param context
     */
    public final static void init(Context context)
    {
        Manager.INSTANCE.init(context);
    }

    /**
     * 栈中是否存在
     */
    public static final boolean isActive(Class<?> c)
    {
        return Manager.INSTANCE.isActive(c);
    }

    public final void submit()
    {
        Manager.INSTANCE.submit(this);
    }

    /**
     * 优先返回目标Activity，如果目标Activity为null，则返回栈中最顶层的Activity
     *
     * @return
     */
    protected Activity getActivity()
    {
        if (mActivity == null)
            throw new RuntimeException("Not available until execute() method is called");

        final Activity cache = mActivity.get();
        if (cache != null)
            return cache;

        return Manager.INSTANCE.getLastActivity();
    }

    private void executeInternal(Activity activity)
    {
        if (activity == null)
            throw new IllegalArgumentException("activity is null");

        mActivity = new WeakReference<>(activity);
        execute();
    }

    /**
     * 返回目标Activity的Class
     *
     * @return
     */
    protected abstract Class<? extends Activity> getTargetClass();

    /**
     * 执行任务
     */
    protected abstract void execute();

    private static class Manager
    {
        private static final Manager INSTANCE = new Manager();

        private Application mApplication;
        private final List<Activity> mListActivity = new CopyOnWriteArrayList<>();
        private final List<ComActivityLaunchTask> mListTask = new CopyOnWriteArrayList<>();

        public void init(Context context)
        {
            if (mApplication != null)
                return;

            mApplication = (Application) context.getApplicationContext();
            mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks()
            {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState)
                {
                    mListActivity.add(activity);

                    for (ComActivityLaunchTask item : mListTask)
                    {
                        if (activity.getClass() == item.getTargetClass())
                        {
                            mListTask.remove(item);
                            item.executeInternal(activity);
                        }
                    }
                }

                @Override
                public void onActivityStarted(Activity activity)
                {
                }

                @Override
                public void onActivityResumed(Activity activity)
                {
                }

                @Override
                public void onActivityPaused(Activity activity)
                {
                }

                @Override
                public void onActivityStopped(Activity activity)
                {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState)
                {
                }

                @Override
                public void onActivityDestroyed(Activity activity)
                {
                    mListActivity.remove(activity);
                }
            });
        }

        public final void submit(ComActivityLaunchTask task)
        {
            for (Activity activity : mListActivity)
            {
                if (activity.getClass() == task.getTargetClass())
                {
                    task.executeInternal(activity);
                    return;
                }
            }

            mListTask.add(task);
        }

        public final Activity getLastActivity()
        {
            if (mListActivity.isEmpty())
                return null;

            return mListActivity.get(mListActivity.size() - 1);
        }

        public boolean isActive(Class<?> a)
        {
            for (Activity activity : mListActivity)
            {
                if (activity.getClass() ==a)
                {
                    return true;
                }
            }

            return false;
        }
    }
}
