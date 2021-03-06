package im.bci.jb3.controllers;

import im.bci.jb3.backend.legacy.LegacyBoard;
import im.bci.jb3.backend.legacy.LegacyPost;
import im.bci.jb3.data.Post;
import im.bci.jb3.data.PostRepository;
import im.bci.jb3.legacy.LegacyUtils;
import im.bci.jb3.logic.TribuneService;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author devnewton
 */
@Controller
@RequestMapping("/legacy")
public class LegacyController {

    @Value("${jb3.host}")
    private String site;

    @Autowired
    private TribuneService tribune;

    @Autowired
    private PostRepository postPepository;

    @Autowired
    private LegacyUtils legacyUtils;

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String post(@RequestParam(value = "nickname", required = false) String nickname, @RequestParam(value = "message") String message, @RequestParam(value = "room", required = false) String room, @RequestParam(value = "last", required = false) Long last, @RequestHeader(value = "User-Agent", required = false) String userAgent, WebRequest webRequest, Model model, HttpServletResponse response) {
        if (StringUtils.isBlank(nickname)) {
            nickname = userAgent;
        }
        Post post = tribune.post(nickname, legacyUtils.convertFromLegacyNorloges(room, message), room);
        if (null != post) {
            response.addHeader("X-Post-Id", Long.toString(post.getTime().getMillis()));
        }
        return xml(room, last, webRequest, model, response);
    }

    @RequestMapping(value = "/xml")
    public String xml(@RequestParam(value = "room", required = false) String room, @RequestParam(value = "last", required = false) Long lastId, WebRequest webRequest, Model model, HttpServletResponse response) {
        if (get(lastId, room, webRequest, model, xmlEscaper)) {
            response.setContentType("text/xml");
            return "legacy/xml";
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/tsv")
    public String tsv(@RequestParam(value = "room", required = false) String room, @RequestParam(value = "last", required = false) Long lastId, WebRequest webRequest, Model model, HttpServletResponse response) {
        if (get(lastId, room, webRequest, model, csvEscaper)) {
            response.setContentType("text/tab-separated-values");
            response.setHeader("Content-Disposition", "attachment; filename=\"backend.tsv\"");
            return "legacy/tsv";
        } else {
            return null;
        }
    }

    private boolean get(Long lastId, String room, WebRequest webRequest, Model model, Escaper escaper) {
        DateTime end = DateTime.now(DateTimeZone.UTC).plusHours(1);
        DateTime start = computeStartTime(lastId, end);
        List<Post> posts = postPepository.findPosts(start, end, room);
        if (posts.isEmpty() || webRequest.checkNotModified(posts.get(0).getTime().getMillis())) {
            return false;
        } else {
            LegacyBoard board = new LegacyBoard();
            board.setSite(site);
            board.setTimezone(LegacyUtils.legacyTimezoneId);
            List<LegacyPost> legacyPosts = new ArrayList<LegacyPost>(posts.size());
            for (Post post : posts) {
                LegacyPost legacyPost = new LegacyPost();
                legacyPost.setId(post.getTime().getMillis());
                legacyPost.setTime(LegacyUtils.legacyPostTimeFormatter.print(post.getTime()));
                String info = Jsoup.clean(post.getNickname(), Whitelist.none());
                String message = Jsoup.clean(legacyUtils.convertToLegacyNorloges(convertUrls(post.getMessage()), post.getTime()), messageWhitelist);
                legacyPost.setInfo(escaper.escape(info));
                legacyPost.setMessage(escaper.escape(message));
                legacyPosts.add(legacyPost);
            }
            board.setPosts(legacyPosts);
            model.addAttribute("board", board);
            return true;
        }
    }

    private static interface Escaper {

        String escape(String s);
    }

    private static final Escaper xmlEscaper = new Escaper() {

        @Override
        public String escape(String s) {
            return StringEscapeUtils.escapeXml10(s);
        }
    };
    private static final Escaper csvEscaper = new Escaper() {

        @Override
        public String escape(String s) {
            return s.replaceAll("\\p{C}", " ");

        }
    };

    private DateTime computeStartTime(Long lastId, DateTime end) {
        DateTime start;
        if (null != lastId) {
            start = new DateTime(lastId, DateTimeZone.UTC);
            if (start.isAfter(end)) {
                start = end.minusSeconds(1);
            }
        } else {
            start = end.minusWeeks(1);
        }
        //workaround shameful olcc new year bug
        if (start.getYear() < end.getYear()) {
            start = new DateTime(end.getYear(), 1, 1, 0, 0, DateTimeZone.UTC);
        }
        return start;
    }

    private static final Pattern urlPattern = Pattern.compile("(https?|ftp|gopher)://[^\\s]+");

    private final Whitelist messageWhitelist = Whitelist.none().addTags("b", "i", "s", "u", "tt", "a").addAttributes("a", "href", "rel", "target");

    private String convertUrls(String message) {
        Matcher matcher = urlPattern.matcher(message);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<a href=\"$0\" rel=\"nofollow\" target=\"_blank\">[url]</a>");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
