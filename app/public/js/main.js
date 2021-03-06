/**
 * Created by Dennis on 11-1-2017.
 * https://github.com/topojson/topojson
 * examples: http://bl.ocks.org/mbostock  http://techslides.com/over-1000-d3-js-examples-and-demos
 * geojson2topojson: http://geojson.io
 */
/*global $,d3*/
console.log("d3 loaded: " + d3.version);
var dataTypeX = new DataType(0, {"owner": "all", "source": "all", "agg": "count", "zero": false}),
    dataTypeY = new DataType(1, {"owner": "all", "source": "co2", "agg": "value", "zero": true}),
    yearSelection = new YearSelection();

var x = new DataTypeSelector("#select-x", dataTypeX),
    y = new DataTypeSelector("#select-y", dataTypeY),
    selection = new SelectionTitle("#selection-title"),
    chart = new Chart("#chart", dataTypeX, dataTypeY),
    yearSelector = new YearSelector("#years-legend", yearSelection),
    list = new ListSelector(),
    map = new GeoMap("#geo", dataTypeY),
    data = new TopoJsonData();

data.get(function () {
    map.bindData(data);
    list.bindData(data);
    chart.bindData(data);
});
//TODO: annnotation gives wrong value when using Custom expression
//TODO: "Bloemenbuurt-West" neighberhood has a glitch