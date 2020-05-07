package net.jejer.hipda.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;

import java.io.IOException;

/**
 * dialog for login
 * Created by GreenSkinMonster on 2015-04-18.
 */
public class LoginDialog extends Dialog {

    private static boolean isShown = false;

    private Context mCtx;
    private HiProgressDialog progressDialog;
    private Handler mHandler;
//    ImageView ivSecCodeVerify;

    WebView gwv;

    private LoginDialog(Context context) {
        super(context);
        mCtx = context;
    }

    public static LoginDialog getInstance(Context context) {
        if (!isShown) {
            isShown = true;
            return new LoginDialog(context);
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_login, null);

        final EditText etUsername = (EditText) view.findViewById(R.id.login_username);
        final EditText etPassword = (EditText) view.findViewById(R.id.login_password);
//        final EditText etSecCodeVerify = (EditText) view.findViewById(R.id.login_seccodeverify);
//        ivSecCodeVerify = (ImageView) view.findViewById(R.id.seccode_image);
        final Spinner spSecQuestion = (Spinner) view.findViewById(R.id.login_question);
        final EditText etSecAnswer = (EditText) view.findViewById(R.id.login_answer);

        final KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(mCtx, R.layout.spinner_row);
        adapter.setEntryValues(mCtx.getResources().getStringArray(R.array.pref_login_question_list_values));
        adapter.setEntries(mCtx.getResources().getStringArray(R.array.pref_login_question_list_titles));
        spSecQuestion.setAdapter(adapter);
        String GoogleVerifyCode = "";

        etUsername.setText(HiSettingsHelper.getInstance().getUsername());
        etPassword.setText(HiSettingsHelper.getInstance().getPassword());

//        final String SecCodeURL = HiUtils.SecCodeVerifyUrl + Math.random();
//
//        etPassword.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                new DownImgAsyncTask().execute(SecCodeURL);
//                return false;
//            }
//        });

//        ivSecCodeVerify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new DownImgAsyncTask().execute(SecCodeURL);
//            }
//        });

        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getSecQuestion())
                && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getSecQuestion())) {
            int idx = Integer.parseInt(HiSettingsHelper.getInstance().getSecQuestion());
            if (idx > 0 && idx < adapter.getCount())
                spSecQuestion.setSelection(idx);
        }
        etSecAnswer.setText(HiSettingsHelper.getInstance().getSecAnswer());

        Button btnGoogle = (Button) view.findViewById(R.id.login_googleverify);
        btnGoogle.setOnClickListener(new OnSingleClickListener(){
            @Override
            public void onSingleClick(View v){
                findViewById(R.id.login_linear).setVisibility(View.INVISIBLE);
                final FrameLayout fl = findViewById(R.id.login_frame);
                gwv = new WebView(mCtx);
                WebSettings gws = gwv.getSettings();
                final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

                gws.setJavaScriptEnabled(true);
                gws.setSupportZoom(true);
                gws.setBuiltInZoomControls(true);
                gws.setDisplayZoomControls(false);

                gwv.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                        if(!message.isEmpty()){
                            Toast.makeText(mCtx, "reCAPTCHA check", Toast.LENGTH_SHORT).show();
                            HiSettingsHelper.getInstance().setGoogleVerifyCode(message);
                            handler.sendEmptyMessage(1);
                            gwv.setVisibility(View.INVISIBLE);
                            findViewById(R.id.login_linear).setVisibility(View.VISIBLE);
                            gwv.destroy();
                        }
                        result.confirm();
                        return true;
                    }
                });

                gwv.setWebViewClient(new WebViewClient()
                {
                    @Override
                    public void onLoadResource(WebView view, String url)
                    {
                        if(url.contains("userverify")){
                            handler.sendEmptyMessageDelayed(0, 1000);
                        }
                    }
                });

                fl.addView(gwv,layoutParams);
                gwv.loadUrl(HiUtils.GoogleVerifyUrl);

            }
        });

        Button btnLogin = (Button) view.findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                InputMethodManager imm = (InputMethodManager) mCtx.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

                HiSettingsHelper.getInstance().setUsername(etUsername.getText().toString());
                HiSettingsHelper.getInstance().setPassword(etPassword.getText().toString());
