package com.example.zes.packages;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class UninstallAsyncTask extends AsyncTask<AppInfo, Void, Boolean> {
    private final WeakReference<UninstallListener> uninstallListenerWeakReference;

    public UninstallAsyncTask(UninstallListener uninstallListener) {
        super();
        this.uninstallListenerWeakReference = new WeakReference<>(uninstallListener);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(AppInfo... params) {
        AppInfo appInfo = params[0];

        if (appInfo.isSystem()) {
            return RootHelper.uninstallSystem(appInfo.getApkFile());
        } else {
            return RootHelper.uninstall(appInfo.getPackageName());
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // Получаем сильную ссылку
        UninstallListener uninstallListener = uninstallListenerWeakReference.get();

        // Проверяем на null
        if (uninstallListener != null) {

            // Вызываем соответствующий метод
            if (result) {
                uninstallListener.onUninstalled();
            } else {
                uninstallListener.onFailed();
            }
        }
    }

    public interface UninstallListener {
        void onUninstalled();

        void onFailed();
    }



}