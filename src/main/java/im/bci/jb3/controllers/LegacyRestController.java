package im.bci.jb3.controllers;

import im.bci.jb3.backend.legacy.LegacyBoard;
import im.bci.jb3.backend.legacy.LegacyPost;
import im.bci.jb3.data.Post;
import im.bci.jb3.data.PostRepository;
import im.bci.jb3.logic.Norloge;
import im.bci.jb3.logic.TribuneService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author devnewton
 */
@RestController
@RequestMapping("/legacy")
public class LegacyRestController {

    @Value("${jb3.host}")
    private String site;

    @Autowired
    private TribuneService tribune;

    @Autowired
    private PostRepository postPepository;

    @RequestMapping("/post")
    public void post(@RequestParam(value = "nickname", required = false) String nickname, @RequestParam(value = "message") String message, @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (StringUtils.isBlank(nickname)) {
            nickname = userAgent;
        }
        tribune.post(nickname, convertFromLegacyNorloges(message));
    }

    private static final String legacyTimezoneId = "Europe/Paris";
    private static final DateTimeZone legacyTimeZone = DateTimeZone.forID(legacyTimezoneId);
    private static final DateTimeFormatter legacyPostTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(legacyTimeZone);

    @RequestMapping(value = "/xml", produces = {"application/xml", "text/xml"})
    public LegacyBoard xml(WebRequest webRequest) {
        List<Post> posts = tribune.get();
        if (posts.isEmpty() || webRequest.checkNotModified(posts.get(0).getTime().getTime())) {
            return null;
        } else {
            LegacyBoard board = new LegacyBoard();
            board.setSite(site);
            board.setTimezone(legacyTimezoneId);
            List<LegacyPost> legacyPosts = new ArrayList<LegacyPost>();
            for (Post post : posts) {
                LegacyPost legacyPost = new LegacyPost();
                final long time = post.getTime().getTime();
                legacyPost.setId(time);
                legacyPost.setTime(legacyPostTimeFormatter.print(time));
                legacyPost.setInfo(post.getNickname());
                legacyPost.setMessage(convertToLegacyNorloges(convertUrls(post.getMessage()), post.getTime()));
                legacyPost.setLogin("");
                legacyPosts.add(legacyPost);
            }
            board.setPost(legacyPosts);
            return board;
        }
    }

    private static final DateTimeFormatter toLegacyFullNorlogeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd#HH:mm:ss").withZone(legacyTimeZone);
    private static final DateTimeFormatter toLegacyLongNorlogeFormatter = DateTimeFormat.forPattern("MM/dd#HH:mm:ss").withZone(legacyTimeZone);
    private static final DateTimeFormatter toLegacyNormalNorlogeFormatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(legacyTimeZone);
    private static final DateTimeFormatter toLegacyShortNorlogeFormatter = DateTimeFormat.forPattern("HH:mm").withZone(legacyTimeZone);

    private String convertToLegacyNorloges(String message, Date messageDate) {
        final DateTime postTime = new DateTime(messageDate);
        final StringBuffer sb = new StringBuffer();
        Norloge.forEachNorloge(message, new Norloge.NorlogeProcessor() {

            @Override
            public void process(Norloge norloge, Matcher matcher) {
                if (null != norloge.getId()) {
                    Post post = postPepository.findOne(norloge.getId());
                    if (null != post) {
                        final DateTime referencedPostTime = new DateTime(post.getTime());
                        DateTimeFormatter formatter = findLegacyNorlogeFormatter(postTime, referencedPostTime);
                        matcher.appendReplacement(sb, formatter.print(referencedPostTime));
                        return;
                    }
                }
                matcher.appendReplacement(sb, norloge.toString());
            }

            @Override
            public void end(Matcher matcher) {
                matcher.appendTail(sb);
            }

        });
        return sb.toString();
    }

    private DateTimeFormatter findLegacyNorlogeFormatter(DateTime postTime, DateTime referencedPostTime) {
        if (Days.daysBetween(postTime, referencedPostTime).isLessThan(Days.ONE)) {
            if (referencedPostTime.getSecondOfMinute() == 0) {
                return toLegacyShortNorlogeFormatter;
            } else {
                return toLegacyNormalNorlogeFormatter;
            }
        } else if (Years.yearsBetween(postTime, referencedPostTime).isLessThan(Years.ONE)) {
            return toLegacyLongNorlogeFormatter;
        } else {
            return toLegacyFullNorlogeFormatter;
        }
    }

    private String convertFromLegacyNorloges(String message) {
        final StringBuffer sb = new StringBuffer();
        Norloge.forEachNorloge(message, new Norloge.NorlogeProcessor() {

            @Override
            public void process(Norloge norloge, Matcher matcher) {
                DateTime time = norloge.getTime();
                if (null != time) {
                    time = time.withZoneRetainFields(legacyTimeZone);
                    Post post = postPepository.findOne(time, time.plusSeconds(1));
                    if (null != post) {
                        matcher.appendReplacement(sb, Norloge.format(post));
                        return;
                    }
                }
                matcher.appendReplacement(sb, norloge.toString());
            }

            @Override
            public void end(Matcher matcher) {
                matcher.appendTail(sb);
            }
        });
        return sb.toString();
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
        return Jsoup.clean(sb.toString(), messageWhitelist);
    }

}
