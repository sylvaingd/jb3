jb3 = {
    init: function () {
        var self = this;

        var controlsMessage = $('#jb3-controls-message');
        controlsMessage.bind('keypress', function (e) {
            if (e.keyCode === 13) {
                self.postMessage(controlsMessage.val());
                controlsMessage.val('');
            }
        });

        $('#jb3-controls-refresh').click(function (e) {
            self.refreshMessages();
        });
    },
    postMessage: function (message) {
        $.ajax({
            type: "POST",
            url: "/api/post",
            data: {message: message}
        });
    },
    refreshMessages: function () {
        var self = this;
        $.ajax({
            dataType: "json",
            type: "GET",
            url: "/api/get",
            success: function (data) {
                self.onNewMessages(data);
            }
        });
    },
    onNewMessages: function (data) {
        var self = this;
        $.each(data.content, function (index, value) {
            self.onMessage(value);
        }
        );
        self.sortMessages();
    },
    onMessage: function (message) {
        var messagesContainer = $('#jb3-messages');
        var existingMessageDiv = messagesContainer.find('#' + message.id);
        if (existingMessageDiv.length === 0) {
            var isoTime = new Date(message.time).toISOString();
            var timeSpan = $('<span/>').addClass('jb3-post-time').text(isoTime.substr(11,8)).attr("title",isoTime);
            var nickSpan = $('<span/>').addClass('jb3-post-nickname').text(message.nickname);
            var messageSpan = $('<span/>').addClass('jb3-post-message').text(message.message);
            var messageDiv = $('<div/>').attr('id', message.id).addClass('jb3-post').attr('time', message.time).append(timeSpan).append(nickSpan).append(messageSpan);
            messagesContainer.append(messageDiv);
        }
    },
    sortMessages: function () {
        $('#jb3-messages').find('.jb3-post').sort(function (a, b) {
            return $(a).attr('time') - $(b).attr('time');
        }).appendTo('#jb3-messages');
    }
};
jb3.init();


