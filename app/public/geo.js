/**
 * Created by Dennis on 28-1-2017.
 */

var width = 500,
    height = 500,
    geos = [];

var projection = d3.geoMercator();
projection.scale(1000).translate([width / 2, height / 2]);
var path = d3.geoPath().projection(projection);

// Setup root element
var svg = d3.select("#geo")
    .attr("width", width)
    .attr("height", height)
    .on('click', stopped);
// Add background
svg.append('rect')
    .attr('class', 'background')
    .attr("width", width)
    .attr("height", height)
    .on('click', reset);
var geo_group = svg.append("g");
var pathGs = [
    geo_group.append('g').attr('id', 'stadsdelen'),
    geo_group.append('g').attr('id', 'wijken').style("display", "none"),
    geo_group.append('g').attr('id', 'buurten').style("display", "none")
];

var lists = [
    d3.select('#list-stadsdeel'),
    d3.select('#list-wijk').style("display", "none"),
    d3.select('#list-buurt').style("display", "none")
];
var listButtons = [
    d3.select("#list-tab-stadsdeel"),
    d3.select("#list-tab-wijk"),
    d3.select("#list-tab-buurt")
];

function geoSetup(us) {
    geos[0] = topojson.feature(us, us.objects.stadsdeel);
    geos[1] = topojson.feature(us, us.objects.wijken);
    geos[2] = topojson.feature(us, us.objects.buurten);

    // calculate the zoom by a bounding box
    var b, s, t;
    projection.scale(1).translate([0, 0]);
    b = path.bounds(geos[0]);
    s = .8 / Math.max((b[1][0] - b[0][0]) / width, (b[1][1] - b[0][1]) / height);
    t = [(width - s * (b[1][0] + b[0][0])) / 2, (height - s * (b[1][1] + b[0][1])) / 2];
    projection.scale(s).translate(t);

    pathGs[0].selectAll('path')
        .data(geos[0].features)
        .enter()
        .append('path')
        .attr('id', function (d) {
            return 's' + d.properties.stadsdeelcode;
        })
        .attr('d', path)
        .on("click", stadsdeel_clicked);

    pathGs[1].selectAll('path')
        .data(geos[1].features)
        .enter()
        .append('path')
        .attr('id', function (d) {
            return 'w' + d.properties.wijkcode;
        })
        .attr('d', path)
        .on("click", wijk_clicked);

    pathGs[2].selectAll('path')
        .data(geos[2].features)
        .enter()
        .append('path')
        .attr('id', function (d) {
            return 'b' + d.properties.buurtcode;
        })
        .attr('d', path)
        .on("click", buurt_clicked);

    var i = -1;
    while (++i <= 2){
        geos[i].features.sort(sortByProperty(propertyNaam[i])).forEach(function (d){
            lists[i].append('li')
                .attr('class', 'list-group-item')
                .append('span')
                .attr('class', 'tag tag-default tag-pill float-xs-right')
                .text(d.properties[propertyNaam[0]]);
        });
    }
}

function geoChange(old_index) {
    // Setup the list

    pathGs[old_index].style("display", "none");
    pathGs[index].style("display", "inline");

    buttons[old_index].attr('class', 'nav-link');
    buttons[index].attr('class', 'nav-link active');

    listTabs[old_index].style("display", "none");
    listTabs[index].style("display", "flex");
}

function stadsdeel_clicked(d) {
    console.log("stadsdeel_clicked");
    console.log(d);

    pathGs[0].style("display", "none");
    pathGs[1].style("display", "inline");
    pathGs[2].style("display", "none");

    clicked(d, 1);
}

function wijk_clicked(d) {
    console.log("wijk_clicked");
    console.log(d);

    pathGs[0].style("display", "none");
    pathGs[1].style("display", "none");
    pathGs[2].style("display", "inline");

    clicked(d, 2);
}

function buurt_clicked(d) {
    console.log("buurt_clicked");
    console.log(d);

    pathGs[0].style("display", "inline");
    pathGs[1].style("display", "none");
    pathGs[2].style("display", "none");

    reset();
}

// Zoom into area
function clicked(d, index) {
    // if (active.node() === this) return reset();
    // active.classed("active", false);
    // active = d3.select(this).classed("active", true);
    console.log("Clicked on: " + d);

    d3.select('#woonwijk-field').text(d.properties[propertyNaam[index]]);
    d3.select('#data-field').text(JSON.stringify(d.properties.data));

    var bounds = path.bounds(d),
        dx = bounds[1][0] - bounds[0][0],
        dy = bounds[1][1] - bounds[0][1],
        x = (bounds[0][0] + bounds[1][0]) / 2,
        y = (bounds[0][1] + bounds[1][1]) / 2,
        scale = Math.max(1, Math.min(8, 0.9 / Math.max(dx / width, dy / height))),
        translate = [width / 2 - scale * x, height / 2 - scale * y];

    svg.transition()
        .duration(750)
        .call(zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale)); // updated for d3 v4
}

// On click outside an area
function reset() {
    active.classed("active", false);
    active = d3.select(null);

    svg.transition()
        .duration(750)
        .call(zoom.transform, d3.zoomIdentity); // updated for d3 v4
}

// Add zooming functionality
var zoom = d3.zoom()
    .scaleExtent([1, 8])
    .on("zoom", zoomed);
//svg.call(zoom);
// On zoom update
function zoomed() {
    geo_group.style("stroke-width", 1.5 / d3.event.transform.k + "px");
    geo_group.attr("transform", d3.event.transform);
}

listButtons.forEach(function (el, i) {
    el.on('click', function () {
        updateSelected(i);
    });
});
