package com.lemondronor.shrimpdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.lang.RT;

import com.lemondronor.shrimpdroid.R;

public class SplashActivity extends Activity {

    private static boolean firstLaunch = true;
    private static String TAG = "Splash";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (firstLaunch) {
            firstLaunch = false;
            setupSplash();
            loadClojure();
        } else {
            proceed();
        }
    }

    public void setupSplash() {
        setContentView(R.layout.splashscreen);

        TextView appNameView = (TextView)findViewById(R.id.splash_app_name);
        appNameView.setText(R.string.app_name);

        Animation rotation = AnimationUtils.loadAnimation(
            this, R.anim.splash_animation);
        ImageView circleView = (ImageView)findViewById(R.id.splash_drone);
        circleView.startAnimation(rotation);
    }

    public void proceed() {
        startActivity(new Intent("com.lemondronor.shrimpdroid.MAIN"));
        finish();
    }

    public void loadClojure() {
        new Thread(new Runnable(){
                @Override
                public void run() {
                    Symbol CLOJURE_MAIN = Symbol.intern("neko.init");
                    Var REQUIRE = RT.var("clojure.core", "require");
                    REQUIRE.invoke(CLOJURE_MAIN);

                    Log.i(TAG, "Calling neko.init");
                    Var INIT = RT.var("neko.init", "init");
                    INIT.invoke(SplashActivity.this.getApplication());
                    Log.i(TAG, "Called neko.init");

                    // We pre-require a few things here in the
                    // transitive closure of turboshrimp's
                    // dependencies because the Android stack is so
                    // small that requiring turboshrimp will result in
                    // a stack overflow otherwise.
                    //
                    // This is tedious.
                    Log.i(TAG, "Pre-requiring dependencies");
                    String[] preloadModules = new String[]{
                        "clj-tuple",
                        "potemkin",
                        "manifold.stream",
                        "gloss.core.formats",
                        "gloss.io"};
                    for (int i = 0; i < preloadModules.length; i++) {
                        Log.i(TAG, "Loading " + preloadModules[i]);
                        REQUIRE.invoke(Symbol.intern(preloadModules[i]));
                        Log.i(TAG, "Loaded " + preloadModules[i]);
                    }
                    try {
                        Class.forName(
                            "com.lemondronor.shrimpdroid.MainActivity");
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Failed loading MainActivity", e);
                    }

                    proceed();
                }
            }).start();
    }
}
