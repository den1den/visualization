/**
 * Created by Dennis on 28-1-2017.
 */



function GeoMap() {
    this.width = 500;
    this.height = 500;
    this.geos = [];

    this.projection = d3.geoMercator();
    this.projection.scale(1000).translate([this.width / 2, this.height / 2]);
    this.path = d3.geoPath().projection(this.projection);

    this.colors = ['#e5f5f9','#99d8c9','#2ca25f'];

    // Setup root element
    var svg = d3.select("#geo")
        .attr("width", this.width)
        .attr("height", this.height)
        .on('click', stopped);
    // Add background
    svg.append('rect')
        .attr('class', 'background')
        .attr("width", this.width)
        .attr("height", this.height)
        .on('click', this.getResetZoom());
    this.root = svg.append("g");

    // Setup zooming behaviour
    this.zoom = d3.zoom()
        .scaleExtent([1, 8])
        .on("zoom", this.getOnZoom());
    //svg.call(this.root);
}
GeoMap.prototype.draw = function () {
    this.initZoom(getMesh(2));

    function append(index, show) {
        var a = this.root
            .append("g").attr("id", "level"+index+"-area");
        a.selectAll('path')
            .data(getFeature(index).features).enter()
            .append('path').attr("d", this.path)
            .on('click', this.getClicked(index));
        var b = this.root.append('g').attr("id", "level"+index+"-border");
        b.append("path")
            .datum(getMesh(index))
            .attr("class", "mesh")
            .attr("d", this.path);

        if(!show){
            a.style("display", "none");
            b.style("display", "none");
        }
    }
    append.call(this, 0, true);
    append.call(this, 1, false);
    append.call(this, 2, false);

};
GeoMap.prototype.initZoom = function (hasBounds) {
    this.projection.scale(1).translate([0, 0]);
    var s, t,
        b = this.path.bounds(hasBounds),
        dx = b[1][0] - b[0][0],
        dy = b[1][1] - b[0][1];
    s = 0.9 / Math.max(dx / this.width, dy / this.height);
    t = [(this.width - s * (b[1][0] + b[0][0])) / 2, (this.height - s * (b[1][1] + b[0][1])) / 2];
    this.projection.scale(s).translate(t);
};
GeoMap.prototype.getOnZoom = function () {
    var _this = this;
    return function () {
        //_this.root.style("stroke-width", 1.5 / d3.event.transform.k + "px");
        _this.root.attr("transform", d3.event.transform);
    };
};
GeoMap.prototype.getResetZoom = function () {
    var _this = this;
    return function () {
        d3.select("#level0-area").style("display", "inherit");
        d3.select("#level0-border").style("display", "inherit");
        d3.select("#level1-area").style("display", "none");
        d3.select("#level1-border").style("display", "none");
        d3.select("#level2-area").style("display", "none");
        d3.select("#level2-border").style("display", "none");

        _this.root.transition()
            .duration(750)
            .call(_this.zoom.transform, d3.zoomIdentity); // updated for d3 v4
    };
};
GeoMap.prototype.getClicked = function (index) {
    var _this = this;
    return function (d) {
        console.log("Clicked on: " + d);

        d3.select('#select-name').text(d.properties[propertyNaam[index]]);
        d3.select('#select-data').text(JSON.stringify(d.properties));

        if(index < 2){
            var id = '#level' + (index);
            d3.select(id + "-area").style("display", "none");
            d3.select(id + "-border").style("display", "none");
            id = '#level' + (index + 1);
            d3.select(id + "-area").style("display", "inherit");
            d3.select(id + "-border").style("display", "inherit");
        }

        var bounds = _this.path.bounds(d),
            dx = bounds[1][0] - bounds[0][0],
            dy = bounds[1][1] - bounds[0][1],
            x = (bounds[0][0] + bounds[1][0]) / 2,
            y = (bounds[0][1] + bounds[1][1]) / 2,
            scale = Math.max(1, Math.min(8, 0.9 / Math.max(dx / _this.width, dy / _this.height))),
            translate = [_this.width / 2 - scale * x, _this.height / 2 - scale * y];

        _this.root.transition()
            .duration(750)
            .call(_this.zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale));
    };
};

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

