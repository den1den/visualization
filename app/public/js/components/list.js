/*global d3,$*/
function ListSelector() {
    var lists = [
            d3.select("#list-stadsdeel"),
            d3.select("#list-wijk").style("display", "none"),
            d3.select("#list-buurt").style("display", "none")
        ],
        listButtons = [
            d3.select("#list-tab-stad"),
            d3.select("#list-tab-stadsdeel").classed("active", true),
            d3.select("#list-tab-wijk"),
            d3.select("#list-tab-buurt")
        ];

    listButtons.forEach(function (el, i) {
        if (i === 0) {
            el.on("click", function () {
                selectionChange("ListSelector-buttom", null, -1);
            });
        } else {
            el.on("click", function () {
                resetFilter(i - 1);
            });
        }
    });

    function setList(index) {
        for(var i = 0; i < 4; i++){
            if(i < 3) {
                lists[i].style("display", i === index || i === 0 && index === -1 ? "block" : "none");
            }
            listButtons[i].classed("active", index === -1 && (i === 1) || index !== -1 && (i === index + 1));
        }
    }

    var _filtered = false;
    function resetFilter(level){
        setFilter(level, -1);
        _filtered = false;
    }
    function setFilter(level, filterLevel, filterValue) {
        _filtered = true;
        var amount = 0;
        setList(level);
        if(level >= 0) {
            lists[level].selectAll("li").each(function (d, i) {
                var el = d3.select(this);
                if (filterLevel === -1) {
                    el.style("display", "block");
                } else {
                    var isShow = d.d.properties[propertyCodeKey[filterLevel]] === filterValue;
                    el.style("display", isShow ? "block" : "none");
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
                .append("div")
                .attr("class", "li-title")
                .text(function (d) {
                    return d.name;
                })
                .on("click", function(d){
                    getOnClickFn(d.d, index)();
                })
            ;
        }

        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);

        SelectionManager.addChangeListener("selection", function (newChangeObject, previousChangeObject) {
            var newData = newChangeObject.value.data,
                newLevel = newChangeObject.value.level;
            if (previousChangeObject !== null) {
                var oldData = previousChangeObject.value.data,
                    oldSelectedLevel = previousChangeObject.value.level;
                if(oldData !== null) {
                    // If anything was selected
                    var oldElIndex = oldData.properties[propertyCodeKey[oldSelectedLevel]],
                        oldEl = lists[oldSelectedLevel].select("#list-item-" + oldSelectedLevel + "-" + oldElIndex);
                    oldEl.classed("active", false);
                    oldEl.select(".li-buttons").remove();
                }
            }

            if (newData === null) {
                setList(newLevel);
            } else {
                var elIndex = newData.properties[propertyCodeKey[newLevel]];
                var el = lists[newLevel].select("#list-item-" + newLevel + "-" + elIndex);
                el.classed("active", true);

                var extraButtons = el.append("div")
                        .attr("class", "li-buttons text-right")
                        .append("div"),
                    onClickBack;
                if (newLevel > 0) {
                    onClickBack = function () {
                        // Find parent and fire changeSelection with data
                        var filterOn = newData.properties[propertyCodeKey[newLevel - 1]];
                        lists[newLevel - 1].selectAll("li").filter(function (d, i) {
                            return filterOn === d.d.properties[propertyCodeKey[newLevel - 1]];
                        }).each(function (parent) {
                            selectionChange("ListSelector-back-root", parent.d, newLevel - 1);
                        });
                        d3.event.preventDefault();
                    };
                } else {
                    onClickBack = function () {
                        selectionChange("ListSelector-back-root", null, -1);
                        d3.event.preventDefault();
                    };
                }
                extraButtons.append("button")
                    .classed("btn btn-default btn-secondary btn-sm", true)
                    .html("<i class=\"fa fa-arrow-left\" aria-hidden=\"true\"></i>")
                    .on("click", onClickBack);
                if (newLevel < 2) {
                    extraButtons.append("button")
                        .classed("btn btn-default btn-secondary btn-sm", true)
                        .html("<i class=\"fa fa-arrow-right\" aria-hidden=\"true\"></i>")
                        .on("click", function () {
                            setFilter(newLevel + 1, newLevel, elIndex);
                        })
                        .attr("disabled", function (d) {
                            return newLevel === 1 && d.d.properties.wijken_in_buurt <= 1 ? "disabled" : null;
                        });
                }
                if(_filtered && previousChangeObject && previousChangeObject.value && (previousChangeObject.value.level === newLevel - 1 || previousChangeObject.value.level === newLevel)){
                    // One deeper after filter
                    // or same level after filer
                } else {
                    resetFilter(newLevel);
                }

                var parent = lists[newLevel].nodes()[0];
                var child = el.nodes()[0];
                scrollIntoView(child, parent);
            }
        });
    };

    function scrollIntoView(child, parent, i) {
        if(typeof i === "undefined"){
            i = 0;
        }
        if(i > 10){
            return false;
        }
        var childLow = child.offsetTop - parent.offsetTop,
            childHigh = childLow + $(child).height(),
            parentLow = parent.scrollTop,
            parentHigh = parent.scrollTop + $(parent).height();
        if(childLow < parentLow){
            parent.scrollTop = childLow;
            return scrollIntoView(child, parent, i+1);
        } else if (childHigh > parentHigh){
            parent.scrollTop = childLow + $(parent).height() - $(child).height();
            return scrollIntoView(child, parent, i+1);
        }
        return true;
    }
}
