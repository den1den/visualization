/*global d3,yearChange*/
var yearColors = ["#feedde", "#fdbe85", "#fd8d3c", "#e6550d", "#a63603"];
yearColors = ["#fdd0a2", "#fdae6b", "#fd8d3c", "#e6550d", "#a63603"];

function YearSelector(rootId, YearSelection) {
    var row = d3.select(rootId)
        .append("ul")
        .attr("class", "pagination justify-content-center");
    var span = row.append("span");
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
        i1 = Math.max(0, i1);
        i2 = Math.min(years.length-1, i2);
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

    var prevYearSelection = YearSelection.store();
    years.forEach(function (th, i) {
        th.text(YearSelection.getYears()[i])
            .style("background-color", function () {
                return yearColors[i];
            })
            .on("click", function () {
                YearSelection.restore(prevYearSelection);
                setSelect(i, i, !YearSelection.isSelected(i));
                prevYearSelection = YearSelection.store();
                // d3.event.preventDefault();
                // yearChange("click", YearSelection);
            })
            .on("mouseover", function () {
                for(var _i = 0; _i < years.length; _i++){
                    YearSelection.setSelected(_i, _i===i);
                }
                // setSelect(0, i-1, false);
                // setSelect(i, i, true);
                // setSelect(i+1, years.length, false);
                yearChange("hover", YearSelection);
            })
            .on("mouseup", function () {
            });
    });
    row
        // .on("mouseenter", function () {
        //     prevYearSelection = YearSelection.store();
        //     console.log("mouseenter(prevYearSelection"+prevYearSelection+")");
        // })
        .on("mouseleave", function () {
            console.log("mouseleave()(prevYearSelection = "+prevYearSelection+")");
            if(prevYearSelection) {
                YearSelection.restore(prevYearSelection);
                yearChange(rootId, YearSelection);
            }
        });
}