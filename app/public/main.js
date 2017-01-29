/**
 * Created by Dennis on 11-1-2017.
 * https://github.com/topojson/topojson
 * examples: http://bl.ocks.org/mbostock  http://techslides.com/over-1000-d3-js-examples-and-demos
 * geojson2topojson: http://geojson.io
 */
console.log("d3 loaded: " + d3.version);

var x = new CollumSelector(),
    y = new CollumSelector(),
    chart = new Chart(x, y),
    list = new ListSelector(),
    map = new GeoMap(list),
    data = new TopoJsonData();

data.get(function () {
    map.setData(data);
});
