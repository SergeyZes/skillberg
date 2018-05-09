package com.example.zes.packages;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppManager {
    private final PackageManager packageManager;

    public AppManager(Context context) {
        packageManager = context.getPackageManager();
    }

    public List<AppInfo> getInstalledApps() {
        List<AppInfo> installedApps = new ArrayList<>();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (PackageInfo installedPackage : installedPackages) {
            AppInfo appInfo = new AppInfo(
                    installedPackage.packageName, // Имя пакета
                    installedPackage.versionCode, // Код версии
                    installedPackage.versionName, // Имя версии
                    installedPackage.applicationInfo.loadLabel(packageManager).toString(), // Имя приложения
                    installedPackage.applicationInfo.loadIcon(packageManager), // Иконка приложения
                    ((installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1), // Системное ли приложение
                    new File(installedPackage.applicationInfo.sourceDir) // Путь до APK
            );
            installedApps.add(appInfo);
        }

        return installedApps;
    }

}
