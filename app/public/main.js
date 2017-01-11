/**
 * Created by Dennis on 11-1-2017.
 */
console.log("d3 loaded: "+d3.version);

var width = $(window).width(),
    height = $(window).height();
var sc = Math.min(width, height) * 0.5;

// var projection = d3.geo.equirectangular()
//     .scale(sc)
//     .translate([width/2,height/2])
//     .rotate([-180,0]);
var projection = d3.geoMercator()
    .scale((1 << 22) / 2 / Math.PI)
    .translate([width / 2, height / 2]);
var path = d3.geoPath().projection(projection);

var svg = d3.select("svg")
    .attr("width", width)
    .attr("height", height);
var center = projection([-1.79636106388265, 46.7740619963089]);

// var zoom = d3.zoom()
//     .scale(projection.scale() * 2 * Math.PI)
//     .scaleExtent([1 << 22, 1 << 28])
//     .translate([width - center[0], height - center[1]])
//     .on("zoom", zoomed);

var vector = svg;

d3.json("/buurten.topojson", function (error, buurten) {
    if (error) throw error;

    var plane = topojson.feature(buurten, buurten.objects.collection);

    var b, s, t; // boxing limits
    projection.scale(1).translate([0, 0]);
    b = path.bounds(plane);
    console.log(b);
    s = .9 / Math.max((b[1][0] - b[0][0]) / width, (b[1][1] - b[0][1]) / height);
    t = [(width - s * (b[1][0] + b[0][0])) / 2, (height - s * (b[1][1] + b[0][1])) / 2];
    projection.scale(s).translate(t);

    vector = svg.selectAll("path")
        .data(plane.features)
        .enter()
        .append("path")
        .attr("class", "county")
        .attr("d", path);


    projection
        .scale(zoom.scale() / 2 / Math.PI)
        .translate(zoom.translate());
    vector.attr("d", path);
});

function zoomed() {
}
