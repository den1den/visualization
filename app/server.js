var express = require("express");
var logfmt = require("logfmt");
var app = express();

app.use(function(req, res, next) {
    res.header("Cache-Control", "no-cache, no-store, must-revalidate");
    res.header("Pragma", "no-cache");
    res.header("Expires", 0);
    next();
});
app.use(logfmt.requestLogger());
app.use(express.static('public'));
app.use(express.static('static'));

var PORT = 8009;
app.listen(PORT, function() {
    console.log("Listening on " + PORT);
});
