<#import "fortune.ftl" as fortuneMacros />
<!DOCTYPE html>
<html>
    <head>
        <title>jb3 fortunes</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="robots" content="noindex,nofollow">
        <link rel="stylesheet" type="text/css" href="/jb3-common.css" />
        <link rel="stylesheet" type="text/css" href="/jb3-fortune.css" />
        <link rel="icon" type="image/png" href="/favicon.png" />
    </head>
    <body>
    <form id="fortune-search-form">
        <label for="fortune-search-from">De</label>
        <input id="fortune-search-from" type="text"></input>
        <label for="fortune-search-to">à</label>
        <input id="fortune-search-to" type="text"></input>
        <label for="fortune-search-nicknameFilter">par</label>
        <input id="fortune-search-nicknameFilter" name="nicknameFilter" type="text" value="${(rq.nicknameFilter)!}"></input>
        <label for="fortune-search-messageFilter">disant</label>
        <input id="fortune-search-messageFilter" name="messageFilter" type="text" value="${(rq.messageFilter)!}"></input>
        <input id="fortune-search-from-hidden" name="from" type="hidden" value="${(rq.from?c)!}"></input>
        <input id="fortune-search-to-hidden" name="to" type="hidden" value="${(rq.to?c)!}"></input>
        <input type="submit"></input>
    </form>
    <#if fortunes?? >
    <div class="jb3-fortunes">
        <#list fortunes as fortune>
        <@fortuneMacros.showFortune fortune />
        </#list>
    </div>
    </#if>
    <div class="jb3-fortune-pager">
        <#if rq.page &gt; 0 >
        <form>
            <input name="from" type="hidden" value="${(rq.from?c)!}"></input>
            <input name="to" type="hidden" value="${(rq.to?c)!}"></input>
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
    <script src="/jb3-fortune.js" defer/></script>
</html>