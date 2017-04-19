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
            resetFilter(i);
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

    function resetFilter(level){
        setFilter(level, -1);
    }
    function setFilter(level, filterLevel, filterValue) {
        var amount = 0;
        setList(level);
        if(level >= 0) {
            lists[level].selectAll("li").each(function (d, i) {
                var el = d3.select(this);
                if (filterLevel === -1) {
                    el.style("display", "flex");
                } else {
                    var isShow = d.d.properties[propertyCodeKey[filterLevel]] === filterValue;
                    el.style("display", isShow ? "flex" : "none");
                    amount += isShow;
                }
            });
            console.log("Filterd down to " + amount + " elements");
        }
        return amount;
    }

    function getOnClickFn(d, index) {
        return function () {
            selectionChange("ListSelector-li", d, index);
        };
    }

    this.bindData = function (data) {
        function append(index) {
            var features = data.getFeatureCollection(index).features;
            features = features
                .sort(sortByProperty(propertyKey[index]))
                .map(function (d) {
                    return {
                        name: d.properties[propertyKey[index]],
                        index: d.properties[propertyCodeKey[index]],
                        d: d
                    };
                });
            lists[index].selectAll("li")
                .data(features)
                .enter()
                .append("li")
                .attr("class", "list-group-item")
                .attr("id", function (d) {
                    return "list-item-" + index + "-" + d.index;
                })
                .append("span")
                .attr("class", "tag tag-default tag-pill float-xs-right")
                .text(function (d) {
                    return d.name;
                })
                .on("click", function(d){
                    getOnClickFn(d.d, index)();
                });
        }

        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);

        SelectionManager.addChangeListener("selection", function (newSelection, previousSelection) {
            var newSelected = newSelection.data, newSelectedLevel = newSelection.level;
            if (previousSelection !== null) {
                var oldSelected = previousSelection.data, oldSelectedLevel = previousSelection.level;
                var oldElIndex = oldSelected.properties[propertyCodeKey[oldSelectedLevel]],
                    oldEl = lists[oldSelectedLevel].select("#list-item-" + oldSelectedLevel + "-" + oldElIndex);
                oldEl.classed("active", false);
                oldEl.select("div").remove();
            }
            function centerNode(el, parent) {
                var current = parent.property("scrollTop");
                var target = el.property("offsetTop");
                target = target < 0 ? 0 : target;
                var interpolate = d3.interpolateNumber(current, target);
                parent.transition()
                    .duration(1000)
                    .tween("centerNode", function () {
                        var parent = this;
                        return function (t) {
                            parent.scrollTop = interpolate(t) - 148;
                        };
                    });
                console.log("interpolating scrollTop of "+parent+" from "+current+" to "+target);
            }

            if (newSelected !== null) {
                var elIndex = newSelected.properties[propertyCodeKey[newSelectedLevel]];
                var el = lists[newSelectedLevel].select("#list-item-" + newSelectedLevel + "-" + elIndex);
                el.classed("active", true);

                var extraButtons = el.append("div")
                    .classed("force-pull-right", true),
                    onClickBack;
                if(newSelectedLevel > 0) {
                    onClickBack = function () {
                        var filterOn = newSelected.properties[propertyCodeKey[newSelectedLevel - 1]];
                        setFilter(newSelectedLevel - 1, newSelectedLevel - 1, filterOn);
                    };
                } else {
                    onClickBack = function () {
                        selectionChange("ListSelector-back-root", null, -1);
                        return false;
                    };
                }
                extraButtons.append("button")
                    .classed("btn btn-default btn-secondary btn-sm", true)
                    .html("<i class=\"fa fa-arrow-left\" aria-hidden=\"true\"></i>")
                    .on("click", onClickBack);
                if(newSelectedLevel < 2) {
                    extraButtons.append("button")
                        .classed("btn btn-default btn-secondary btn-sm", true)
                        .html("<i class=\"fa fa-arrow-right\" aria-hidden=\"true\"></i>")
                        .on("click", function () {
                            setFilter(newSelectedLevel + 1, newSelectedLevel, elIndex);
                        })
                        .classed("disabled", function (d) {
                            return newSelectedLevel === 1 && d.d.properties.wijken_in_buurt <= 1;
                        });
                }
                resetFilter(newSelectedLevel);
                centerNode(el, lists[newSelectedLevel]);
            } else {
                resetFilter(newSelectedLevel);
            }
        });
    };
}
