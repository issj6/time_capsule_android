package com.example.timecapsule_2;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

public class ActivityData extends Application {
    public List<Activity> mList = new LinkedList<Activity>();
    private static ActivityData instance;

    public List<Activity> getmList() {
        return mList;
    }

    public void setmList(List<Activity> mList) {
        this.mList = mList;
    }

    private ActivityData(){

    }

    public synchronized static ActivityData getInstance() {
        if (null == instance) {
            instance = new ActivityData();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
