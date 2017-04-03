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

    function getOnClickFn(d, index) {
        return function () {
            data.fireSelectChange('list', d, index);
        }
    }

    var fillList = function (features, index) {
        features.sort(sortByProperty(propertyKey[index])).forEach(function (d) {
            lists[index].append('li')
                .attr('class', 'list-group-item')
                .append('span')
                .attr('class', 'tag tag-default tag-pill float-xs-right')
                .text(d.properties[propertyKey[index]])
                .on('click', getOnClickFn(d, index));
        });
    };

    this.bindData = function (data) {
        function append(index) {
            var features = data.getFeature(index).features;
            fillList(features, index);
        }
        append.call(this, 0);
        append.call(this, 1);
        append.call(this, 2);
    };
}
