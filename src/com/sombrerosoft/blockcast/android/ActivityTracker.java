package com.sombrerosoft.blockcast.android;

public class ActivityTracker {

    private static ActivityTracker instance = new ActivityTracker();
    private boolean resumed;
    private boolean inForeground;

    private ActivityTracker() { /*no instantiation*/ }

    public static ActivityTracker getInstance() {
        return instance;
    }

    public void onActivityStarted() {
        if (!inForeground) {
            /* 
             * Started activities should be visible (though not always interact-able),
             * so you should be in the foreground here.
             *
             * Register your location listener here. 
             */
            inForeground = true;
        }
    }

    public void onActivityResumed() {
        resumed = true;
    }

    public void onActivityPaused() {
        resumed = false;
    }

    public void onActivityStopped() {
        if (!resumed) {
            /* If another one of your activities had taken the foreground, it would
             * have tripped this flag in onActivityResumed(). Since that is not the
             * case, your app is in the background.
             *
             * Unregister your location listener here.
             */
            inForeground = false;
        }
    }
}
