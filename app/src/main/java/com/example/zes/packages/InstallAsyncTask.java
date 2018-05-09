package com.example.zes.packages;


import android.os.AsyncTask;

import java.io.File;
import java.lang.ref.WeakReference;

public class InstallAsyncTask extends AsyncTask<String, Void, Integer> {
    private final WeakReference<InstallListener> installListenerWeakReference;

    public static final int INSTALL_IN_ROOT_OK = 1;
    public static final int INSTALL_IN_ROOT_BAD = 2;
    public static final int NEED_STANDARD_INSTALL = 3;
    private String fileName;


    public InstallAsyncTask(InstallListener installListener) {
        super();
        this.installListenerWeakReference = new WeakReference<>(installListener);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        fileName=params[0];
        return RootHelper.InstallAPK(params[0]);

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        // Получаем сильную ссылку
        InstallListener installListener = installListenerWeakReference.get();

        // Проверяем на null
        if (installListener != null) {

            switch (result)
            {
                case INSTALL_IN_ROOT_OK:
                    installListener.onInstalled();
                    break;
                case INSTALL_IN_ROOT_BAD:
                    installListener.onFailed();
                    break;
                default:
                    installListener.needStandardInstall(fileName);

            }

        }
    }

    public interface InstallListener {
        void onInstalled();
        void onFailed();
        void needStandardInstall(String fileName);
    }



}