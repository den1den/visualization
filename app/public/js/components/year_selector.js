/*global d3,yearChange*/
var yearColors = ["#feedde", "#fdbe85", "#fd8d3c", "#e6550d", "#a63603"];
yearColors = ["#fdd0a2", "#fdae6b", "#fd8d3c", "#e6550d", "#a63603"];

function YearSelector(rootId, YearSelection) {
    var row = d3.select(rootId)
        .append("div")
        .attr("class", "years-legend")
        .append("ul")
        .attr("class", "pagination");
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
            var setSelected = v;
            if (setSelected) {
                years[i].style("background-color", function () {
                    var _i = i;
                    return function(){return yearColors[_i];};
                }());
                YearSelection.setSelected(i, true);
            } else {
                years[i].style("background-color", function () {
                    return "#FCF4ED";
                });
                YearSelection.setSelected(i, false);
            }
        }
    }

    years.forEach(function (th, i) {
        th.text(YearSelection.getYears()[i])
            .style("background-color", function () {
                return yearColors[i];
            })
            .on("click", function () {
                setSelect(i, i, !YearSelection.isSelected(i));
                d3.event.preventDefault();
                yearChange(rootId, YearSelection);
            })
            .on("mouseover", function () {
            })
            .on("mouseup", function () {
            });
    });

}