//                HiSettingsHelper.getInstance().setSecCodeVerity(etSecCodeVerify.getText().toString());
                HiSettingsHelper.getInstance().setSecQuestion(adapter.getEntryValue(spSecQuestion.getSelectedItemPosition()));
                HiSettingsHelper.getInstance().setSecAnswer(etSecAnswer.getText().toString());
                HiSettingsHelper.getInstance().setUid("");

                progressDialog = HiProgressDialog.show(mCtx, "正在登录...");

                final LoginHelper loginHelper = new LoginHelper(mCtx, null);

                new AsyncTask<Void, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Void... voids) {
                        return loginHelper.login();
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        if (result == Constants.STATUS_SUCCESS) {
                            Toast.makeText(mCtx, "登录成功", Toast.LENGTH_SHORT).show();
                            dismiss();
                            isShown = false;
                            if (mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = ThreadListFragment.STAGE_REFRESH;
                                mHandler.sendMessage(msg);
                            }
                            FavoriteHelper.getInstance().updateCache();
                        } else {
                            Toast.makeText(mCtx, loginHelper.getErrorMsg(), Toast.LENGTH_SHORT).show();
                            if (result == Constants.STATUS_SECCODE_FAIL_ABORT){
//                                HiSettingsHelper.getInstance().setSecCodeVerity("");
//                                new DownImgAsyncTask().execute(SecCodeURL);
                            } else {
                                HiSettingsHelper.getInstance().setUsername("");
                                HiSettingsHelper.getInstance().setPassword("");
                                HiSettingsHelper.getInstance().setSecQuestion("");
                                HiSettingsHelper.getInstance().setSecAnswer("");
//                                HiSettingsHelper.getInstance().setSecCodeVerity("");
//                                new DownImgAsyncTask().execute(SecCodeURL);
                            }
                            if (mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = ThreadListFragment.STAGE_ERROR;
                                Bundle b = new Bundle();
                                b.putString(ThreadListFragment.STAGE_ERROR_KEY, loginHelper.getErrorMsg());
                                msg.setData(b);
                                mHandler.sendMessage(msg);
                            }
                        }
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        });

        setContentView(view);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void onStop() {
        if(gwv!=null){
            gwv.loadDataWithBaseURL(null,"","text/html","utf-8",null);
            gwv.clearHistory();
            ((ViewGroup)gwv.getParent()).removeView(gwv);
            gwv.destroy();
            gwv = null;
        }

        super.onStop();
        isShown = false;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    // 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
                    handler.removeMessages(0);
                    // app的功能逻辑处理
                    String jscode = "javascript:alert(document.getElementById('g-recaptcha-response').value)";
                    gwv.loadUrl(jscode);
                    // 再次发出msg，循环更新
                    handler.sendEmptyMessageDelayed(0, 1000);
                    break;

                case 1:
                    // 直接移除，定时器停止
                    handler.removeMessages(0);
                    break;

                default:
                    break;
            }
        };
    };

//    class DownImgAsyncTask extends AsyncTask<String, Void, Bitmap> {
//
//
//        @Override
//        protected void onPreExecute() {
//            // TODO Auto-generated method stub
//            super.onPreExecute();
//            ivSecCodeVerify.setImageBitmap(null);
//        }
//
//        @Override
//        protected Bitmap doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            Bitmap b = null;
//            try {
//                b = OkHttpHelper.getInstance().getSecCode(params[0]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return b;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            // TODO Auto-generated method stub
//            super.onPostExecute(result);
//            if (result != null) {
//                ivSecCodeVerify.setImageBitmap(result);
//            }
//        }
//
//
//    }
}

