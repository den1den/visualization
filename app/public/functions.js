/**
 * Created by Dennis on 27-1-2017.
 */

var objectKey = ['stadsdeel', 'wijken', 'buurten'];
var propertyNaam = ['stadsdeelnaam', 'wijknaam', 'buurtnaam'];

function TopoJsonData(){
    var data = null;
    this.get = function (callback) {
        d3.json('combined.topojson', function (error, _data) {
            if (error) throw error;
            console.log("Loaded combined.topojson");
            data = _data;
            callback();
        });
    };
    this.getMesh = function(index) {
        return topojson.mesh(data, data.objects[objectKey[index]], neq);
    };
    this.getFeature = function(index) {
        return topojson.feature(data, data.objects[objectKey[index]]);
    };
}

function loadDatas(callback) {
    d3.queue()
        .defer(d3.json, '/stadsdeel.topojson')
        .defer(d3.json, '/wijken.topojson')
        .defer(d3.json, '/buurten-simple.topojson')
        .awaitAll(function (error, datas) {
            if (error) throw error;
            console.log("Data loaded");
            callback(datas);
        });
}
function loadCsv(callback) {
    d3.request('/buurten.topojson')
        .mimeType("text/csv")
        .response(function (xhr) {
            return d3.dsvFormat(";").parse(xhr.responseText, function (d) {
                    d2 = {};
                    for (var key in d) {
                        if (d.hasOwnProperty(key)) {
                            if (key == 'Jaar') {
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
            console.log("Data loaded");
            callback(data);
        });
}


function sortByProperty(key) {
    return function(a, b)
    {
        if (a.properties[key] < b.properties[key]) {
            return -1;
        } else if (a.properties[key] > b.properties[key]) {
            return 1;
        }
        return 0;
    }
}

// If the drag behavior prevents the default click,
// also stop propagation so we don’t click-to-zoom.
function stopped() {
    if (d3.event.defaultPrevented) d3.event.stopPropagation();
}

function neq(a, b){
    return a !== b;
}