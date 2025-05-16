package com.lsposed.badlock;

import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.os.Build;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        try {
            if (lpparam.packageName.equals("com.samsung.android.honeyboard")) {
                XposedBridge.log("DCCG: Hooking Samsung Keyboard");

                // Hook SystemProperties.get()
                Class<?> clazz = XposedHelpers.findClass(
                    "android.os.SystemProperties", lpparam.classLoader
                );

                XposedHelpers.findAndHookMethod(clazz, "get", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if ("ro.build.characteristics".equals(param.args[0])) {
                            param.setResult("phone");
                            XposedBridge.log("DCCG: Spoofed ro.build.characteristics to phone");
                        } else if ("ro.cmc.device_type".equals(param.args[0])) {
                            param.setResult("pd");
                            XposedBridge.log("DCCG: Spoofed ro.cmc.device_type to pd");
                        }
                    }
                });

                // Hook hasSystemFeature
                XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager",
                    lpparam.classLoader,
                    "hasSystemFeature",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if ("com.samsung.feature.device_category_tablet".equals(param.args[0])) {
                                param.setResult(false);
                                XposedBridge.log("DCCG: Spoofed tablet system feature to false");
                            }
                        }
                    }
                );

            } else if (lpparam.packageName.equals("com.kakao.talk")) {
                XposedBridge.log("DCCG: Hooking KakaoTalk");

                // Hook SystemProperties.get()
                Class<?> clazz = XposedHelpers.findClass(
                    "android.os.SystemProperties", lpparam.classLoader
                );

                XposedHelpers.findAndHookMethod(clazz, "get", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if ("ro.product.model".equals(param.args[0])) {
                            param.setResult("SM-T975N");
                            XposedBridge.log("DCCG: Spoofed ro.product.model to SM-T975N for KakaoTalk");
                        }
                    }
                });

                // Hook Build.MODEL directly
                try {
                    XposedHelpers.setStaticObjectField(Build.class, "MODEL", "SM-T975N");
                    XposedBridge.log("DCCG: Spoofed Build.MODEL to SM-T975N for KakaoTalk");
                } catch (Throwable t) {
                    XposedBridge.log("DCCG: Failed to spoof Build.MODEL - " + t.getMessage());
                }
            }

        } catch (Throwable t) {
            XposedBridge.log("DCCG: Error hooking - " + t.getMessage());
        }
    }
}