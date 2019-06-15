var e = $('#error');
var m = $('#error-msg');
var s = $('#save');
var f = $('#create_user');

$(document).ready(function () {

    var username = $('#username');

    username.change(function () {

        if (username.length < 1) {
            return false;
        }

        axios.get("/api/validate/username/" + username.val())
            .then(function (response) {
                var valid = response.data.body;
                if (valid) {
                    s.show();
                    e.hide();
                } else {
                    s.hide();
                    m.text("Username already in use!");
                    e.show();
                }
            });

    });

    s.click(function () {
        if (username.length < 1) {
            return false;
        }

        axios.get("/api/validate/username/" + username.val())
            .then(function (response) {
                var valid = response.data.body;
                if (valid) {
                    e.hide();
                    f.submit();
                } else {
                    s.hide();
                    m.text("Username already in use!");
                    e.show();
                }
            });
    });
});
