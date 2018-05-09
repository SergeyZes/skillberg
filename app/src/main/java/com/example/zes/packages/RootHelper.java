package com.example.zes.packages;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class RootHelper {
    public static boolean uninstall(String packageName) {
        String output = executeCommand("pm uninstall " + packageName);
        if (output != null && output.toLowerCase().contains("success")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean uninstallSystem(File appApk) {
        executeCommand("mount -o rw,remount /system");
        executeCommand("rm " + appApk.getAbsolutePath());
        executeCommand("mount -o ro,remount /system");

        // Проверяем, удалился ли файл
        String output = executeCommand("ls " + appApk.getAbsolutePath());

        if (output != null && output.trim().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean CheckRoot(){
        String output = executeCommand("id");
        if (output != null && output.toLowerCase().contains("uid=0")) {
            return true;
        } else {
            return false;
        }

    }

    public static int InstallAPK(String appApk){
        if (CheckRoot()){
            String output = executeCommand("pm install " + appApk);
            if (output != null && output.toLowerCase().contains("success")) {
                return InstallAsyncTask.INSTALL_IN_ROOT_OK;
            } else {
                return InstallAsyncTask.INSTALL_IN_ROOT_BAD;
            }

        } else {
           return InstallAsyncTask.NEED_STANDARD_INSTALL;
        }
    }

    @Nullable
    private static String executeCommand(String command) {
        List<String> stdout = Shell.SU.run(command);
        if (stdout == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : stdout) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }
}
