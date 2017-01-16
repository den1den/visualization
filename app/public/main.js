/**
 * Created by Dennis on 11-1-2017.
 * https://github.com/topojson/topojson
 * examples: http://bl.ocks.org/mbostock  http://techslides.com/over-1000-d3-js-examples-and-demos
 * geojson2topojson: http://geojson.io
 */
console.log("d3 loaded: "+d3.version);
var us = false;

var width = 500,
    height = $(window).height() - 100,
    active = d3.select(null);

var projection = us ? d3.geoAlbersUsa() : d3.geoMercator();
projection.scale(1000)
    .translate([width / 2, height / 2]);
var path = d3.geoPath()
    .projection(projection);

var zoom = d3.zoom()
    .scaleExtent([1, 8])
    .on("zoom", zoomed);

var svg = d3.select("svg")
    .attr("width", width)
    .attr("height", height)
    .on('click', stopped);
svg.append('rect')
    .attr('class', 'background')
    .attr("width", width)
    .attr("height", height)
    .on('click', reset);
//svg.call(zoom);
var g = svg.append("g");


var data_url = us ? '/us.json' : "/buurten.topojson";
d3.json(data_url, function (error, topo) {
    if (error) throw error;

    var data_key = us ? 'states' : 'collection';

    var plane = topojson.feature(topo, topo.objects[data_key]);
    var mesh = topojson.mesh(topo, topo.objects[data_key], function (a, b) {
        return a !== b;
    });

    var b, s, t; // boxing limits
    projection.scale(1).translate([0, 0]);
    b = path.bounds(plane);
    s = .8 / Math.max((b[1][0] - b[0][0]) / width, (b[1][1] - b[0][1]) / height);
    t = [(width - s * (b[1][0] + b[0][0])) / 2, (height - s * (b[1][1] + b[0][1])) / 2];
    projection.scale(s).translate(t);

    g.selectAll('path')
        .data(plane.features)
        .enter()
        .append('path')
        .attr("class", "feature")
        .attr("d", path)
        .on("click", clicked);

    g.append("path")
        .datum(mesh)
        .attr("class", "mesh")
        .attr("d", path);
});

function clicked(d) {
    if (active.node() === this) return reset();
    active.classed("active", false);
    active = d3.select(this).classed("active", true);

    var bounds = path.bounds(d),
        dx = bounds[1][0] - bounds[0][0],
        dy = bounds[1][1] - bounds[0][1],
        x = (bounds[0][0] + bounds[1][0]) / 2,
        y = (bounds[0][1] + bounds[1][1]) / 2,
        scale = Math.max(1, Math.min(8, 0.9 / Math.max(dx / width, dy / height))),
        translate = [width / 2 - scale * x, height / 2 - scale * y];

    svg.transition()
        .duration(750)
        .call( zoom.transform, d3.zoomIdentity.translate(translate[0],translate[1]).scale(scale) ); // updated for d3 v4
}

function reset() {
    active.classed("active", false);
    active = d3.select(null);

    svg.transition()
        .duration(750)
        .call( zoom.transform, d3.zoomIdentity ); // updated for d3 v4
}

function zoomed() {
    g.style("stroke-width", 1.5 / d3.event.transform.k + "px");
    g.attr("transform", d3.event.transform);
}

// If the drag behavior prevents the default click,
// also stop propagation so we don’t click-to-zoom.
function stopped() {
    if (d3.event.defaultPrevented) d3.event.stopPropagation();
}
