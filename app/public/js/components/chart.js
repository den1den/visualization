/**
 * Created by Dennis on 29-1-2017.
 */
/*global d3,$*/

var Chart = function (rootId, csx, csy) {
    var $svg = $(rootId);
    var svg = d3.select(rootId);

    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var width = $svg.width() - margin.left - margin.right - 10;
    var height = 490 - 10;

    svg.append("rect")
        .attr("class", "background")
        .attr("width", width)
        .attr("height", height)
        .attr("x", margin.left).attr("y", margin.top);

    var xAxis, yAxis;
    function setXY() {
        xAxis = d3.scaleLinear().range([0, width]);
        yAxis = d3.scaleLinear().range([height, 0]);
    }
    setXY();

    svg
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);
    var root = svg
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    function getX(d) {
        return +csx.getValueFromData(d[1]);
    }

    function getY(d) {
        return +csy.getValueFromData(d[1]);
    }

    function getYear(d) {
        return d[0];
    }

    d3.select("#chart-legend .table-years").selectAll("th")
        .style("background-color", function (d, i) {
            return yearColors[i];
        });

    function setDomain(d) {
        var l = d[1] - d[0];
        var p = 0.05;
        return [d[0] - p * l, d[1] + p * l];
    }

    var chartData = null;

    function parseXY(feature, index) {
        feature = data.withAggregate(feature, index);
        chartData = [];
        if (feature.properties.data) {
            for (var key in feature.properties.data) {
                if (feature.properties.data.hasOwnProperty(key)) {
                    chartData.push([+key, feature.properties.data[key]]);
                }
            }
        } else {
            console.log("Could not find feature.properties for chart");
            root.selectAll("*").remove();
        }
    }

    function redraw() {
        function x(d) {
            return xAxis(getX(d));
        }
        function y(d) {
            return yAxis(getY(d));
        }
        var valueLine = d3.line()
            .x(x).y(y);

        // Scale the range of the data
        xAxis.domain(setDomain(d3.extent(chartData, getX)));
        yAxis.domain(setDomain(d3.extent(chartData, getY)));
        // xAxis.domain([0, d3.max(data, getX)]);
        // yAxis.domain([0, d3.max(data, getY)]);

        root.selectAll("*").remove();

        root.selectAll("path")
            .data([chartData])
            .enter()
            .append("path")
            .attr("d", valueLine)
            .attr("class", "line");

        root.selectAll("dot")
            .data(chartData)
            .enter()
            .append("circle")
            .attr("r", 10)
            .attr("cx", x)
            .attr("cy", y)
            .style("fill", function (d) {
                return yearColors[getYear(d) - 2011];
            })
            .append("svg:title").text(function (d) {
                return "year = "+d[0]+", data[0]="+d[1][0];
            });

        // Add the X Axis
        root.append("g")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.axisBottom(xAxis));

        // Add the Y Axis
        root.append("g")
            .call(d3.axisLeft(yAxis));
    }

    var data = null;
    this.bindData = function (d) {
        data = d;
        parseXY(null, -1);
        redraw();

        SelectionManager.addChangeListener("selection", function (newSelection, previousSelection) {
            parseXY(newSelection.data, newSelection.level);
            redraw();
        });

        SelectionManager.addChangeListener(["data-0", "data-1"], function (newSelection, previousSelection) {
            redraw();
        });
    };

    //TODO; https://bl.ocks.org/mbostock/3885304
};
