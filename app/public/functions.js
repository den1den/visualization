/**
 * Created by Dennis on 27-1-2017.
 */

function jsonLoad(url, callback) {
    d3.json(url, function (error, data) {
        if (error) throw error;
        console.log("Loaded json " + data_url);
        callback(data);
    });
}

function loadData(url, callback) {
    d3.request(url)
        .mimeType("text/csv")
        .response(function (xhr) {
            return d3.dsvFormat(";").parse(xhr.responseText, function (d) {
                    d2 = {};
                    for (var key in d) {
                        if (d.hasOwnProperty(key)) {
                            if(key == 'Jaar'){
                                d2[key] = new Date(+d[key], 0, 1);
                            } else if (key == 'Buurt') {
                                d2[key] = d[key]
                            } else {
                                d2[key] = +d[key]
                            }
                        }
                    }
                    return d2;
                }
            );
        })
        .get(function (error, data) {
            if (error) throw error;
            console.log("Loaded csv " + data_url);
            callback(data);
        });
}
