package com.example.zes.packages;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_APK = 1;

    private AppManager appManager;
    private AppsAdapter appsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final UninstallAsyncTask.UninstallListener uninstallListener = new UninstallAsyncTask.UninstallListener() {
        @Override
        public void onUninstalled() {
            Toast.makeText(MainActivity.this, "Удалено!", Toast.LENGTH_LONG).show();
            reloadApps();
        }

        @Override
        public void onFailed() {
            Toast.makeText(MainActivity.this, "Не удалось удалить!", Toast.LENGTH_LONG).show();
            reloadApps();
        }
    };

    private InstallAsyncTask.InstallListener installListener = new InstallAsyncTask.InstallListener() {
        @Override
        public void onInstalled() {
            Toast.makeText(MainActivity.this, "Установлено успешно!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed() {
            Toast.makeText(MainActivity.this, "Не удалось установить!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void needStandardInstall(String fileName){
            try {

                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(MainActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider", new File(fileName));
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(new File(fileName));
                }

                installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Создаст новый процесс

                startActivity(installIntent);
                Toast.makeText(MainActivity.this, "Установлено успешно!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Не удалось установить! "+e.toString(), Toast.LENGTH_LONG).show();

            }

        }

    };





    private void startFilePickerActivity() {
        Intent intent = new Intent(this, FilePickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PICK_APK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                appsAdapter.setQuery(newText.toLowerCase().trim());
                appsAdapter.notifyDataSetChanged();

                return true;
            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.install_item:
                startFilePickerActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);


        appManager = new AppManager(this);

        appsAdapter = new AppsAdapter();

        RecyclerView recyclerView = findViewById(R.id.apps_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(appsAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        recyclerView.addItemDecoration(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        reloadApps();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_APK && resultCode == RESULT_OK) {
            String apkPath = data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH);
            Log.i(TAG, "APK: " + apkPath);
            startAppInstallation(apkPath);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showToast() {
        Toast toast = Toast.makeText(this, "Hello", Toast.LENGTH_LONG);
        toast.show();

    }

    private void startAppInstallation(String apkPath) {

        installWithRoot(apkPath);

//        Intent installIntent = new Intent(Intent.ACTION_VIEW);
//        Uri uri;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            uri = FileProvider.getUriForFile(this,
//                    BuildConfig.APPLICATION_ID + ".provider", new File(apkPath));
//            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        } else {
//            uri = Uri.fromFile(new File(apkPath));
//        }
//
//        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
//        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Создаст новый процесс
//
//        startActivity(installIntent);
    }

    private void reloadApps() {
        List<AppInfo> installedApps = appManager.getInstalledApps();
        appsAdapter.setApps(installedApps);
        appsAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private final SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            reloadApps();
        }
    };
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
    private void startAppUninstallation(AppInfo appInfo) {
        uninstallWithRoot(appInfo);
    }

    private void uninstallWithRoot(AppInfo appInfo) {
        UninstallAsyncTask uninstallAsyncTask = new UninstallAsyncTask(uninstallListener);
        uninstallAsyncTask.execute(appInfo);
    }

    private void installWithRoot(String apkPath){
        InstallAsyncTask installAsyncTask=new InstallAsyncTask(installListener);
        installAsyncTask.execute(apkPath);
    }


    private final ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.Callback() {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            AppInfo appInfo = (AppInfo) viewHolder.itemView.getTag();
            startAppUninstallation(appInfo);
        }

    };


}
