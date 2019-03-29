package b.jakub.bigfilefinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import b.jakub.bigfilefinder.R;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class Storage extends AppCompatActivity {
    private static final int NOTIFICATION_ID = 999;
    public static final String TASK_RESULT = "result";
    private static final String CHANNEL_ID = "notification_channel_id";
    private String rootDirectory = Environment.getExternalStorageDirectory().getPath();
    private StorageAdapter adapter;
    private String curDir;
    private List<String> paths;
    private List<String> items;
    private RecyclerView storageList;
    private Boolean isRootDirectory;
    private int numberOfFiles;
    private SearchAndSort searchAndSortFilesTask;
    private ScrollView loadingScreen;
    private TextView tv_directoryProgress;
    private boolean updateOptionItems;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private boolean updateNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        setChildDirectoriesFromParentDirectory(rootDirectory);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
    }

    public void setChildDirectoriesFromParentDirectory(String rootPath) {
        List<String> files = new ArrayList<>();
        List<String> filesPath = new ArrayList<>();
        File file = new File(rootPath);
        File[] filesArray = file.listFiles();
        isRootDirectory = true;
        items = new ArrayList<>();
        paths = new ArrayList<>();
        if (!rootPath.equals(rootDirectory)) {
            items.add(file.getPath());
            paths.add(file.getParent());
            isRootDirectory = false;
        }
        curDir = rootPath;
        try {
            Arrays.sort(filesArray);
            for (File mfile : filesArray) {
                if (mfile.isDirectory()) {
                    items.add(mfile.getName());
                    paths.add(mfile.getPath());
                } else {
                    files.add(mfile.getName());
                    filesPath.add(mfile.getPath());
                }
            }
            items.addAll(files);
            paths.addAll(filesPath);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setRecyclerViewAndAdapter();
    }

    private void setRecyclerViewAndAdapter() {
        adapter = new StorageAdapter(this, items, paths, isRootDirectory);
        storageList = findViewById(R.id.rvStorage);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        storageList.setLayoutManager(layoutManager);
        storageList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (updateOptionItems)
        {
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (adapter.selectedItems.size() == 0) {
                    Toast.makeText(Storage.this, R.string.check_directories, Toast.LENGTH_SHORT).show();
                } else {
                    buildAlertDialog();
                }
                return true;
            case R.id.action_delete:
                if (adapter.selectedItems.size() == 0) {
                    Toast.makeText(Storage.this, R.string.check_directories_to_delete, Toast.LENGTH_SHORT).show();
                } else {
                    deleteDirectory();
                }
                return true;
            case R.id.action_add:
                showCreateNewFolder();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void buildAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        final EditText editInput = new EditText(this);
        editInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        editInput.setHint(R.string.dialog_hint);
        editInput.setWidth(300);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    numberOfFiles = Integer.parseInt(editInput.getText().toString());
                    if (numberOfFiles > 0) {
                        initializeLoading();
                        updateOptionItems = true;
                        invalidateOptionsMenu();
                        dialog.dismiss();
                        searchAndSortFilesTask = new SearchAndSort();
                        searchAndSortFilesTask.execute();
                    } else {
                        Toast.makeText(Storage.this, R.string.enter_number_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException n) {
                    Toast.makeText(Storage.this, R.string.enter_number_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(editInput);
        builder.show();
    }

    private void setViewsAfterSearchSort() {
        loadingScreen.setVisibility(View.GONE);
        storageList.setVisibility(View.VISIBLE);
    }

    private void initializeLoading() {
        storageList.setVisibility(View.GONE);
        loadingScreen = findViewById(R.id.loading_screen);
        loadingScreen.setVisibility(View.VISIBLE);
        tv_directoryProgress = findViewById(R.id.tv_dirProgress);
        TextView tv_cancel = findViewById(R.id.btn_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSearchAndSortTask();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(searchAndSortFilesTask != null) {
            cancelSearchAndSortTask();
        } else {
            super.onBackPressed();
        }
    }

    private void cancelSearchAndSortTask() {
        searchAndSortFilesTask.cancel(true);
        setViewsAfterSearchSort();
        updateOptionItems = false;
        invalidateOptionsMenu();
        updateNotificationProgressText(getString(R.string.task_canceled));
        searchAndSortFilesTask = null;
    }

    private void deleteDirectory() {
        for (int i : adapter.selectedItems) {
            File deleteDirectories = new File(paths.get(i));
            boolean isDeleted = deleteDirectories.delete();
            if (isDeleted) {
                Toast.makeText(Storage.this, getString(R.string.directory) + deleteDirectories.toString() + getString(R.string.directory_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Storage.this, getString(R.string.directory) + deleteDirectories.toString() + getString(R.string.directory_cannot_delete), Toast.LENGTH_SHORT).show();
            }
        }
        setChildDirectoriesFromParentDirectory(curDir);
    }

    private void showCreateNewFolder() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.custom_dialog_title);
        final EditText editInput = new EditText(this);
        editInput.setInputType(InputType.TYPE_CLASS_TEXT);
        editInput.setHint(R.string.custom_dialog_hint);
        editInput.setWidth(300);
        builder.setPositiveButton(R.string.custom_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editInput.getText().toString();
                File newPath = new File(curDir, text);
                if (!newPath.exists()) {
                    boolean createDir = newPath.mkdirs();
                    Toast.makeText(Storage.this, R.string.custom_dialog_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Storage.this, R.string.custom_dialog_error, Toast.LENGTH_SHORT).show();
                }
                setChildDirectoriesFromParentDirectory(curDir);
            }
        });
        builder.setNegativeButton(R.string.custom_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(editInput);
        builder.show();
    }

    private void buildNotification() {
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.search)
                .setContentTitle(getString(R.string.notification_title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setProgress(numberOfFiles, 0, true);
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationProgressText(String updateText) {
        notificationBuilder.setContentText(updateText)
                .setProgress(0,0,false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private class SearchAndSort extends AsyncTask<Void, String, List<File>> {
        List<File> result;
        Comparator<File> fileComparator;
        @Override
        protected void onPreExecute() {
            result = new ArrayList<>();
            fileComparator = new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.length() < file2.length() ? 1 : -1;
                }
            };
            buildNotification();
        }

        @Override
        protected List<File> doInBackground(Void... voids) {
            for (int i : adapter.selectedItems) {
                File newFile = new File(paths.get(i));
                Stack<File> fileStack = new Stack<>();
                fileStack.push(newFile);
                while (!fileStack.isEmpty()) {
                    File child = fileStack.pop();
                    if (child.isDirectory()) {
                        publishProgress(child.getPath());
                        for (File file : child.listFiles()) {
                            fileStack.push(file);
                        }
                    } else {
                        if (numberOfFiles > result.size()) {
                            result.add(child);

                        } else {
                            Collections.sort(result, fileComparator);
                            if (result.get(result.size() - 1).length() < child.length()) {
                                result.remove(result.size() - 1);
                                result.add(child);
                                updateNotification = true;
                            }
                        }
                    }
                }
            }
            if (!result.isEmpty()) {
                Collections.sort(result, fileComparator);
                return result;
            } else {
                Toast.makeText(Storage.this, R.string.no_files, Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String value : values) {
                tv_directoryProgress.setText(value);
                if(updateNotification) {
                    notificationBuilder.setProgress(0, 0, true)
                            .setContentText(value);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    updateNotification = false;
                }
            }
        }

        @Override
        protected void onPostExecute(List<File> files) {
            setViewsAfterSearchSort();
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            intent.putExtra(TASK_RESULT, (Serializable) files);
            startActivity(intent);
            updateOptionItems = false;
            invalidateOptionsMenu();
            updateNotificationProgressText(getString(R.string.task_completed));
            searchAndSortFilesTask = null;
        }
    }
}
