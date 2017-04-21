/*global d3,yearChange*/
var yearColors = ["#feedde", "#fdbe85", "#fd8d3c", "#e6550d", "#a63603"];
yearColors = ["#fdd0a2", "#fdae6b", "#fd8d3c", "#e6550d", "#a63603"];

function YearSelector(rootId, YearSelection) {
    var row = d3.select(rootId)
        .append("ul")
        .attr("class", "pagination justify-content-center");
    var years = [
        row.append("li").attr("class", "page-item").append("a").attr("class", "page-link"),
        row.append("li").attr("class", "page-item").append("a").attr("class", "page-link"),
        row.append("li").attr("class", "page-item").append("a").attr("class", "page-link"),
        row.append("li").attr("class", "page-item").append("a").attr("class", "page-link"),
        row.append("li").attr("class", "page-item").append("a").attr("class", "page-link")
    ];

    function setSelect(i1, i2, v) {
        if (i2 < i1) {
            var i3 = i1;
            i1 = i2;
            i2 = i3;
        }
        for (var i = i1; i <= i2; i++) {
            if (v) {
                years[i]
                    .classed("active", true)
                    .style("background-color", function () {
                        var _i = i;
                        return function () {
                            return yearColors[_i];
                        };
                    }());
                YearSelection.setSelected(i, true);
            } else {
                years[i]
                    .classed("active", false)
                    .style("background-color", function () {
                        return "#FCF4ED";
                    });
                YearSelection.setSelected(i, false);
            }
        }
    }

    var cache = YearSelection.store();
    years.forEach(function (th, i) {
        th.text(YearSelection.getYears()[i])
            .style("background-color", function () {
                return yearColors[i];
            })
            .classed("active", true)
            .on("click", function () {
                YearSelection.restore(cache);
                setSelect(i, i, !YearSelection.isSelected(i));
                cache = YearSelection.store();
                yearChange("legend", YearSelection);
                d3.event.preventDefault();
            })
            .on("mouseover", function () {
                // mouse in
                for(var _i = 0; _i < years.length; _i++){
                    YearSelection.setSelected(_i, _i===i);
                }
                yearChange("legend-hover", YearSelection);
                //YearSelection.restore(cache);
            })
            .on("mouseup", function () {
            });
    });
    row.on("mouseout", function () {
        YearSelection.restore(cache);
        yearChange(rootId, YearSelection);
    });

}