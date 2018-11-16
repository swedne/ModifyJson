package com.example.administrator.adapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    JSONArray mJsonArr;
    private ArrayList<String> arrayList = new ArrayList<>();
    private JSONArray jsonArray;
    private TextView tvChange;
    private JSONObject jsonP;
    private JSONArray jsonCs;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查是否已经给了权限
            int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE);
            if (checkpermission != PackageManager.PERMISSION_GRANTED) {//没有给权限
                Log.e("permission", "动态申请");
                //   参数分别是当前活动，权限字符串数组，requestcode
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, 1);
            }
        }
        Log.e("path", Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.e("path", Environment.getExternalStorageDirectory().getPath());
        mWebView = findViewById(R.id.mWebView);
        tvChange = findViewById(R.id.tv_change);
        tvChange.setOnClickListener(v -> {
            changeConfig();
        });
        findViewById(R.id.tv_copy).setOnClickListener(V -> {
            Util.copyFileFromAssets(getApplicationContext(), "config.json", getSDCardPath() + File.separator + "config.json");
        });
//        getIndexFile();
        initJsonData();
        initJsonDatas();

    }

    private void changeConfig() {
        analyzeJson("carou", "slide", jsonArray);
        analyzeJson("data", "myimage.jpg", jsonArray);
        Log.e("TAG", jsonArray.toString());
//        for (int j = 0; j < jsonCs.length(); j++) {
//            String imgName = null;// 获取每张图片
//            try {
//                imgName = jsonCs.getString(j);
//                if (imgName.equals("1.png")) {
//                    jsonCs.put(j, "myimge.png");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            arrayList.add(imgName);
//        }

    }

    private void initJsonData() {
        String strConfig = getAssets(this, "config.json");
        String substring = strConfig.substring(strConfig.indexOf("=") + 1, strConfig.length());
        try {
            jsonArray = new JSONArray(substring);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //
    private void initJsonDatas() {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonP = jsonArray.getJSONObject(i);
                jsonCs = jsonP.getJSONArray("data");
                for (int j = 0; j < jsonCs.length(); j++) {
                    String imgName = jsonCs.getString(j);// 获取每张图片
                    arrayList.add(imgName);
                }
            }
        } catch (Exception ex) {
            Log.e("TAG", "解析异常");
        }
    }

    /**
     * 解析Json数据.
     *
     * @param key    更换数据key
     * @param value  更换Value
     * @param object 解析对象
     */
    public void analyzeJson(String key, Object value, Object object) {
        try {
            if (object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) object;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    analyzeJson(key, value, jsonObject);
                }
            } else if (object instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) object;
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String jsonKey = iterator.next().toString();
                    Object ob = jsonObject.get(jsonKey);
                    if (ob != null) {
                        if (ob instanceof JSONArray) {
                            if (jsonKey.equals(key)) {
                                ((JSONArray) ob).put(position, value);
                            } else {
                                analyzeJson(key, value, ob);
                            }
                        } else if (ob instanceof JSONObject) {
                            analyzeJson(key, value, ob);
                        } else {
                            if (jsonKey.equals(key)) {
                                jsonObject.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static String getAssets(Activity mActivity, String fileName) {
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = mActivity.getAssets().open(fileName);
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "UTF-8"));
            }
            is.close();
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }


    private void getIndexFile() {
        String filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "海报图片演示";
        File file = new File(filePath);
        // 先判断这个文件是否存在
        if (file.exists()) {
            List<Info> list = new ArrayList<Info>();
            GetFilePath(list, file);//开始扫描此文件夹下想要的文件
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();

        }
    }

    private String getSDCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        }
        return sdDir.toString();

    }

    private void GetFilePath(final List<Info> list, File file) {

        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                int i = name.indexOf('.');
                if (i != -1) {
                    if (name.equals("index.html")) {
                        //得到文件路径
                        String file_path = file.getAbsolutePath();
                        mWebView.loadUrl(file_path);
                        return true;
                    }
                } else if (file.isDirectory()) {//如果此文件夹存在子目录
                    //继续递归搜索子目录，如果注释，则只搜索当前目录
                    GetFilePath(list, file);
                }
                return false;
            }
        });
    }

    public class Info {
        String name;
        String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
