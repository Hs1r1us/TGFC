package net.jejer.hipda.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailBean.Contents;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.SmallImages;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.ui.textstyle.TextStyle;
import net.jejer.hipda.ui.textstyle.TextStyleHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiParserThreadDetail {

    private static String URL_REGEX = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    private static Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    public static DetailListBean parse(Context ctx, Handler handler, Document doc, boolean parseTid) {

        boolean isVoteThread = doc.select("div.wrap div.specialthread").size() > 0;
        Document voteDoc = null;
        if(isVoteThread){
            String path = "";
            Elements scriptES = doc.select("script");
            if (scriptES.size() > 0){
                for (Element e : scriptES){
                    if(e.toString().contains("ajaxspecialpost")){
                        path = HttpUtils.getMiddleString(e.toString(), "ajaxget('","', 'ajaxspecialpost'");
                    }
                }
            }
            Map<String, String> params = new HashMap<>();
            String voteRsp;
            try {
                voteRsp = OkHttpHelper.getInstance().post(HiUtils.BaseUrl + path, params);
                voteDoc = Jsoup.parse(voteRsp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // get last page
        Elements pagesES = isVoteThread ? voteDoc.select("div.wrap div.pages_btns div.pages") : doc.select("div.wrap div.pages_btns div.pages");
        // thread have only 1 page don't have "div.pages"
        pagesES.select("em").remove();
        int last_page = 1;
        int page = 1;
        if (pagesES.size() != 0) {
            for (Node n : pagesES.first().childNodes()) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
                if ("strong".equals(n.nodeName())) {
                    page = tmp;
                }
            }
        }

        // Update UI
        Message msgStartParse = Message.obtain();
        msgStartParse.what = ThreadListFragment.STAGE_PARSE;
        Bundle b = new Bundle();
        b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, page);
        msgStartParse.setData(b);
        handler.sendMessage(msgStartParse);

        // Async check notify
        new HiParserThreadList.parseNotifyRunnable(ctx, doc).run();
        HiSettingsHelper.updateMobileNetworkStatus(ctx);

        DetailListBean details = new DetailListBean();
        details.setPage(page);
        details.setLastPage(last_page);

        if (parseTid) {
            Elements printES = doc.select("div.posterinfo span.headactions a.notabs");
            if (printES.size() > 0) {
                String tid = HttpUtils.getMiddleString(printES.first().attr("href"), "tid=", "&");
                if (!TextUtils.isEmpty(tid) && TextUtils.isDigitsOnly(tid))
                    details.setTid(tid);
            }
        }

        //get forum id
        Elements divNavES = doc.select("div#foruminfo div#nav");
        if (divNavES.size() > 0) {
            Elements divNavLinkES = divNavES.first().select("a");
            if (divNavLinkES.size() > 0) {
                for (int i = 0; i < divNavLinkES.size(); i++) {
                    Element forumLink = divNavLinkES.get(i);
                    String forumUrl = Utils.nullToText(forumLink.attr("href"));
                    if (forumUrl.indexOf("-") > 0) {
                        details.setFid(HttpUtils.getMiddleString(forumUrl, "forum-", "-"));
                        break;
                    }
                }
            }
            //get thread title from nav div
            divNavLinkES.remove();
            String title = divNavES.text();
            title = title.replace("»", "").trim();
            details.setTitle(title);
        }

        //Title, only avaliable in first page
        if (TextUtils.isEmpty(details.getTitle())) {
            Elements threadtitleES = doc.select("div.viewthread");
            if (threadtitleES.size() > 0) {
                threadtitleES.select("a").remove();
                details.setTitle(threadtitleES.first().text());
            }
        }

        Elements rootES = isVoteThread ?  voteDoc.select("form[name=\"modactions\"]") : doc.select("form[name=\"modactions\"]");
        if (rootES.size() != 1) {
            return null;
        }

        if(isVoteThread && page == 1){
            Element voteE = doc.select("div.specialthread table").first();
            DetailBean voteDetail = new DetailBean();
            //id
            voteDetail.setPostId(HttpUtils.getMiddleString(voteE.select("cite a").first().attr("href"),"uid-",".html"));
            //time
            voteDetail.setTimePost("2017");
            //floor
            voteDetail.setFloor("0");
            //author
            voteDetail.setAuthor(voteE.select("cite a").text());
            //avatar
            voteDetail.setAvatarUrl(HiUtils.BaseUrl + voteE.select("div.avatar img").first().attr("src"));
            //content
            Contents content = voteDetail.getContents();
            TextStyle ts = new TextStyle();
            String text = ts.toHtml(voteE.select("div.postmessage").toString());
            content.addText(text, ts);

            details.add(voteDetail);
        }

        Elements postsEL = isVoteThread ? rootES.select("div.specialpost") : rootES.select("div.viewthread");
        for (int i = 0; i < postsEL.size(); i++) {
            Element postE = postsEL.get(i);
                DetailBean detail = new DetailBean();

                //id
                String id = isVoteThread ? "pid" + HttpUtils.getMiddleString(postE.select("div.postinfo h2 a").attr("id"),"author_","") : postE.select("table").attr("id");
                if (id.length() < "pid".length()) {
                    continue;
                }
                id = id.substring("pid".length());
                detail.setPostId(id);

                //time
                if(isVoteThread){
                    detail.setTimePost(postE.select("div.postinfo h2").text());
                } else {
                    Elements timeEMES = postE.select("table tbody tr td.postcontent div.postinfo").clone();
                    timeEMES.select("strong").remove();
                    timeEMES.select("em").remove();
                    timeEMES.select("a").remove();
                    if (timeEMES.size() == 0) {
                        continue;
                    }
                    String time = timeEMES.first().text();
                    detail.setTimePost(time);
                }
                //floor
                Elements postinfoAES = isVoteThread ? postE.select("div.postinfo strong") : postE.select("table tbody tr td.postcontent div.postinfo strong");
                postinfoAES.remove("sup");
                if (postinfoAES.size() == 0) {
                    continue;
                }
                String floor = postinfoAES.first().text().replace("#","");
                detail.setFloor(floor);

                //update max posts in page, this is controlled by user setting
                if (i == 0) {
                    if (page == 1 && last_page > 1) {
                        HiSettingsHelper.getInstance().setMaxPostsInPage(postsEL.size());
                    } else if (page > 1) {
                        int maxPostsInPage = (Integer.parseInt(floor) - 1) / (page - 1);
                        HiSettingsHelper.getInstance().setMaxPostsInPage(maxPostsInPage);
                    }
                }

                //author
                Elements postauthorAES = isVoteThread ? postE.select("div.postinfo h2 a") : postE.select("table tbody tr td.postauthor cite a");
                if (postauthorAES.size() == 0) {
                    continue;
                }
                String uidUrl = postauthorAES.first().attr("href");
                String uid = isVoteThread ? HttpUtils.getMiddleString(uidUrl, "uid=", "") : HttpUtils.getMiddleString(uidUrl, "uid-", ".");
                Logger.v(uid);
                if (uid != null) {
                    detail.setUid(uid);
                } else {
                    continue;
                }

                String author = postauthorAES.first().text();
                if (!detail.setAuthor(author)) {
                    detail.setAuthor("[[黑名单用户]]");
                    details.add(detail);
                    continue;
                }

                //avatar
                Elements avatarES = postE.select("table tbody tr td.postauthor div.avatar img");
                if (avatarES.size() == 0) {
                    // avatar display can be closed by user
                    detail.setAvatarUrl("noavatar");
                } else {
                    if (avatarES.first().attr("src").startsWith("http")) {
                        detail.setAvatarUrl(avatarES.first().attr("src"));
                    } else {
                        detail.setAvatarUrl(HiUtils.BaseUrl+avatarES.first().attr("src"));
                    }
                }

                //content
                Contents content = detail.getContents();
                Elements postmessageES = isVoteThread ?  postE.select("div.postmessage div.t_msgfont") : postE.select("table tbody tr td.postcontent div.defaultpost div.t_msgfont");

                //locked user content
                if (postmessageES.size() == 0) {
                    postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.locked");
                    if (postmessageES.size() > 0) {
                        content.addNotice(postmessageES.text());
                        details.add(detail);
                        continue;
                    }
                }

                //poll content
                boolean isPollFirstPost = false;
                if (postmessageES.size() == 0) {
                    postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.specialmsg table tbody tr td.t_msgfont");
                    isPollFirstPost = "1".equals(floor);
                }
                if (isPollFirstPost) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.pollinfo").text()).append("<br>");
                    Elements pollOptions = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.pollchart table  tbody tr");
                    for (int j = 0; j < pollOptions.size(); j++) {
                        if (j % 2 == 0 && j < pollOptions.size() - 1)
                            sb.append(pollOptions.get(j).text());
                        if (j % 2 == 1)
                            sb.append(pollOptions.get(j).text()).append("<br>");
                    }
                    sb.append("<br>");
                    content.addText(sb.toString());
                }

                if (postmessageES.size() == 0) {
                    content.addNotice("[[!!找不到帖子内容，可能是该帖被管理员或版主屏蔽!!]]");
                    details.add(detail);
                    continue;
                }

                Element postmessageE = postmessageES.first();
                if (postmessageE.childNodeSize() == 0) {
                    content.addNotice("[[无内容]]");
                    details.add(detail);
                    continue;
                }

                //post status
            String poststatus = "";
            Elements poststatusES = postmessageE.select("i");
            if (poststatusES.size() > 0) {
                //poststatus = poststatusES.first().text();
                //remove then it will not show in content
                //保留引用者信息
                //poststatusES.first().remove();
            }

            //wap platform
            Elements postplatformES = postmessageE.select("font[color=DarkRed] font[size=2]");
            if (postplatformES.size() > 0) {
                String postplatform = postplatformES.first().text();
                if (poststatus == "") {
                    poststatus = postplatform;
                } else {
                    poststatus = postplatform + "\r\n" + poststatus;
                }
                //remove then it will not show in content
                postplatformES.first().remove();
            }

            //post rating
            Elements postratingES = postE.select("table tbody tr td.postcontent div.defaultpost fieldset ul li");
            if (postratingES.size() > 0) {
                for (Element postratingE : postratingES) {
                    if (poststatus == "") {
                        poststatus = postratingE.text();
                    } else {
                        poststatus = poststatus + "\r\n" + postratingE.text();
                    }
                }
            }

            detail.setPostStatus(poststatus);

            // Nodes including Elements(have tag) and text without tag
                TextStyleHolder textStyles = new TextStyleHolder();
                Node contentN = postmessageE.childNode(0);
                int level = 1;
                boolean processChildren;
                while (level > 0 && contentN != null) {

                    textStyles.addLevel(level);

                    processChildren = parseNode(contentN, content, level, textStyles);

                    if (processChildren && contentN.childNodeSize() > 0) {
                        contentN = contentN.childNode(0);
                        level++;
                    } else if (contentN.nextSibling() != null) {
                        contentN = contentN.nextSibling();
                        textStyles.removeLevel(level);
                    } else {
                        while (contentN.parent().nextSibling() == null) {
                            contentN = contentN.parent();
                            textStyles.removeLevel(level);
                            textStyles.removeLevel(level - 1);
                            level--;
                        }
                        contentN = contentN.parent().nextSibling();
                        textStyles.removeLevel(level);
                        textStyles.removeLevel(level - 1);
                        level--;
                    }
                }

                // IMG attachments

                Elements postimgES = postE.select("table tbody tr td.postcontent div.postmessage div.postattachlist img");
                for (int j = 0; j < postimgES.size(); j++) {
                    Element imgE = postimgES.get(j);
                    if(imgE.attr("onclick").startsWith("zoom(this") && !imgE.attr("src").contains("images/attachicons")){
                        content.addImg(imgE.attr("src"), imgE.attr("src").substring(imgE.attr("src").lastIndexOf("/")+1), true);
                    }
                }

                // other attachments
                Elements attachmentES = postE.select("dl.t_attachlist p.attachname");
                for (int j = 0; j < attachmentES.size(); j++) {
                    Element attachE = attachmentES.get(j);
                    Elements attachLinkES = attachE.select("a[href]");

                    if (attachLinkES.size() > 0) {
                        Element linkE = attachLinkES.first();
                        if (linkE.attr("href").startsWith("attachment.php?")) {
                            attachLinkES.remove();
                            String desc = attachE.text();

                            if (j == 0)
                                content.addText("<br>");
                            content.addAttach(linkE.attr("href"), linkE.text(), desc);
                        }
                    }
                }
                details.add(detail);
        }
        return details;
    }

    // return true for continue children, false for ignore children
    private static boolean parseNode(Node contentN, DetailBean.Contents content, int level, @NonNull TextStyleHolder textStyles) {

        if (contentN.nodeName().equals("font")) {
            Element elemFont = (Element) contentN;
            Element elemParent = elemFont.parent();
            if (elemFont.attr("size").equals("1")
                    || (elemParent != null
                    && elemParent.nodeName().equals("font")
                    && elemParent.attr("size").equals("1"))) {
                content.addAppMark(elemFont.text(), null);
                return false;
            } else {
                textStyles.setColor(level, Utils.nullToText(elemFont.attr("color")).trim());
                return true;
            }
        }

        if (contentN.nodeName().equals("i")    //text in an alternate voice or mood
                || contentN.nodeName().equals("u")    //text that should be stylistically different from normal text
                || contentN.nodeName().equals("em")    //text emphasized
                || contentN.nodeName().equals("strike")    //text strikethrough
                || contentN.nodeName().equals("ol")    //ordered list
                || contentN.nodeName().equals("ul")    //unordered list
                || contentN.nodeName().equals("hr")   //a thematic change in the content(h line)
                || contentN.nodeName().equals("blockquote")) {
            textStyles.addStyle(level, contentN.nodeName());
            //continue parse child node
            return true;
        } else if (contentN.nodeName().equals("strong")) {
            String tmp = ((Element) contentN).text();
            String postId = "";
            String tid = "";
            Elements floorLink = ((Element) contentN).select("a[href]");
            if (floorLink.size() > 0) {
                postId = HttpUtils.getMiddleString(floorLink.first().attr("href"), "pid=", "&");
                tid = HttpUtils.getMiddleString(floorLink.first().attr("href"), "ptid=", "&");
            }
            if (tmp.startsWith("回复 ") && tmp.length() < (3 + 6 + 15) && tmp.contains("#")) {
                int floor = HttpUtils.getIntFromString(tmp.substring(0, tmp.indexOf("#")));
                String author = tmp.substring(tmp.lastIndexOf("#") + 1).trim();
                if (!TextUtils.isEmpty(postId) && floor > 0) {
                    content.addGoToFloor(tmp, tid, postId, floor, author);
                    return false;
                }
            }
            textStyles.addStyle(level, contentN.nodeName());
            return true;
        } else if (contentN.nodeName().equals("#text")) {
            String text = ((TextNode) contentN).text();
            TextStyle ts = null;
            if (textStyles.getTextStyle(level - 1) != null)
                ts = textStyles.getTextStyle(level - 1).newInstance();

            Matcher matcher = URL_PATTERN.matcher(text);

            int lastPos = 0;
            while (matcher.find()) {
                String t = text.substring(lastPos, matcher.start());
                String url = text.substring(matcher.start(), matcher.end());

                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
                if (url.contains("@") && !url.contains("/")) {
                    content.addEmail(url);
                } else {
                    content.addLink(url, url);
                }
                lastPos = matcher.end();
            }
            if (lastPos < text.length()) {
                String t = text.substring(lastPos);
                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
            }
            return false;
        } else if (contentN.nodeName().equals("li")) {    // list item
            return true;
        } else if (contentN.nodeName().equals("br")) {    // single line break
            content.addText("<br>");
            return false;
        } else if (contentN.nodeName().equals("p")) {    // paragraph
            Element pE = (Element) contentN;
            if (pE.hasClass("imgtitle")) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("img")) {
            Element e = (Element) contentN;
            String src = e.attr("src");

            if (src.startsWith(HiUtils.SMILE_PATH)
                    || SmallImages.contains(src)) {
                //emotion added as img tag, will be parsed in TextViewWithEmoticon later
                if(!src.startsWith(HiUtils.Pic2_Url)) {
                    content.addText("<img src=\"" + HiUtils.BaseUrl + src + "\"/>");
                }
                return false;
            } else if (src.equals("images/common/none.gif") || src.startsWith("attachments/day_") || src.startsWith("attachment.php")) {
                //internal image
                content.addImg(e.attr("src"), e.attr("src").substring(e.attr("src").lastIndexOf("/")+1), true);
                return false;
            } else if (src.equals("images/common/")) {
                //skip common icons
                return false;
            } else if (src.startsWith("http://") || src.startsWith("https://")) {
                //external image
                content.addImg(src);
                return false;
            } else if (src.startsWith("images/attachicons/")) {
                //attach icon
                return false;
            } else if (src.startsWith("images/default/")) {
                //default icon
                return false;
            } else {
                content.addNotice("[[ERROR:UNPARSED IMG:" + src + "]]");
                return false;
            }
        } else if (contentN.nodeName().equals("span")) {    // a section in a document
            Elements attachAES = ((Element) contentN).select("a");
            Boolean isInternalAttach = false;
            for (int attIdx = 0; attIdx < attachAES.size(); attIdx++) {
                Element attachAE = attachAES.get(attIdx);
                //it is an attachment and not an image attachment
                if (attachAE.attr("href").startsWith("attachment.php?")
                        && !attachAE.attr("href").contains("nothumb=")) {
                    String desc = "";
                    Node sibNode = contentN.nextSibling();
                    if (sibNode != null && sibNode.nodeName().equals("#text")) {
                        desc = sibNode.toString();
                        sibNode.remove();
                    }
                    content.addAttach(attachAE.attr("href"), attachAE.text(), desc);
                    isInternalAttach = true;
                }
            }
            if (isInternalAttach) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("a")) {
            Element aE = (Element) contentN;
            String text = aE.text();
            String url = aE.attr("href");
            if (aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("img")) {
                content.addLink(url, url);
                return true;
            }

            if (aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("font") &&
                    aE.childNode(0).attr("size").equals("1")) {
                content.addAppMark(text, url);
                return false;
            }

            if (url.startsWith("attachment.php?")) {
                // is Attachment
                content.addAttach(url, text, null);
                return false;
            }

            content.addLink(text, url);
            return false;
        } else if (contentN.nodeName().equals("div")) {    // a section in a document
            Element divE = (Element) contentN;
            if (divE.hasClass("t_attach")) {
                // remove div.t_attach
                String divId = divE.attr("id");
                if (!TextUtils.isEmpty(divId) && divId.startsWith("aimg_") && divId.endsWith("_menu")) {
                    String sizeText = HttpUtils.getMiddleString(divE.text(), "(", ")");
                    long size = Utils.parseSizeText(sizeText);
                    if (size > 0) {
                        content.updateImgSize(divId.substring(0, divId.length() - 5), size);
                    }
                }
                return false;
            } else if (divE.hasClass("quote")) {
                String tid = "";
                String postId = "";
                Elements redirectES = divE.select("a");
                for (Element element : redirectES) {
                    String href = Utils.nullToText(element.attr("href"));
                    if (href.contains("redirect.php?goto=findpost")) {
                        postId = HttpUtils.getMiddleString(href, "pid=", "&");
                        tid = HttpUtils.getMiddleString(href, "ptid=", "&");
                        break;
                    }
                }
                Elements postEls = divE.select("font[size=2]");
                String authorAndTime = "";
                if (postEls.size() > 0) {
                    authorAndTime = postEls.first().text();
                    postEls.first().remove();
                }

                //remove hidden elements
                divE.select("[style*=display][style*=none]").remove();

                //only keep line break, text with styles, links
                content.addQuote(Utils.clean(divE.html()), authorAndTime, tid, postId);
                return false;
            } else if (divE.hasClass("attach_popup")) {
                // remove div.attach_popup
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("table")) {
            return true;
        } else if (contentN.nodeName().equals("tbody")) {    //Groups the body content in a table
            return true;
        } else if (contentN.nodeName().equals("tr")) {    //a row in a table
            content.addText("<br>");
            return true;
        } else if (contentN.nodeName().equals("td")) {    //a cell in a table
            content.addText(" ");
            return true;
        } else if (contentN.nodeName().equals("dl")) {    //a description list
            return true;
        } else if (contentN.nodeName().equals("dt")) {    //a term/name in a description list
            return true;
        } else if (contentN.nodeName().equals("dd")) {    //a description/value of a term in a description list
            return true;
        } else if (contentN.nodeName().equals("script") || contentN.nodeName().equals("#data")) {
            // video
            String html = contentN.toString();
            String url = HttpUtils.getMiddleString(html, "'src', '", "'");
            if (url.startsWith("http://player.youku.com/player.php")) {
                //http://player.youku.com/player.php/sid/XNzIyMTUxMzEy.html/v.swf
                //http://v.youku.com/v_show/id_XNzIyMTUxMzEy.html
                url = HttpUtils.getMiddleString(url, "sid/", "/v.swf");
                url = "http://v.youku.com/v_show/id_" + url;
                if (!url.endsWith(".html")) {
                    url = url + ".html";
                }
                content.addLink("YouKu视频自动转换手机通道 " + url, url);
            } else if (url.startsWith("http")) {
                content.addLink("FLASH VIDEO,手机可能不支持 " + url, url);
            }
            return false;
        } else {
            if (HiSettingsHelper.getInstance().isErrorReportMode()
                    && !"#comment".equals(contentN.nodeName())) {
                content.addNotice("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + ":" + contentN.toString() + "]]");
                Logger.e("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + "]]");
            }
            return false;
        }
    }

}
