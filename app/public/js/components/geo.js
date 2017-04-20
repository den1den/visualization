/**
 * Created by Dennis on 28-1-2017.
 */
/*global d3,$*/
function GeoMap(rootId, dataType) {
    var svg = d3.select(rootId);

    var $svg = $(rootId);
    var width = $svg.width(),
        height = $svg.height();

    var projection = d3.geoMercator();
    projection.scale(1000).translate([width / 2, height / 2]);
    var path = d3.geoPath().projection(projection);

    // Setup root element
    svg.attr("width", width)
        .attr("height", height)
        .on("click", stopped);
    // Add background
    svg.append("rect")
        .attr("class", "background")
        .attr("width", width)
        .attr("height", height)
        .on("click", function () {
            selectionChange("GeoMap-background", null, -1);
        });
    var root = svg.append("g");

    var areas = [
        root.append("g").attr("class", "area").attr("id", "level0-area"),
        root.append("g").attr("class", "area").attr("id", "level1-area").style("display", "none"),
        root.append("g").attr("class", "area").attr("id", "level2-area").style("display", "none")
    ];
    // meshes = [
    //     root.append("g").attr("class", "mesh").attr("id", "level0-border").style("display", "none"),
    //     root.append("g").attr("class", "mesh").attr("id", "level1-border").style("display", "none"),
    //     root.append("g").attr("class", "mesh").attr("id", "level2-border")
    // ];

    // Setup zooming behaviour
    var zoom = d3.zoom()
        .scaleExtent([1, 8])
        .on("zoom", onZoom);

    // ZOOM OPTION:
    // FIXME: this does not work :(
    // svg.call(zoom);

    var selected = root.append("g").attr("id", "selected-area");

    function ColorFunction(min, max) {
        this.min = min;
        this.max = max;
    }
    ColorFunction.prototype.getColorFunction = function () {
        var colorFunction = this;
        return function (d) {
            var val = getValue(d);
            var val01 = (val - colorFunction.min) / (colorFunction.max - colorFunction.min);
            return d3.hsl(0, val01, 0.5);
        };
    };
    ColorFunction.prototype.addjustBounds = function (d) {
        var val = getValue(d);
        if (isNaN(this.min) || val < this.min) {
            this.min = val;
        }
        if (isNaN(this.max) || val > this.max) {
            this.max = val;
        }
    };
    ColorFunction.prototype.toString = function () {
        return "ColorFunction(min="+this.min+", max="+this.max+")";
    };
    function getValue(dataElement){
        return dataType.getDataValue(dataElement.properties.data[2011]);
    }

    // single colorFunction
    var colorFunction = new ColorFunction();

    this.bindData = function (data) {
        //init
        initZoom(data.getMesh(2));
        function append(index) {
            var featureCollection,
                mesh = data.getMesh(index),
                clickedFn = getOnClickedFn(index);
            featureCollection = data.getFeatureCollection(index);
            featureCollection.features.map(function (f) {
                return data.withAggregate(f, index);
            });

            // multiple colorFunctions:
            // var colorFunction = new ColorFunction();
            featureCollection.features.forEach(function(f){
                colorFunction.addjustBounds(f);
            });

            areas[index].selectAll("path").remove();
            areas[index].selectAll("path")
                .data(featureCollection.features)
                .enter()
                .append("path")
                .attr("d", path)
                .style("fill", colorFunction.getColorFunction())
                .on("click", clickedFn);
            // meshes[index].append("path")
            //     .datum(mesh)
            //     .attr("d", path);
        }

        append(0);
        append(1);
        append(2);

        SelectionManager.addChangeListener("selection", function (newSelection, previousSelection) {
            var newSelected = newSelection.change.data;
            var newSelectedLevel = newSelection.level;

            // if (newSelectedLevel + 1 < areas.length ) {
            //     areas[newSelectedLevel + 1].style("display", "inherit");
            // }
            for (var i = 0; i < areas.length; i++) {
                if (i === newSelectedLevel + 1 || (newSelectedLevel === 2 && i === areas.length - 1)) {
                    areas[i].style("display", "inherit");
                } else {
                    areas[i].style("display", "none");
                }
            }
            //areas[i].style("display", "none");
            // meshes[index-1].style("display", "none");
            // meshes[index].style("display", "inherit");

            // Zoom te specific area
            if (newSelected === null) {
                resetZoom();
            } else {
                var bounds = path.bounds(newSelected),
                    dx = bounds[1][0] - bounds[0][0],
                    dy = bounds[1][1] - bounds[0][1],
                    x = (bounds[0][0] + bounds[1][0]) / 2,
                    y = (bounds[0][1] + bounds[1][1]) / 2,
                    scale = Math.max(1, Math.min(8, 0.9 / Math.max(dx / width, dy / height))),
                    translate = [width / 2 - scale * x, height / 2 - scale * y];
                root.transition()
                    .duration(750)
                    .call(zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale));
            }

            // Add outline to selected path
            selected.selectAll("path").remove();
            selected.selectAll("path")
                .data([newSelected])
                .enter()
                .append("path")
                .attr("d", path);
        });

        SelectionManager.addChangeListener(["data-1"], function (newSelection, previousSelection) {
            colorFunction = new ColorFunction();
            append(0);
            append(1);
            append(2);
        });
    };

    function initZoom(hasBounds) {
        projection.scale(1).translate([0, 0]);
        var s, t,
            b = path.bounds(hasBounds),
            dx = b[1][0] - b[0][0],
            dy = b[1][1] - b[0][1];
        s = 0.83 / Math.max(dx / width, dy / height);
        t = [(width - s * (b[1][0] + b[0][0])) / 2, (height - s * (b[1][1] + b[0][1])) / 2];
        projection.scale(s).translate(t);
    }

    function onZoom() {
        //root.style("stroke-width", 1.5 / d3.event.transform.k + "px");
        root.attr("transform", d3.event.transform);
    }

    function resetZoom() {
        // meshes[0].style("display", "inherit");
        // meshes[1].style("display", "none");
        // meshes[2].style("display", "none");

        // areas[0].style("display", "inherit");
        // areas[1].style("display", "none");
        // areas[2].style("display", "none");

        root.transition()
            .duration(750)
            .call(zoom.transform, d3.zoomIdentity); // updated for d3 v4

    }

    function getOnClickedFn(index) {
        return function (d) {
            selectionChange("GeoMap-path", d, index); // also send inex?
        };
    }
}
