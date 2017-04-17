/*global d3*/
function ListSelector() {
    var lists = [
            d3.select("#list-stadsdeel"),
            d3.select("#list-wijk").style("display", "none"),
            d3.select("#list-buurt").style("display", "none")
        ],
        listButtons = [
            d3.select("#list-tab-stadsdeel"),
            d3.select("#list-tab-wijk"),
            d3.select("#list-tab-buurt")
        ];

    listButtons.forEach(function (el, i) {
        el.on("click", function () {
            setList(i);
        });
    });

    function setList(index) {
        var i = -1;
        while (++i <= 2) {
            var isShow = i === index || (i === 0 && index === -1);
            lists[i].style("display", isShow ? "flex" : "none");
            listButtons[i].attr("class", "nav-link" + (isShow ? " active" : ""));
        }
    }

    function getOnClickFn(d, index) {
        return function () {
            data.fireSelectChange("list", d, index);
        }
    }

    this.bindData = function (data) {
        function append(index) {
            var features = data.getFeature(index).features;
            features.sort(sortByProperty(propertyKey[index])).forEach(function (d) {
                var elName = d.properties[propertyKey[index]],
                    elIndex = d.properties[propertyCodeKey[index]];
                lists[index].append("li")
                    .attr("class", "list-group-item")
                    .attr("id", "list-item-" + index + "-" + elIndex)
                    .append("span")
                    .attr("class", "tag tag-default tag-pill float-xs-right")
                    .text(elName)
                    .on("click", getOnClickFn(d, index));
            });
        }

        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);

        data.addChangeListener(function (source, newSelected, newSelectedLevel, oldSelected, oldSelectedLevel) {
            if (oldSelectedLevel !== -1 && oldSelected !== null) {
                var oldElIndex = oldSelected.properties[propertyCodeKey[oldSelectedLevel]],
                    oldEl = lists[oldSelectedLevel].select("#list-item-" + oldSelectedLevel + "-" + oldElIndex);
                oldEl.classed("active", false);
                oldEl.select("button").remove();
            }
            if (newSelected !== null) {
                var elIndex = newSelected.properties[propertyCodeKey[newSelectedLevel]];
                var el = lists[newSelectedLevel].select("#list-item-" + newSelectedLevel + "-" + elIndex);
                el.classed("active", true);
                el.append("button")
                    .on("click", function () {
                        console.log("clicked");
                    })
            }
            setList(newSelectedLevel);
        })
    };
}
