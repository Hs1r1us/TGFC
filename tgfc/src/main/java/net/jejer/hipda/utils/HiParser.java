package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.UserInfoBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HiParser {

    public static SimpleListBean parseSimpleList(Context ctx, int type, Document doc) {

        // Async check notify
        new HiParserThreadList.parseNotifyRunnable(ctx, doc).run();

        switch (type) {
            case SimpleListLoader.TYPE_MYREPLY:
                return parseReplyList(ctx, doc);
            case SimpleListLoader.TYPE_MYPOST:
                return parseMyPost(ctx, doc);
            case SimpleListLoader.TYPE_SMS:
                return parseSMS(doc);
            case SimpleListLoader.TYPE_THREAD_NOTIFY:
                return parseNotify(doc);
            case SimpleListLoader.TYPE_SMS_DETAIL:
                return parseSmsDetail(doc);
            case SimpleListLoader.TYPE_SEARCH:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_SEARCH_USER_THREADS:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_FAVORITES:
                return parseFavorites(doc);
            case SimpleListLoader.TYPE_ATTENTION:
                return parseFavorites(doc);
        }

        return null;
    }

    private static SimpleListBean parseReplyList(Context ctx, Document doc) {
        if (doc == null) {
            return null;
        }

        Elements mainES = doc.select("div.content div.mainbox");
        if (mainES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = mainES.first().select("table tbody tr");

        SimpleListItemBean item = null;
        //first tr is title, skip
        for (int i = 0; i < trES.size(); ++i) {
            Element trE = trES.get(i);

            // odd have title, even have reply text;
            item = new SimpleListItemBean();

            // thread
            Elements tdES = trE.select("td");
            if (tdES.size() == 0) {
                continue;
            }
            Elements linkES = tdES.get(0).select("a");
            if (linkES.size() != 1) {
                continue;
            }
            String tid = linkES.first().attr("href");
            if (!tid.startsWith("redirect.php?goto=")) {
                continue;
            }
            item.setTid(HttpUtils.getMiddleString(tid, "ptid=", "&"));
            item.setPid(HttpUtils.getMiddleString(tid, "pid=", "&"));
            String title = linkES.first().text();

            // time
            Elements lastpostES = trE.select("td cite a");
            if (lastpostES.size() == 0) {
                continue;
            }
            String time = lastpostES.first().text();
            String author= lastpostES.get(1).text();

            item.setTitle(title);
            item.setTime(author + " " + time);

            Elements forumES = trE.select("td").get(1).select("a");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }
        return list;
    }

    private static SimpleListBean parseMyPost(Context ctx, Document doc) {
        if (doc == null) {
            return null;
        }

        Elements mainES = doc.select("div.content div.mainbox");
        if (mainES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = mainES.first().select("table tbody tr");

        SimpleListItemBean item = null;
        //first tr is title, skip
        for (int i = 0; i < trES.size(); ++i) {
            Element trE = trES.get(i);

            // odd have title, even have reply text;
            item = new SimpleListItemBean();

            // thread
            Elements tdES = trE.select("td");
            if (tdES.size() == 0) {
                continue;
            }
            Elements linkES = tdES.get(0).select("a");
            if (linkES.size() != 1) {
                continue;
            }
            String tid = linkES.first().attr("href");
            if (!tid.startsWith("thread-")) {
                continue;
            }
            tid = HttpUtils.getMiddleString(tid, "thread-", "-");
            String title = linkES.first().text();

            // time
            Elements lastpostES = trE.select("td cite a");
            if (lastpostES.size() == 0) {
                continue;
            }
            String time = lastpostES.first().text();
            String author= lastpostES.get(1).text();

            item.setTid(tid);
            item.setTitle(title);
            item.setTime(author + " " + time);

            Elements forumES = trE.select("td").get(1).select("a");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }
        return list;
    }

    public static SimpleListBean parseSMS(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements pmlistES = doc.select("table#pmlist tbody");
        if (pmlistES.size() < 1) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        Elements liES = pmlistES.first().select("tr");

        for (int i = 0; i < liES.size(); ++i) {
//            if (!liES.get(i).select("td").get(1).select("a").text().contains("您发表的帖子")) {
                Element liE = liES.get(i);
                SimpleListItemBean item = new SimpleListItemBean();


                // author and author uid
                Element pciteES = liE.select("td").get(2);
                Elements citeES = pciteES.select("a");
                if (citeES.size() == 0) {
                    continue;
                }
                item.setAuthor(citeES.first().text());
                item.setForum(item.getAuthor());
                Elements uidAES = citeES.first().select("a");
                if (uidAES.size() == 0) {
                    continue;
                }
                String uidHref = uidAES.first().attr("href");
                item.setUid(HttpUtils.getMiddleString(uidHref, "uid-", "."));

                // avatar
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(item.getUid()));

                // time
                item.setTime(liE.select("td em").text());

                // new
                Element newES = liE.select("td").get(1);
                if (newES.attr("style").contains("font-weight:800")) {
                    item.setNew(true);
                }

                // info
                Element summaryES = liE.select("td a").get(0);

                item.setTitle(summaryES.text());

                // detail url
                String detailHref = liE.select("td a").get(0).attr("href");
                item.setDetailUrl(HiUtils.BaseUrl+detailHref);

                // pmid
                String pmid = HttpUtils.getMiddleString(detailHref,"pmid=","&");
                item.setPmid(pmid);

                list.add(item);
//            }
        }

        return list;
    }

    public static SimpleListBean parseNotify(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements feedES = doc.select("table#pmlist tbody");
        if (feedES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        Elements liES = feedES.first().select("tr");
        for (int i = 0; i < liES.size(); ++i) {
            Element liE = liES.get(i);
            Elements divES = liE.select("td");
            if (divES.size() == 0) {
                continue;
            }
            SimpleListItemBean item = null;
            if (divES.first().hasClass("f_thread")) {
                // user reply your thread
                item = parseNotifyThread(divES.first());
            } else if (divES.get(1).select("a").toString().contains("您发表的帖子")) {
                // user quote your post
                item = parseNotifyQuoteandReply(divES);
                if (divES.get(1).attr("style").contains("font-weight:800")){
                    item.setNew(true);
                }
            } else if (divES.first().hasClass("f_reply")) {
                // user reply your post
                item = parseNotifyQuoteandReply(divES);
            }

            if (item != null) {
                list.add(item);
            }
        }

        return list;
    }

    public static SimpleListItemBean parseNotifyThread(Element root) {
        SimpleListItemBean item = new SimpleListItemBean();
        String info = "";

        Elements aES = root.select("a");
        for (Element a : aES) {
            String href = a.attr("href");
            if (href.contains("space.php")) {
                // get replied usernames
                info += a.text() + " ";
            } else if (href.startsWith(HiUtils.BaseUrl + "redirect.php?from=notice&goto=findpost")) {
                // Thread Name and TID and PID
                item.setTitle(a.text());
                item.setTid(HttpUtils.getMiddleString(a.attr("href"), "ptid=", "&"));
                item.setPid(HttpUtils.getMiddleString(a.attr("href"), "pid=", "&"));
                break;
            }
        }

        // time
        Elements emES = root.select("em");
        if (emES.size() == 0) {
            return null;
        }
        item.setTime(emES.first().text());

        if (root.text().contains("回复了您关注的主题"))
            info += "回复了您关注的主题";
        else
            info += "回复了您的帖子 ";

        // new
        Elements imgES = root.select("img");
        if (imgES.size() > 0) {
            if (imgES.first().attr("src").equals("images/default/notice_newpm.gif")) {
                item.setNew(true);
            }
        }

        item.setInfo(info);
        return item;
    }

    public static SimpleListItemBean parseNotifyQuoteandReply(Elements td) {
        SimpleListItemBean item = new SimpleListItemBean();

        String detailHref = td.get(1).select("a").attr("href");
        String authorHref = td.get(2).select("a").attr("href");
        String uid = HttpUtils.getMiddleString(authorHref,"uid-",".");
        item.setUid(uid);
        item.setForum(td.get(2).select("a").text());
        item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
        item.setTitle(td.get(1).text());
        item.setTime(td.get(3).text());
        item.setDetailUrl(HiUtils.BaseUrl+detailHref);
        item.setInfo("");
        if (td.get(1).attr("style").contains("font-weight:800")){
            item.setNew(true);
        }
        return item;
    }

    public static SimpleListItemBean parseNotifyQuoteandDetail(String rsp) {
        Document doc = Jsoup.parse(rsp);
        if (doc == null) {
            return null;
        }

        SimpleListItemBean item = new SimpleListItemBean();

        String tid = "";
        String pid = "";
        String info = "";
        String author = "";
        String type = doc.select("div.content div.mainbox h1").text();
        Elements quoteES = doc.select("div.content div.postmessage");
        Elements aES = quoteES.select("a");
        if (type.contains("您发表的帖子被引用")) {
            if (aES.get(0).attr("href").contains("redirect.php")) {
                tid = HttpUtils.getMiddleString(aES.get(2).attr("href"), "tid=", "&");
                pid = HttpUtils.getMiddleString(aES.get(2).attr("href"), "pid=", "&");
            } else if (aES.size() == 2) {
                tid = HttpUtils.getMiddleString(aES.get(1).attr("href"), "tid=", "&");
                pid = HttpUtils.getMiddleString(aES.get(1).attr("href"), "pid=", "#");
            } else {
                tid = HttpUtils.getMiddleString(aES.get(0).attr("href"), "tid=", "&");
            }
            author = HttpUtils.getMiddleString(quoteES.toString(), "以下您所发表的帖子被 ", " 引用并通知您。");
            String yourInfo = HttpUtils.getMiddleString(quoteES.toString(), "</div>", "[/quote]");
            String quoteInfo = HttpUtils.getMiddleString(quoteES.toString(), "引用摘要:</strong>", "<br>");
            info = "<u>您的帖子:</u>" + yourInfo + "<br><u>" + author + " 说:</u>" + quoteInfo;
        } else if(type.contains("您发表的帖子被回复")) {
             author = HttpUtils.getMiddleString(quoteES.toString(), "以下您所发表的帖子被 ", " 回复并通知您。");
             info = quoteES.toString();
        } else if (type.contains("您发表的帖子被评分")) {
            author = HttpUtils.getMiddleString(aES.toString(), "以下您所发表的帖子被 ", " 评分。");
            for (Element a : aES) {
                if (a.attr("href").contains("viewthread.php")) {
                    tid = HttpUtils.getMiddleString(a.attr("href"), "tid=", "&");
                    pid = HttpUtils.getMiddleString(a.attr("href"), "pid=", "&");
                }
            }
            info = quoteES.toString();
        }
        item.setInfo(info);
        item.setTid(tid);
        item.setPid(pid);

        return item;
    }

    private static SimpleListBean parseSmsDetail(Document doc) {
        if (doc == null) {
            return null;
        }

        String tid = "";
        String pid = "";
        String author = "";
        String authorUid = "";
        String smsTime = "";
        String smsDetail = "";
        Elements mainBox = doc.select("div.mainbox");
        if (mainBox.size() > 0) {
            String type = doc.select("div.content div.mainbox h1").text();
            Elements quoteES = doc.select("div.content div.postmessage");
            Elements aES = quoteES.select("a");
            if (type.contains("您发表的帖子被引用")) {
                if (aES.get(0).attr("href").contains("redirect.php")) {
                    tid = HttpUtils.getMiddleString(aES.get(2).attr("href"), "tid=", "&");
                    pid = HttpUtils.getMiddleString(aES.get(2).attr("href"), "pid=", "&");
                } else if (aES.size() == 2) {
                    tid = HttpUtils.getMiddleString(aES.get(1).attr("href"), "tid=", "&");
                    pid = HttpUtils.getMiddleString(aES.get(1).attr("href"), "pid=", "#");
                } else {
                    tid = HttpUtils.getMiddleString(aES.get(0).attr("href"), "tid=", "&");
                }
                author = HttpUtils.getMiddleString(quoteES.toString(), "以下您所发表的帖子被 ", " 引用并通知您。");
                String yourInfo = HttpUtils.getMiddleString(quoteES.toString(), "</div>", "[/quote]");
                String quoteInfo = HttpUtils.getMiddleString(quoteES.toString(), "引用摘要:</strong>", "<br>");
                smsDetail = "<u>您的帖子:</u>" + yourInfo + "<br><u>" + author + " 说:</u>" + quoteInfo;
            } else if(type.contains("您发表的帖子被回复")) {
                author = HttpUtils.getMiddleString(quoteES.toString(), "以下您所发表的帖子被 ", " 回复并通知您。");
                smsDetail = quoteES.toString();
            } else if (type.contains("您发表的帖子被评分")) {
                author = HttpUtils.getMiddleString(aES.toString(), "以下您所发表的帖子被 ", " 评分。");
                for (Element a : aES) {
                    if (a.attr("href").contains("viewthread.php")) {
                        tid = HttpUtils.getMiddleString(a.attr("href"), "tid=", "&");
                        pid = HttpUtils.getMiddleString(a.attr("href"), "pid=", "&");
                    }
                }
                smsDetail = quoteES.toString();
            } else {
                String smsTitle = mainBox.select("h1").text();
                Elements smsDetailES = mainBox.select("table tbody tr td.postcontent");

                Elements postInfoES = smsDetailES.select("p.postinfo").select("a");
                //get my uid and username
                smsTime = HttpUtils.getMiddleString(smsDetailES.select("p.postinfo").text(), "时间:", ",");
                Element myUserInfo = postInfoES.size() == 1 ? postInfoES.get(0) : postInfoES.get(1);
                String myUsername = postInfoES.size() == 1 ? myUserInfo.text() : myUserInfo.text();
                String myUid = HttpUtils.getMiddleString(myUserInfo.attr("href"), "uid-", ".");
                author = postInfoES.get(0).text();
                authorUid = HttpUtils.getMiddleString(postInfoES.get(0).attr("href"), "uid-", ".");

                Elements postmessageES = smsDetailES.select("div.postmessage");
                String mySmsDetail = postmessageES.select("div.quote blockquote").text();
                String authorSmsDetail = HttpUtils.getMiddleString(postmessageES.first().toString(), "<div data-plugin=\"autoitem\" class=\"postmessage\">", "");
                smsDetail = authorSmsDetail;
            }
        } else if(doc.text().contains("您的信箱已满")) {
            author = "系统消息";
            authorUid = "0";
            smsDetail = "您的信箱已满，在阅读短消息前，请使用浏览器删除一些不用的信息。";
        } else {
            return null;
        }
            SimpleListBean list = new SimpleListBean();
            SimpleListItemBean item = new SimpleListItemBean();

            // link
            item.setTid(tid);
            item.setPid(pid);

            // author
            item.setAuthor(author);
            item.setUid(authorUid);

            // avatar
            item.setAvatarUrl(HiUtils.getAvatarUrlByUid(item.getUid()));


            // time
            item.setTime(smsTime);

            // info
            item.setInfo(smsDetail);

        // new
        list.add(item);


        return list;
    }

    private static SimpleListBean parseSearch(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        String searchIdUrl;
        if (pagesES.size() > 0) {
            searchIdUrl = pagesES.first().attr("href");
            if (searchIdUrl.contains("srchtype=fulltext")) {
                return parseSearchFullText(doc);
            }
            list.setSearchIdUrl(searchIdUrl);
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("div.threadlist tbody");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element tbodyE = tbodyES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = tbodyE.select("tr th");
            if (subjectES.first().text().contains("没有找到匹配结果")) {
                continue;
            }
            item.setTitle(subjectES.select("a").first().text());

            Elements subjectAES = subjectES.first().select("a");
            if (subjectAES.size() == 0) {
                continue;
            }
            String href = subjectAES.first().attr("href");
            item.setTid(HttpUtils.getMiddleString(href, "tid=", "&"));

            Elements authorAES = tbodyE.select("tr td.author cite a");
            if (authorAES.size() == 0) {
                continue;
            }
            item.setAuthor(authorAES.first().text());

            String spaceUrl = authorAES.first().attr("href");
            if (!TextUtils.isEmpty(spaceUrl)) {
                String uid = HttpUtils.getMiddleString(spaceUrl, "space-uid-", ".");
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
            }

            Elements timeES = tbodyE.select("tr td.author em");
            if (timeES.size() > 0) {
                item.setTime(item.getAuthor() + "  " + timeES.first().text());
            }

            Elements forumES = tbodyE.select("tr td.forum");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearchFullText(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        String searchIdUrl;
        if (pagesES.size() > 0) {
            searchIdUrl = pagesES.first().attr("href");
            list.setSearchIdUrl(searchIdUrl);
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("div.threadlist tbody");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element tbodyE = tbodyES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = tbodyE.select("tr th");
            if (subjectES.size() == 0) {
                continue;
            }
            item.setTitle(subjectES.select("a").first().text());

            Elements subjectAES = subjectES.first().select("a");
            if (subjectAES.size() == 0) {
                continue;
            }
            String href = subjectAES.first().attr("href");
            item.setTid(HttpUtils.getMiddleString(href, "tid=", "&"));

            Elements authorAES = tbodyE.select("tr td.author cite a");
            if (authorAES.size() == 0) {
                continue;
            }
            item.setAuthor(authorAES.first().text());

            String spaceUrl = authorAES.first().attr("href");
            if (!TextUtils.isEmpty(spaceUrl)) {
                String uid = HttpUtils.getMiddleString(spaceUrl, "space-uid-", ".");
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
            }

            Elements timeES = tbodyE.select("tr td.author em");
            if (timeES.size() > 0) {
                item.setTime(item.getAuthor() + "  " + timeES.first().text());
            }

            Elements forumES = tbodyE.select("tr td.forum");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseFavorites(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages a");
        pagesES.addAll(doc.select("div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = doc.select("div.content div.mainbox form table tbody tr");
        for (int i = 0; i < trES.size(); ++i) {
            Element trE = trES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements tdES = trE.select("td");
            if (tdES.size() < 2 ){
                continue;
            }
            Element subjectE = trE.select("td").get(1);

            item.setTitle(subjectE.text());

            Elements subjectAES = subjectE.select("a");
            if (subjectAES.size() == 0) {
                continue;
            }
            String href = subjectAES.first().attr("href");
            item.setTid(HttpUtils.getMiddleString(href, "thread-", "-"));

            Elements timeES = trE.select("cite a");
            if (timeES.size() > 0) {
                String time = timeES.get(0).text();
                String author = timeES.get(1).text();
                item.setTime(author + "" + time);
            }

            Elements forumES = trE.select("td").get(2).select("a");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text().trim());
            }

            list.add(item);
        }

        return list;
    }

    public static UserInfoBean parseUserInfo(String rsp) {
        Document doc = Jsoup.parse(rsp);
        if (doc == null) {
            return null;
        }

        UserInfoBean info = new UserInfoBean();

        Elements usernameES = doc.select("div.specialthread h1");
        if (usernameES.size() > 0) {
            info.setUsername(Utils.nullToText(usernameES.first().text()).trim().replace(" 的个人资料",""));
        }

        Elements onlineImgES = doc.select("div#profilecontent div.itemtitle img");
        if (onlineImgES.size() > 0) {
            info.setOnline(Utils.nullToText(onlineImgES.first().attr("src")).contains("online"));
        }

        Elements uidTdES = doc.select("td.postcontent table thead");
        Elements uidES = uidTdES.get(0).select("tr td a");
        if (uidES.size() > 0) {
            info.setUid(HttpUtils.getMiddleString(uidES.get(0).attr("href"), "eccredit.php?uid=", ""));
        }

        Elements avatarES = doc.select("div.avatar img");
        if (avatarES.size() != 0) {
            info.setAvatarUrl(HiUtils.BaseUrl + avatarES.first().attr("src"));
        }

        StringBuilder sb = new StringBuilder();

        Elements titleES = doc.select("td.postcontent table tbody");
        int i = 0;
        for (Element titleEl : titleES) {
//            sb.append(titleEl.text()).append("\n\n");
            if (i == 0) {
                Elements detailES = doc.select("td.postcontent table tbody tr");
                for (Element detail : detailES) {
                    sb.append(detail.text()).append('\n');
                }
            }
            i++;
            sb.append("\n");
            if (i >= 2)
                break;
        }


        info.setDetail(sb.toString());

        return info;
    }
}
