/**
 * Created by Dennis on 28-1-2017.
 */

function GeoMap(list) {
    var width = 500,
        height = 500;

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
        .on('click', resetZoom);
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

    svg.call(zoom);

    var selected = root.append("g").attr("id", "selected-area");

    this.setData = function (data) {
        //init
        initZoom(data.getMesh(2));

        function append(index) {
            var features = data.getFeature(index).features,
                mesh = data.getMesh(index),
                clickedFn = getOnClickedFn(index);

            areas[index].selectAll('path')
                .data(features).enter()
                .append('path').attr("d", path)
                .on('click', clickedFn);
            // meshes[index].append("path")
            //     .datum(mesh)
            //     .attr("d", path);

            list.fillList(features, index);
        }

        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);
    };
    function initZoom(hasBounds) {
        projection.scale(1).translate([0, 0]);
        var s, t,
            b = path.bounds(hasBounds),
            dx = b[1][0] - b[0][0],
            dy = b[1][1] - b[0][1];
        s = 0.9 / Math.max(dx / width, dy / height);
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

        list.setList(0);

        setSelect(null);
    }

    function getOnClickedFn(index) {
        return function (d) {
            setSelect(d);

            if (index < 2) {
                // meshes[index-1].style("display", "none");
                // meshes[index].style("display", "inherit");

                areas[index].style("display", "none");
                areas[index + 1].style("display", "inherit");

                list.setList(index + 1);
            }

            // Zoom te specific area
            var bounds = path.bounds(d),
                dx = bounds[1][0] - bounds[0][0],
                dy = bounds[1][1] - bounds[0][1],
                x = (bounds[0][0] + bounds[1][0]) / 2,
                y = (bounds[0][1] + bounds[1][1]) / 2,
                scale = Math.max(1, Math.min(8, 0.9 / Math.max(dx / width, dy / height))),
                translate = [width / 2 - scale * x, height / 2 - scale * y];
            root.transition()
                .duration(750)
                .call(zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale));

            chart.setChartData(d);
        };
    }

    function setSelect(el) {
        console.log("Select:");
        console.log(el);

        selected.selectAll("path").remove();
        if (el !== null) {
            selected.selectAll("path")
                .data([el]).enter()
                .append('path')
                .attr("d", path);
            // d3.select('#select-name').text(d.properties[propertyNaam[index]]);
            // d3.select('#select-data').text(JSON.stringify(d.properties));
        }
    }
}

function ListSelector() {
    var lists = [
            d3.select('#list-stadsdeel'),
            d3.select('#list-wijk').style("display", "none"),
            d3.select('#list-buurt').style("display", "none")
        ],
        listButtons = [
            d3.select("#list-tab-stadsdeel"),
            d3.select("#list-tab-wijk"),
            d3.select("#list-tab-buurt")
        ];

    listButtons.forEach(function (el, i) {
        el.on('click', function () {
            setList(i);
        });
    });

    function setList(index) {
        var i = -1;
        while (++i <= 2) {
            lists[i].style("display", i === index ? "flex" : "none");
            listButtons[i].attr("class", "nav-link" + (i === index ? " active" : ""));
        }
    }
    this.setList = setList;

    function getOnClickFn(d) {
        return function () {
            map.select(d);
        }
    }

    this.fillList = function (features, index) {
        features.sort(sortByProperty(propertyNaam[index])).forEach(function (d) {
            lists[index].append('li')
                .attr('class', 'list-group-item')
                .append('span')
                .attr('class', 'tag tag-default tag-pill float-xs-right')
                .text(d.properties[propertyNaam[index]])
                .on('click', getOnClickFn(d));
        });
    }
}
