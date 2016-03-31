package net.jejer.hipda.utils;

import android.text.TextUtils;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiApplication;

public class HiUtils {
    public static final String UserAgentPrefix = "net.jejer.hipda ";
    public static final String BaseUrl = "http://club.tgfcer.com/";
    public static final String SecCodeVerifyUrl = BaseUrl + "seccode.php?update=";
    public static final String ThreadListUrl = BaseUrl + "forumdisplay.php?fid=";
    public static final String DetailListUrl = BaseUrl + "viewthread.php?tid=";
    public static final String ReplyUrl = BaseUrl + "post.php?action=reply&tid=";
    public static final String EditUrl = BaseUrl + "post.php?action=edit";
    public static final String NewThreadUrl = BaseUrl + "post.php?action=newthread&fid=";
    public static final String MyReplyUrl = BaseUrl + "my.php?item=posts";
    public static final String MyPostUrl = BaseUrl + "my.php?item=threads";
    public static final String LastPageUrl = BaseUrl + "redirect.php?goto=lastpost&from=fastpost&tid=";
    public static final String RedirectToPostUrl = BaseUrl + "redirect.php?goto=findpost&pid={pid}&ptid={tid}";
    public static final String GotoPostUrl = BaseUrl + "gotopost.php?pid={pid}";
    public static final String SMSUrl = BaseUrl + "pm.php?folder=inbox";
    public static final String SMSDetailUrl = BaseUrl + "pm.php?action=view&folder=inbox&pmid=";
    public static final String SMSPreparePostUrl = BaseUrl + "pm.php?action=send&do=reply&pmid=";
    public static final String SMSPostByUid = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1&uid={uid}";
    public static final String SMSPostByUsername = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1";
    public static final String SMSPostByPmid = BaseUrl + "pm.php?action=send&pmsubmit=yes&inajax=1";
    public static final String ThreadNotifyUrl = BaseUrl + "pm.php?folder=inbox";
    public static final String CheckSMS = BaseUrl + "pm.php?checknewpm";
    public static final String NewSMS = BaseUrl + "pm.php?folder=inbox";
    public static final String ClearSMS = BaseUrl + "pm.php?action=del&uid={uid}&filter=privatepm";
    public static final String UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
    public static final String SearchTitle = BaseUrl + "search.php?srchtype=title&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchFullText = BaseUrl + "search.php?srchtype=fulltext&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid=";
    public static final String FavoritesUrl = BaseUrl + "my.php?item={item}&type=thread";
    public static final String FavoriteAddUrl = BaseUrl + "my.php?item={item}&action=add&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
    public static final String FavoriteRemoveUrl = BaseUrl + "my.php?item={item}&action=remove&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
    public static final String UserInfoUrl = BaseUrl + "space.php?uid=";
    public static final String AvatarBaseUrl = BaseUrl + "customavatars/";

    public static final String LoginSubmit = BaseUrl + "logging.php?action=login&loginsubmit=yes&inajax=1";
    public static final String LoginGetFormHash = BaseUrl + "logging.php?action=login&referer=http%3A//club.tgfcer.com/index.php";

    private static String userAgent;

    public final static String SMILE_PATH = "images/smilies/";

    private final static String AVATAR_BASE = "000000000";
    public static int MAX_THREADS_IN_PAGE = 50;

    public static int FID_DIGIT = 10;
    public static int FID_BS = 90;

    public final static String[] FORUMS = {"灌水与情感",  "完全数码讨论区", "游戏业界综合讨论区", "摄影区", "汽车版", "二手交易区"};
    public final static int[] FORUM_IDS = {25, FID_DIGIT, 33, 41, 59, FID_BS};
    public final static IIcon[] FORUM_ICONS = {
            FontAwesome.Icon.faw_anchor,
            FontAwesome.Icon.faw_mobile_phone,
            FontAwesome.Icon.faw_gamepad,
            FontAwesome.Icon.faw_camera,
            FontAwesome.Icon.faw_car,
            FontAwesome.Icon.faw_shopping_cart
    };

    public static int getForumID(int idx) {
        return FORUM_IDS[idx];
    }

    public static int getForumIndexByFid(int fid) {
        for (int i = 0; i < FORUM_IDS.length; i++) {
            if (fid == FORUM_IDS[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isForumEnabled(int fid) {
        if (fid == FID_DIGIT) {
            return true;
        }
        if (getForumIndexByFid(fid) >= 0) {
            if (HiSettingsHelper.getInstance().getForums().contains(fid + ""))
                return true;
        }
        return false;
    }

    public final static String[] BS_TYPES = {"全部", "手机", "掌上电脑", "笔记本电脑", "无线产品", "数码相机、摄像机", "MP3随身听", "各类配件", "其他好玩的"};
    public final static String[] BS_TYPE_IDS = {"", "1", "2", "3", "4", "5", "6", "7", "8"};
    public final static IIcon[] BS_TYPE_ICONS = {
            FontAwesome.Icon.faw_tags,
            FontAwesome.Icon.faw_mobile_phone,
            FontAwesome.Icon.faw_tablet,
            FontAwesome.Icon.faw_laptop,
            FontAwesome.Icon.faw_wifi,
            FontAwesome.Icon.faw_camera_retro,
            FontAwesome.Icon.faw_music,
            FontAwesome.Icon.faw_desktop,
            FontAwesome.Icon.faw_gamepad
    };

    public static int getBSTypeIndexByFid(String typeId) {
        for (int i = 0; i < BS_TYPE_IDS.length; i++) {
            if (BS_TYPE_IDS[i].equals(typeId)) {
                return i;
            }
        }
        return -1;
    }

    public static String getFullUrl(String particalUrl) {
        return BaseUrl + particalUrl;
    }

    public static String getAvatarUrlByUid(String uid) {
        /* Hi-Pda Avatar
        if (TextUtils.isEmpty(uid)
                || uid.length() > AVATAR_BASE.length()
                || !TextUtils.isDigitsOnly(uid))
            return "";

        String fullUid = AVATAR_BASE.substring(0, AVATAR_BASE.length() - uid.length()) + uid;
        String url = AvatarBaseUrl
                + fullUid.substring(0, 3) + "/"
                + fullUid.substring(3, 5) + "/"
                + fullUid.substring(5, 7) + "/"
                + fullUid.substring(7, 9) + "_avatar_middle.jpg";
        return url;
        */
        // TGFC Avatar
        //TODO 获取链接与论坛自带头像

        String url = AvatarBaseUrl + uid + ".jpg";
        return url;
    }

    public static int getThemeValue(String theme) {
        if ("light".equals(theme)) {
            return R.style.ThemeLight;
        } else if ("dark".equals(theme)) {
            return R.style.ThemeDark;
        } else if ("black".equals(theme)) {
            return R.style.ThemeBlack;
        } else if ("light-deep_orange".equals(theme)) {
            return R.style.ThemeLight_DeepOrange;
        } else if ("light-blue_grey".equals(theme)) {
            return R.style.ThemeLight_BlueGrey;
        } else if ("light-teal".equals(theme)) {
            return R.style.ThemeLight_Teal;
        } else if ("light-indigo".equals(theme)) {
            return R.style.ThemeLight_Indigo;
        }
        HiSettingsHelper.getInstance().setTheme("light");
        return R.style.ThemeLight;
    }

    public static boolean isValidId(String id) {
        return !TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id) && Integer.parseInt(id) > 0;
    }

    public static String getUserAgent() {
        if (userAgent == null)
            userAgent = UserAgentPrefix + " " + HiApplication.getAppVersion();
        return userAgent;
    }

}
