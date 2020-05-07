package net.jejer.hipda.utils;

import android.text.TextUtils;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiApplication;

public class HiUtils {
    public static final String UserAgentPrefix = "Mozilla/5.0 (Android 10; Mobile; rv:75.0)";
    public static final String BaseUrl = "https://club.tgfcer.com/";
    public static final String Pic2_Url = "http://pic2.52tgfc.com/";
    public static final String SecCodeVerifyUrl = BaseUrl + "seccode.php?update=";
    public static final String GoogleVerifyUrl = "https://wap.tgfcer.com/index.php?action=login";
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
    public static final String ClearSMS = BaseUrl + "pm.php?action=delete&folder=inbox&pmid=";
    public static final String UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
    public static final String SearchTitle = BaseUrl + "search.php?srchtype=title&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchFullText = BaseUrl + "search.php?srchtype=fulltext&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid=";
    public static final String FavoritesUrl = BaseUrl + "my.php?item={item}&type=thread";
    public static final String FavoriteAddUrl = BaseUrl + "my.php?item={item}&action=add&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
    public static final String FavoriteRemoveUrl = BaseUrl + "my.php?item={item}&action=remove&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
    public static final String UserInfoUrl = BaseUrl + "space.php?action=viewpro&uid=";
    public static final String AvatarBaseUrl = BaseUrl + "customavatars/";

    public static final String LoginSubmit = BaseUrl + "logging.php?action=login&";
    public static final String LoginGetFormHash = BaseUrl + "logging.php?action=login";
    public static final String RatingSubmit = BaseUrl + "misc.php?action=rate&inajax=1";
    public static final String PreRating = BaseUrl + "misc.php?action=rate";

    private static String userAgent;

    public final static String SMILE_PATH = "images/smilies/";

    private final static String AVATAR_BASE = "000000000";
    public static int MAX_THREADS_IN_PAGE = 50;

    public static int FID_DIGIT = 10;
    public static int FID_BS = 90;

    public final static String[] FORUMS = {
            "灌水与情感",
            "完全数码讨论区",
            "Apple 专区",
            "摄影区",
            "汽车版",
            "二手交易区",
            "游戏业界综合讨论区",
            "主机游戏讨论区",
            "主机和掌机游戏讨论区",
            "体育运动专区",
            "体育赛事直喷专区",
            "影视专区",
            "影视资源",
            "手机游戏讨论区",
            "经典游戏怀旧专区",
            "动漫模玩",
            "安卓电视游戏讨论区",
            "硬件维修区",
            "旅行度假",
            "舌尖上的TG",
            "宠物乐园",
            "TGFC御宅深喉K歌大赛",
            "招聘求职",
            "网游业界讨论区",
            "英雄联盟",
            "暴雪游戏专区",
            "ChinaJoy 2013",
            "写真摄影服务",
            "新品贩卖区",
            "神鬼奇谈",
            "文史春秋",
            "公共工作区"
    };
    public final static int[] FORUM_IDS = {25, FID_DIGIT, 85, 41, 59, FID_BS, 33, 29, 5, 6, 48, 12, 104, 5, 101, 11, 103, 45, 97, 99, 95, 76, 86, 67, 98, 40, 65, 88, 17, 72, 93, 26};
    public final static IIcon[] FORUM_ICONS = {
            FontAwesome.Icon.faw_anchor,
            FontAwesome.Icon.faw_mobile_phone,
            FontAwesome.Icon.faw_apple,
            FontAwesome.Icon.faw_camera,
            FontAwesome.Icon.faw_car,
            FontAwesome.Icon.faw_shopping_cart,
            FontAwesome.Icon.faw_star,
            FontAwesome.Icon.faw_gamepad,
            FontAwesome.Icon.faw_credit_card,
            FontAwesome.Icon.faw_soccer_ball_o,
            FontAwesome.Icon.faw_soccer_ball_o,
            FontAwesome.Icon.faw_video_camera,
            FontAwesome.Icon.faw_video_camera,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o,
            FontAwesome.Icon.faw_sticky_note_o
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
        if(particalUrl.startsWith("http")) {
            return particalUrl;
        }else{
            return BaseUrl + particalUrl;
        }
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
