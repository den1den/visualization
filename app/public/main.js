/**
 * Created by Dennis on 11-1-2017.
 * https://github.com/topojson/topojson
 * examples: http://bl.ocks.org/mbostock  http://techslides.com/over-1000-d3-js-examples-and-demos
 * geojson2topojson: http://geojson.io
 */
console.log("d3 loaded: " + d3.version);

var index = 0;
var active = d3.select(null);

loadData(function (data) {
    geoSetup(data);
});

function updateSelected(i){
    var old_index = index;
    index = i;

    geoChange(old_index);
}