var map = new GeoMap();

function geoSetup() {
    // v3: https://medium.com/@ccanipe/building-a-u-s-election-basemap-with-d3-js-and-topojson-fa4b5ab5175d#.jrgby0q48
    //     http://bl.ocks.org/chriscanipe/071984bcf482971a94900a01fdb988fa
    //
    // https://bl.ocks.org/HarryStevens/1c07d73efaf074de05e63a33431eb80a
    // https://bl.ocks.org/iamkevinv/0a24e9126cd2fa6b283c6f2d774b69a2

    map.draw();

    var topo = TOPOJSONDATA;
    var data = [
        topo.objects[objectKey[0]],
        topo.objects[objectKey[1]],
        topo.objects[objectKey[2]]
    ];
    var featurecollections = [
        topojson.feature(topo, data[0]),
        topojson.feature(topo, data[1]),
        topojson.feature(topo, data[2])
    ];
    var meshes = [
        topojson.mesh(topo, data[0], neq),
        topojson.mesh(topo, data[1], neq),
        topojson.mesh(topo, data[2], neq)
    ];

    // calculate the zoom by a bounding box


    // g_features.selectAll('.level0')
    //     .selectAll('path')
    //     .data(featurecollections[0].features)
    //     .enter()
    //     .append('path')
    //     .attr("class", "level0 area")
    //     .attr("d", path);
    //
    // g_features.append('path')
    //     .datum(topojson.mesh(topo, data[0], neq))
    //     .attr("class", "level0-border border")
    //     .attr("d", path)
    //     .style("stroke-width", "1.5px");
    //
    // g_features.append('path')
    //     .data(topojson.feature(topo, data[1]).features)
    //     .attr("class", "level1 area")
    //     .attr("d", path);
    // g_features.append('path')
    //     .datum(topojson.mesh(topo, data[1], neq))
    //     .attr("class", "level1-border border")
    //     .attr("d", path)
    //     .style("stroke-width", "1.5px");

    // pathGs[0].selectAll('path')
    //     .data(geos[0].features)
    //     .enter()
    //     .append('path')
    //     .attr('id', function (d) {
    //         return 's' + d.properties.stadsdeelcode;
    //     })
    //     .attr('class', 'area')
    //     .attr('d', path)
    //     .on("click", stadsdeel_clicked);
    // pathGs[3]
    //     .append('path')
    //     .datum(topojson.mesh(topo, geos[3], neq))
    //     .attr("class", "stadsdeel-border border")
    //     .attr('d', path);
    //
    // pathGs[1].selectAll('path')
    //     .data(geos[1].features)
    //     .enter()
    //     .append('path')
    //     .attr('id', function (d) {
    //         return 'w' + d.properties.wijkcode;
    //     })
    //     .attr('class', 'area')
    //     .attr('d', path)
    //     .on("click", wijk_clicked);
    // pathGs[1]
    //     .append('path')
    //     .datum(topojson.mesh(topo, geos[1], neq))
    //     .attr("class", "wijk-border border");
    //
    // pathGs[2].selectAll('path')
    //     .data(geos[2].features)
    //     .enter()
    //     .append('path')
    //     .attr('id', function (d) {
    //         return 'b' + d.properties.buurtcode;
    //     })
    //     .attr('class', 'area')
    //     .attr('d', path)
    //     .on("click", buurt_clicked);
    // pathGs[2]
    //     .append('path')
    //     .datum(topojson.mesh(topo, geos[2], neq))
    //     .attr("class", "buurt-border border");
    //
    // var i = -1;
    // while (++i <= 2){
    //     geos[i].features.sort(sortByProperty(propertyNaam[i])).forEach(function (d){
    //         lists[i].append('li')
    //             .attr('class', 'list-group-item')
    //             .append('span')
    //             .attr('class', 'tag tag-default tag-pill float-xs-right')
    //             .text(d.properties[propertyNaam[0]]);
    //     });
    // }
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

}

// On click outside an area
function reset() {
    active.classed("active", false);
    active = d3.select(null);


}

listButtons.forEach(function (el, i) {
    el.on('click', function () {
        updateSelected(i);
    });
});
