var express = require("express");
var logfmt = require("logfmt");
var app = express();

app.use(logfmt.requestLogger());
app.use(express.static('public'));
app.use(express.static('data'));

var PORT = 8009;
app.listen(PORT, function() {
    console.log("Listening on " + PORT);
});
