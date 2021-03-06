<!DOCTYPE html>
<html>
    <head>
        <title>jb3 archives</title>
        <meta charset="UTF-8">
        <meta name="viewport" messageFilter="width=device-width, initial-scale=1.0">
        <meta name="robots" messageFilter="noindex,nofollow">
        <link rel="stylesheet" type="text/css" href="/jb3-common.css" />
        <link rel="stylesheet" type="text/css" href="/jb3-archive.css" />
        <link rel="icon" type="image/png" href="/favicon.png" />
    </head>
    <body>
    <form id="archive-search-form">
        <label for="archive-search-from">De</label>
        <input id="archive-search-from" type="text"></input>
        <label for="archive-search-to">à</label>
        <input id="archive-search-to" type="text"></input>
        <label for="archive-search-roomFilter">dans</label>
        <input id="archive-search-roomFilter" name="roomFilter" type="text" value="${(rq.roomFilter)!}"></input>
        <label for="archive-search-nicknameFilter">par</label>
        <input id="archive-search-nicknameFilter" name="nicknameFilter" type="text" value="${(rq.nicknameFilter)!}"></input>
        <label for="archive-search-messageFilter">disant</label>
        <input id="archive-search-messageFilter" name="messageFilter" type="text" value="${(rq.messageFilter)!}"></input>
        <input id="archive-search-from-hidden" name="from" type="hidden" value="${(rq.from?c)!}"></input>
        <input id="archive-search-to-hidden" name="to" type="hidden" value="${(rq.to?c)!}"></input>
        <input type="submit"></input>
    </form>
    <#if posts?? >
    <div class="jb3-posts">
        <#list posts as post>
        <div id="${post.id}" class="jb3-post">
            <span class="jb3-post-time">${post.time.getMillis()?c}</span>
            <span class="jb3-post-room">${(post.room)!}</span>
            <span class="jb3-post-nickname">${(post.nickname)!}</span>
            <span class="jb3-post-message">${(post.message)!}</span>
        </div>
        </#list>
    </div>
    </#if>
    <div class="jb3-archive-pager">
        <#if rq.page &gt; 0 >
        <form>
            <input name="from" type="hidden" value="${(rq.from?c)!}"></input>
            <input name="to" type="hidden" value="${(rq.to?c)!}"></input>
            <input name="roomFilter" type="hidden" value="${(rq.roomFilter)!}"></input>
            <input name="nicknameFilter" type="hidden" value="${(rq.nicknameFilter)!}"></input>
            <input name="messageFilter" type="hidden" value="${(rq.messageFilter)!}"></input>
            <input name="page" type="hidden" value="${(rq.page - 1)!}"></input>
            <input type="submit" value="Précédents"></input>
        </form>
        </#if>
        <#if posts?? && posts?has_content >
        <form>
            <input name="from" type="hidden" value="${(rq.from?c)!}"></input>
            <input name="to" type="hidden" value="${(rq.to?c)!}"></input>
            <input name="roomFilter" type="hidden" value="${(rq.roomFilter)!}"></input>
            <input name="nicknameFilter" type="hidden" value="${(rq.nicknameFilter)!}"></input>
            <input name="messageFilter" type="hidden" value="${(rq.messageFilter)!}"></input>
            <input name="page" type="hidden" value="${(rq.page + 1)!}"></input>
            <input type="submit" value="Suivants"></input>
        </form>
        </#if>
    </div>
    </body>
    <script src="/webjars/jquery/2.1.1/jquery.js" defer></script>
    <script src="/webjars/momentjs/2.8.3/moment.js" defer/></script>
    <script src="/jb3-common.js" defer/></script>
    <script src="/jb3-archive.js" defer/></script>
</html>