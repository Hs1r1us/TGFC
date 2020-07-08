package net.jejer.hipda.async;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

public class LoginHelper {

    private Context mCtx;
    private Handler mHandler;

    private String mErrorMsg = "";

    public LoginHelper(Context ctx, Handler handler) {
        mCtx = ctx;
        mHandler = handler;
    }

    public int login() {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_RELOGIN;
            mHandler.sendMessage(msg);
        }

        int status = Constants.STATUS_FAIL_ABORT;

        if (HiSettingsHelper.getInstance().isLoginInfoValid()) {
//            String formhash = getFormhash();
//            if (!TextUtils.isEmpty(formhash)) {
//                status = doLogin(formhash);
            String formhash = "";
            status = doLogin(formhash);
//            }
        } else {
            mErrorMsg = "登录信息不完整";
        }

        if (mHandler != null) {
            Message msg = Message.obtain();
            if (status == Constants.STATUS_FAIL) {
                msg.what = ThreadListFragment.STAGE_ERROR;
            } else if (status == Constants.STATUS_FAIL_ABORT) {
                msg.what = ThreadListFragment.STAGE_NOT_LOGIN;
            }
            Bundle b = new Bundle();
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, mErrorMsg);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

        if (status == Constants.STATUS_SUCCESS)
            EventBus.getDefault().post(new LoginEvent());

        return status;
    }

    public String getFormhash() {
        String rstStr = null;
        try {
            rstStr = OkHttpHelper.getInstance().get(HiUtils.LoginGetFormHash);

            if (!TextUtils.isEmpty(rstStr)) {
                Document doc = Jsoup.parse(rstStr);

                Elements elements = doc.select("input[name=formhash]");
                Element element = elements.first();

                if (element == null) {
                    Elements alartES = doc.select("div.alert_info");
                    if (alartES.size() > 0) {
                        mErrorMsg = alartES.first().text();
                    } else {
                        mErrorMsg = "Can NOT get formhash";
                    }
                    return "";
                }
                return element.attr("value");
            }
        } catch (Exception e) {
            mErrorMsg = OkHttpHelper.getErrorMessage(e);
        }
        return rstStr;
    }

    private int doLogin(String formhash) {
        Map<String, String> post_param = new HashMap<>();
        post_param.put("formhash", formhash);
        post_param.put("referer", HiUtils.BaseUrl + "index.php");
        post_param.put("loginfield", "username");
        post_param.put("username", HiSettingsHelper.getInstance().getUsername());
        post_param.put("password", HiSettingsHelper.getInstance().getPassword());
//        post_param.put("seccodeverify",HiSettingsHelper.getInstance().getSecCodeVerify());
        post_param.put("questionid", HiSettingsHelper.getInstance().getSecQuestion());
        post_param.put("answer", HiSettingsHelper.getInstance().getSecAnswer());
        post_param.put("g-recaptcha-response",HiSettingsHelper.getInstance().getGoogleVerifyCode());
        post_param.put("cookietime", "2592000");
        post_param.put("loginsubmit", "true");

        Logger.v(post_param.toString());

        String rspStr;
        try {
            rspStr = OkHttpHelper.getInstance().post(HiUtils.LoginSubmit, post_param);
            Logger.v(rspStr);

            // response is in XML format
            if (rspStr.contains(mCtx.getString(R.string.login_success))) {
                Logger.v("Login success!");
                return Constants.STATUS_SUCCESS;
            }else if(rspStr.contains("安全提问")) {
                mErrorMsg = "登录失败，请正确回答安全提问";
                return Constants.STATUS_FAIL;
            }else{
                Document doc = Jsoup.parse(rspStr);
                String errorMsg = doc.select("div.box p").text();
                if(!errorMsg.isEmpty()){
                    mErrorMsg = "登录失败:" + errorMsg;
                }else {
                    mErrorMsg = "登录失败，未知错误";
                }
                return Constants.STATUS_FAIL_ABORT;
            }
        } catch (Exception e) {
            mErrorMsg = "登录失败 : " + OkHttpHelper.getErrorMessage(e);
            return Constants.STATUS_FAIL;
        }
    }

    public static boolean checkLoggedin(Context context, String mRsp) {
        boolean loggedIn = !mRsp.contains(context.getString(R.string.not_login));
//        if (!loggedIn)
            //logout();
        return loggedIn;
    }

    public static boolean isLoggedIn() {
        return OkHttpHelper.getInstance().isLoggedIn();
    }

    public static void logout() {
        OkHttpHelper.getInstance().clearCookies();
        FavoriteHelper.getInstance().clearAll();
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

}
