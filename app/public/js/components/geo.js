/**
 * Created by Dennis on 28-1-2017.
 */
/*global d3*/
function GeoMap() {
    var svg = d3.select("#geo");

    var $svg = $("#geo");
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
            data.fireSelectChange("geo", null, -1);
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

    this.bindData = function (data) {
        //init
        initZoom(data.getMesh(2));
        function append(index) {
            var features = data.getFeature(index).features,
                mesh = data.getMesh(index),
                clickedFn = getOnClickedFn(index);
            areas[index].selectAll("path")
                .data(features).enter()
                .append("path").attr("d", path)
                .on("click", clickedFn);
            // meshes[index].append("path")
            //     .datum(mesh)
            //     .attr("d", path);
        }

        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);

        data.addChangeListener(function (source, newSelected, newSelectedLevel, oldSelected, oldSelectedLevel) {
            if (newSelectedLevel < 2) {
                // meshes[index-1].style("display", "none");
                // meshes[index].style("display", "inherit");

                if (newSelectedLevel >= 0)
                    areas[newSelectedLevel].style("display", "none");
                areas[newSelectedLevel + 1].style("display", "inherit");
            }

            // Zoom te specific area
            if (newSelected == null) {
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

            // Set selected Text
            var selectedText;
            selected.selectAll("path").remove();
            if (newSelectedLevel === -1) {
                selectedText = "Den Haag (city)";
            } else if (newSelected === null) {
                selectedText = "None";
            } else {
                selectedText = newSelected.properties[propertyKey[newSelectedLevel]] + " (" + typeName[newSelectedLevel].toLowerCase() + ")";
            }
            selected.selectAll("path")
                .data([newSelected]).enter()
                .append("path")
                .attr("d", path);
            d3.select("#selected-title").text(selectedText);
        })
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

        areas[0].style("display", "inherit");
        areas[1].style("display", "none");
        areas[2].style("display", "none");

        root.transition()
            .duration(750)
            .call(zoom.transform, d3.zoomIdentity); // updated for d3 v4

    }

    function getOnClickedFn(index) {
        return function (d) {
            data.fireSelectChange("geo", d, index); // also send inex?
        };
    }
}
