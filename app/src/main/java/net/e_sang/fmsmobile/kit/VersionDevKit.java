/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.e_sang.fmsmobile.kit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import android.widget.Toast;

import net.e_sang.fmsmobile.BuildConfig;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.ui.SplashActivity;

import okhttp3.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class VersionDevKit implements TelKit.OnResultListener {
    private Activity mActivity = null;
    private OkHttpClient mHttpClient = new OkHttpClient();
    private AlertDialog mAlertDialog = null;
    private DownloadAsyncTask mDownloadTask = null;
    private ProgressDialog mProgressDialog = null;

    private OnResultListener mOnResultListener = null;

    public interface OnResultListener {
        public abstract void onResult(boolean result);
    }

    public VersionDevKit(Activity activity, OnResultListener listener) {
        super();
        // TODO Auto-generated constructor stub
        mActivity = activity;
        mOnResultListener = listener;

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    public void checkVersion() {
        HashMap<String, String> body = new HashMap<>();
        body.put("device_flag", "A");
        new TelKit(mActivity, VersionDevKit.this).request(TelKit.URL_API_GET_VERSION, body);
    }

    public void download() {
        if (mDownloadTask == null) {
            mDownloadTask = new DownloadAsyncTask();
            mDownloadTask.execute();
        } else {
            if (mDownloadTask.getStatus() == AsyncTask.Status.PENDING) {
                mDownloadTask.execute();
            } else if (mDownloadTask.getStatus() == AsyncTask.Status.FINISHED) {
                if (mDownloadTask.isCancelled() == false)
                    mDownloadTask.cancel(true);
                mDownloadTask = new DownloadAsyncTask();
                mDownloadTask.execute();
            }
        }
    }

    protected void showAlertIfNeedUpdate(String newVersion, String memo) {
        if (newVersion == null || newVersion.isEmpty())
            return;

        String appVersion = Kit.getPackageVersionName(mActivity);
        Kit.log(Kit.LogType.VALUE, "appVersion = " + appVersion);
        Kit.log(Kit.LogType.VALUE, "newVersion = " + newVersion);
        if (appVersion != null && !appVersion.isEmpty()) {
            if (appVersion.compareTo(newVersion) < 0) {
                if (mAlertDialog != null) {
                    if (mAlertDialog.isShowing()) {
                        mAlertDialog.dismiss();
                    }
                }

                String appName = Kit.getAppName(mActivity);
                String msg = String.format(
                        "'%s' 새 버전이 있습니다. 업데이트하겠습니까?\n\n현재 버전: %s\n새 버전: %s\n수정사항: %s\n\n" +
                                mActivity.getResources().getString(R.string.str_update_msg),
                        appName, appVersion, newVersion, memo);

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(msg);
                builder.setCancelable(false);
                builder.setPositiveButton("업데이트",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                boolean available = Kit.checkAvailableStorage(200 * 1024 * 1024);    // 200 MB 미만이면 X
                                if (available == false) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                    builder.setMessage("앱 설치 저장 공간이 부족합니다.\n불필요한 파일 삭제 후 다시 시도해 주세요.");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("확인",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    mActivity.finish();
                                                }
                                            });
                                    builder.show();
                                } else {
                                    download();
                                }
                            }
                        });
                builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mOnResultListener != null) {
                                    mOnResultListener.onResult(true);
                                }
                            }
                        });
                builder.setNeutralButton("다운로드 링크", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intentUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.URL_DOWNLOAD_APK));
                        mActivity.startActivity(intentUrl);
                        mActivity.finishAffinity();
                    }
                });
                mAlertDialog = builder.show();
            } else {
                if (mOnResultListener != null)
                    mOnResultListener.onResult(true);
            }
        }
    }

    // TelKit.OnResultListener
    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc == false)
            return;

        if (result.mRequestUrl.equals(TelKit.URL_API_GET_VERSION)) {
            if (!result.mResponse.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONObject versionObj = json.optJSONObject("version");
                        if (versionObj != null) {
                            String version = versionObj.optString("VERSION");
                            String memo = versionObj.optString("MEMO");
                            if (Kit.isNotNullNotEmpty(version)) {
                                showAlertIfNeedUpdate(version, memo);
                                return;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mOnResultListener != null) {
                mOnResultListener.onResult(false);
            }
        }
    }

    public class DownloadAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            //mProgressDialog.setTitle("저장 공간 확인");
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            mProgressDialog.setTitle("다운로드");
            mProgressDialog.show();

//            boolean available = Kit.checkAvailableStorage(100 * 1024 * 1024);    // 100 MB 미만이면 X
//            if (available == false) {
//                Toast.makeText(mActivity, "저장 공간이 부족합니다.", Toast.LENGTH_SHORT).show();
//                cancel(true);
//            } else {
//                mProgressDialog.setTitle("다운로드");
//            }

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            // TODO Auto-generated method stub
            trustAllHosts();

            URL url;
            HttpsURLConnection c;
            File file;
            FileOutputStream fos;
            InputStream is;
            try {
                url = new URL(TelKit.URL_DOWNLOAD_APK);
                c = (HttpsURLConnection) url.openConnection();
                c.connect();
                int resCode = c.getResponseCode();
                Kit.log("resCode = " + resCode);

                File outputFile = getDownloadFile();
                if (outputFile == null)
                    cancel(true);

                fos = new FileOutputStream(outputFile);
                is = c.getInputStream();
                int fileSize = c.getContentLength();
                mProgressDialog.setMax(fileSize);
                byte[] buffer = new byte[1024];
                int len1 = 0, total = 0;

                int progress = 0;
                publishProgress(progress);
                while ((len1 = is.read(buffer)) != -1) {
                    if (isCancelled()) {
                        break;
                    }

                    fos.write(buffer, 0, len1);
                    total += len1;

                    progress = (int) ((float) total / (float) fileSize * 100.0f);
                    publishProgress(total);
                }
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            Toast.makeText(mActivity, "다운로드...취소!", Toast.LENGTH_SHORT).show();

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            if (mOnResultListener != null)
                mOnResultListener.onResult(false);

            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            if (result) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PackageManager packageManager = mActivity.getPackageManager();
                    if (packageManager.canRequestPackageInstalls()) {
                        installAPK(mActivity);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage("앱을 설치하기 위해 설정 화면의\n'앱 설치 허용'을 설정해 주시기 바랍니다.\n\n설정화면으로 이동하시겠습니까?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                        intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
                                        mActivity.startActivityForResult(intent, SplashActivity.REQUEST_CODE_GET_UNKNOWN_APP_SOURCES);
                                    }
                                });
                        builder.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
//                                        mActivity.finishAffinity();
                                        if (mOnResultListener != null)
                                            mOnResultListener.onResult(true);
                                    }
                                });
                        builder.show();
                    }
                } else {
                    installAPK(mActivity);
                }
            } else {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage("파일을 다운로드하지 못했습니다.");
                builder.setCancelable(false);
                builder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mOnResultListener != null)
                                    mOnResultListener.onResult(true);
                            }
                        });
                builder.show();
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... args) {
            int progress = args[0];
            if (mProgressDialog != null) {
                if (mProgressDialog.isIndeterminate())
                    mProgressDialog.setIndeterminate(false);

                mProgressDialog.setProgress(progress);
            }

            super.onProgressUpdate(args);
        }
    }

    public File getDownloadFile() {
        File tempFile = null;
        try {

            File file = new File(getSaveFolderPath());
            if (!file.exists()) {
                file.mkdirs();
            }
            tempFile = new File(file, TelKit.APK_FILE_NAME);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }

    protected static String getSaveFolderPath() {
        return String.format("%s%s%s",
                Environment.getExternalStorageDirectory().getAbsolutePath(),
                File.separator,
                BuildConfig.APP_FLAVOR);
    }

    public static void installAPK(Activity activity) {
        try {
            String filePath = String.format("%s%s%s", getSaveFolderPath(), File.separator, TelKit.APK_FILE_NAME);
            File file = new File(filePath);
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }
            activity.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        activity.finish();
    }

    // SSL 인증서 확인 무력화, 보안 취약점 발생 (APK 배포일 때만 사용할 것)
    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
