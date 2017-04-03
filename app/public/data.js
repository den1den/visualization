/**
 * Created by Dennis on 27-1-2017.
 */

var typeName = ['City part', 'District', 'Neighborhood'];
var objectKey = ['stadsdeel', 'wijken', 'buurten'];
var propertyKey = ['stadsdeelnaam', 'wijknaam', 'buurtnaam'];
var propertyCodeKey = ['stadsdeelcode', 'wijkcode', 'buurtcode'];
var householdfilter = [['Total', 'total'], ['With electricity', 'electricity'], ['With gas', 'gas'], ['With solar', 'solar'], ['With other source', 'other']];

var yearColors = ['#feedde', '#fdbe85', '#fd8d3c', '#e6550d', '#a63603'];
var yearColors = ['#fdd0a2', '#fdae6b', '#fd8d3c', '#e6550d', '#a63603'];

function TopoJsonData() {
    var _data = null;
    this.get = function (callback) {
        d3.json('combined.topojson', function (error, resp) {
            if (error) throw error;
            console.log("Loaded combined.topojson");
            _data = resp;
            callback();
        });
    };
    this.getMesh = function (index) {
        return topojson.mesh(_data, _data.objects[objectKey[index]], neq);
    };
    this.getFeature = function (index) {
        return topojson.feature(_data, _data.objects[objectKey[index]]);
    };
    this.withAggregate = function (feature, index) {
        if (index == 2) {
            return feature;
        }
        var new_data = [],
            datagroup = _data.objects[objectKey[2]];
        if (index === -1) {
            //all
            datagroup.geometries.forEach(function (g) {
                new_data.push(g.properties.data);
            });
            return {
                'properties': {
                    'data': join(new_data)
                }
            };
        } else {
            var key = feature.properties[propertyCodeKey[index]];
            datagroup.geometries.forEach(function (g) {
                var k = g.properties[propertyCodeKey[index]];
                if (k == key) {
                    new_data.push(g.properties.data);
                }
            });
            feature.properties.data = join(new_data);
            return feature;
        }
    };
    function join(property_datas) {
        var new_data = [], i, year, newdict, newval;
        for (i = 0; i < 28; i++) {
            newdict = {};
            for (year = 2011; year <= 2015; year++) {
                function getSum(i) {
                    var sum = 0, x;
                    for (x = 0; x < property_datas.length; x++) {
                        sum += property_datas[x][year][i];
                    }
                    return sum;
                }

                if ((i >= 12 && i <= 14) || (i >= 21 && i <= 23)) {
                    // avg
                    newval = getSum(i - 3) / getSum(i - 6);
                } else {
                    // summation
                    newval = getSum(i);
                }
                if (newval !== 0 && !newval) {
                    throw new Error("Stange value calculated: " + newval);
                }
                if (!new_data[year]) {
                    new_data[year] = [];
                }
                new_data[year].push(newval);
            }
        }
        return new_data;
    }

    var changeListeners = [];
    this.addChangeListener = function(onChange){
        changeListeners.append(onChange);
    };

    var selected = null;
    this.fireSelectChange = function(source, newSelected){
        console.log("fireSelectChange("+source+", "+newSelected+")");
        changeListeners.forEach(function (onChange) {
            onChange(source, selected, newSelected);
        });
        selected = newSelected;
    }
}

function loadDatas(callback) {
    d3.queue()
        .defer(d3.json, '/stadsdeel.topojson')
        .defer(d3.json, '/wijken.topojson')
        .defer(d3.json, '/buurten-simple.topojson')
        .awaitAll(function (error, datas) {
            if (error) throw error;
            console.log("Data loaded");
            callback(datas);
        });
}
function loadCsv(callback) {
    d3.request('/buurten.topojson')
        .mimeType("text/csv")
        .response(function (xhr) {
            return d3.dsvFormat(";").parse(xhr.responseText, function (d) {
                    d2 = {};
                    for (var key in d) {
                        if (d.hasOwnProperty(key)) {
                            if (key == 'Jaar') {
                                d2[key] = new Date(+d[key], 0, 1);
                            } else if (key == 'Buurt') {
                                d2[key] = d[key]
                            } else {
                                d2[key] = +d[key]
                            }
                        }
                    }
                    return d2;
                }
            );
        })
        .get(function (error, data) {
            if (error) throw error;
            console.log("Data loaded");
            callback(data);
        });
}

function Axis(controlSelect, titleSelect, defaults) {
    var AXIS = this,
        axisTitle = d3.select(titleSelect),
        root = d3.select(controlSelect),
        collum_names_nl = [
            'Totaal aantal Vastgoedobjecten', 'Totaal aantal Vastgoedobjecten Elektra', 'Totaal aantal Vastgoedobjecten Gas',
            'Totaal CO2 uitstoot (kg)', 'Totaal verbruik Elektra (kWh)', 'Totaal verbruik Gas (m3)',
            'Aantal Vastgoedobjecten Particulier', 'Aantal Vastgoedobjecten Elektra Particulier', 'Aantal Vastgoedobjecten Gas Particulier',
            'CO2 uitstoot Particulier (kg)', 'Verbruik Elektra Particulier (kWh)', 'Verbruik Gas Particulier (m3)',
            'Gemiddelde CO2 uitstoot Particulier (kg)', 'Gemiddelde verbruik Elektra Particulier (kWh)', 'Gemiddelde verbruik Gas Particulier (m3)', 'Aantal Vastgoedobjecten Zakelijk', 'Aantal Vastgoedobjecten Elektra Zakelijk', 'Aantal Vastgoedobjecten Gas Zakelijk', 'CO2 uitstoot Zakelijk (kg)', 'Verbruik Elektra Zakelijk (kWh)', 'Verbruik Gas Zakelijk (m3)', 'Gemiddelde CO2 uitstoot Zakelijk (kg)', 'Gemiddelde verbruik Elektra Zakelijk (kWh)', 'Gemiddelde verbruik Gas Zakelijk (m3)', 'Aantal Vastgoedobjecten Opwek Zonne-energie KV', 'Opwek Zonne-energie KV (kWh)', 'Aantal Vastgoedobjecten Opwek Overig', 'Opwek Overig (kWh)'
        ],
        collum_names = [
            'Real estates', //0
            'Real estates with electricity',
            'Real estates with gas',
            'Co2 emissions (kg)',
            'Power consumption (kWh)',
            'Gas consumption (m3)',

            'Average Co2 emissions (kg)', // 6 CUSTOM ADDED
            'Average power consumption (kWh)',
            'Average gas consumption (m3)',

            'Private real estates', //9
            'Private real estates with electricity',
            'Private real estates with gas',
            'Private Co2 emissions (kg)',
            'Private power consumption (kWh)',
            'Private gas consumption (m3)',

            'Average private Co2 emissions (kg)', //15
            'Average private power consumption (kWh)',
            'Average private gas consumption (m3)',

            'Commercial real estates', //18
            'Commercial real estates with electricity',
            'Commercial real estates with gas',
            'Commercial Co2 emissions (kg)',
            'Commercial power consumption (kWh)',
            'Commercial gas consumption (m3)',

            'Average commercial Co2 emissions (kg)',//24
            'Average commercial power consumption (kWh)',
            'Average commercial gas consumption (m3)',

            'Real estates with solar energy', //27 (private+commercial)
            'Solar energy production (kWh)',

            'Real estates with other power production', //29 (private+commercial)
            'Other production (kWh)'
        ],
        _owner,
        _source,
        _aggr,
        _index = -1;

    function setOwner(owner) {
        if (owner === 'all' || owner === 'total') {
            _owner = 0;
        } else if (owner === 'private') {
            _owner = 9;
        } else if (owner === 'commercial') {
            _owner = 18;
        } else {
            throw new Error("setOner(" + owner + ") not possible");
        }
        _index = -1;
    }

    function setSource(source) {
        if (source === 'co2' || source === 'total') {
            _source = 0;
        } else if (source === 'electricity') {
            _source = 1;
        } else if (source === 'gas') {
            _source = 2;
        } else if (source === 'solar') {
            _source = 27;
        } else if (source === 'other') {
            _source = 29;
        } else {
            throw new Error("setSource(" + source + ") not possible");
        }
        _index = -1;
    }

    function setAgg(aggr) {
        if (aggr === 'count' || aggr === 'total') {
            _aggr = 0;
        } else if (aggr === 'value') {
            _aggr = 3;
        } else if (aggr === 'avg') {
            _aggr = 6;
        } else {
            throw new Error("setAgg(" + aggr + ") not possible");
        }
        _index = -1;
    }

    function constructIndex() {
        if (_source === 27 || _source === 29) {
            if (_owner === 0) {
                if (_aggr === 0) {
                    return _source;
                } else if (_aggr === 3) {
                    return _source + 1;
                }
            }
        } else if (_source !== -1) {
            if (_aggr !== -1) {
                if (_owner !== -1) {
                    return _owner + _aggr + _source;
                }
            }
        }
        throw new Error("No valid config _source=" + _source + ", _owner=" + _owner + ", _aggr=" + _aggr);
    }

    function getIndex() {
        if (_index === -1) {
            _index = constructIndex();
        }
        return _index;
    }

    this.onChange = null;

    function reset() {
        if (defaults) {
            setOwner(defaults['owner']);
            setSource(defaults['source']);
            setAgg(defaults['agg']);
        } else {
            _owner = -1;
            _source = -1;
            _aggr = -1;
            _index = -1;
        }
        changed();
    }

    reset();

    function changed() {
        updateAxisTitle();
        if (AXIS.onChange) {
            AXIS.onChange();
        }
    }

    this.update = changed;

    function updateAxisTitle() {
        var index = getIndex();
        if (index === -1) {
            axisTitle.html("None set").style("color", "#999");
        } else {
            axisTitle.html(collum_names[index]).style("color", "#000");
        }
    }

    function dataIndex(index) {
        if (index >= 6) {
            index -= 3;
        }
        return index;
    }

    this.getValue = function (data) {
        var index = getIndex(),
            val;
        if (_aggr === 6) {
            val = data[dataIndex(index - 3)] / data[dataIndex(index - 6)]; //avg
        } else {
            val = data[dataIndex(index)];
        }
        if (!val) {
            throw new Error("Strange value on " + collum_names[index]);
        }
        return val;
    };

    // var year = false;
    // function setYear(year){
    //     year = !!year;
    // }
    // this.setYear = setYear;

    // HTML
    function getDropdownID(name) {
        return controlSelect.substring(1) + '-dropdown-' + name.split(' ')[0].toLowerCase();
    }

    function TogggleMenu(options, onChange) {
        var buttons = root.append('div')
            .attr('data-toggle', 'buttons');
        options.forEach(function (o) {
            buttons.append('label')
                .attr('class', 'btn btn-primary')
                .on('click', function () {
                    setValue(o[1]);
                })
                .html(o[0])
                .append('input')
                .attr('type', 'radio')

        });

        var value = null;
        this.getLastValue = function () {
            return value;
        };
        function setValue(newVal) {
            value = newVal;
            onChange(newVal);
        }

        this.setValue = setValue;
    }

    function DropdownMenu(name, values, onChange, visible) {
        var _visible = visible;
        var dropdown = root.append('div')
            .attr('class', 'dropdown')
            .attr('id', getDropdownID(name));
        dropdown.append('button')
            .attr('class', 'btn btn-secondary dropdown-toggle')
            .attr('type', 'button')
            .attr('data-toggle', 'dropdown')
            .html(name);
        var dropdownOptions = dropdown.append('div')
            .attr('class', 'dropdown-menu');
        values.forEach(function (v) {
            dropdownOptions.append('button')
                .attr('class', 'dropdown-item')
                .html(v[0])
                .on('click', function () {
                    setValue(v[1]);
                });
        });
        this.show = function () {
            var r = !_visible;
            dropdown.style('display', 'inherit');
            _visible = true;
            return r;
        };
        this.hide = function () {
            var r = _visible;
            dropdown.style('display', 'none');
            _visible = false;
            return r;
        };
        if (!_visible) {
            this.hide();
        }
        var value = null;
        this.getLastValue = function () {
            return value;
        };
        function setValue(newVal) {
            value = newVal;
            onChange(newVal);
        }

        this.setValue = setValue;
    }

    this.reset = reset;
    this.DropdownMenu = DropdownMenu;
    this.TogggleMenu = TogggleMenu;
    this.setAgg = setAgg;
    this.setSource = setSource;
    this.setOwner = setOwner;
    this.changed = changed;
}
// function YearAxis(controlSelect, titleSelect, defaults) {
//     Axis.call(this, controlSelect, titleSelect, defaults);
//     var AXIS = this;
//
//     var menu = new AXIS.TogggleMenu(
//         [['Households', 0], ['Years', 1]],
//         function (v) {
//             console.log("Xaxis=" + v);
//             if (v === 0) {
//                 AXIS.setAgg('count');
//                 AXIS.setSource('total');
//                 AXIS.setOwner('all');
//                 AXIS.updateAxisTitle();
//                 hfilter.show();
//             } else {
//                 hfilter.hide();
//
//             }
//         }),
//         hfilter = new AXIS.DropdownMenu(
//             'Filter',
//             householdfilter,
//             function (v) {
//                 AXIS.setSource(v);
//                 AXIS.updateAxisTitle();
//             });
// }
function DataAxis(controlSelect, titleSelect, defaults) {
    Axis.call(this, controlSelect, titleSelect, defaults);
    var AXIS = this;

    var avg = false,
        dr0 = new AXIS.DropdownMenu('Data',
            [['Real estate', 0], ['Measurements', 1]],
            function (v) {
                console.log('Type=' + v);
                var changed;
                if (v === 0) {
                    changed = dr1.show();
                    dr2.hide();
                } else {
                    changed = dr2.show();
                    dr1.hide();
                }
                if (changed) {
                    AXIS.reset();
                }
            }, true),
        dr1 = new AXIS.DropdownMenu('Filter',
            householdfilter,
            function (v) {
                console.log('Filter1=' + v);
                //agg == 0
                AXIS.setAgg('count');
                AXIS.setSource(v);
                AXIS.setOwner('all');
                AXIS.changed();
            }, false),
        dr2 = new AXIS.DropdownMenu('Energy type',
            [['CO<sub>2</sub>', 'co2'], ['Electricity', 'electricity'], ['Gas', 'gas'], ['Solar', 'solar'], ['Other', 'other']],
            function (v) {
                console.log('Energy type=' + v);
                dr3.setValue('all');
                dr3.show();
            }, false),
        dr3 = new AXIS.DropdownMenu('Filter',
            [['All', 'all'], ['Private', 'private'], ['Commercial', 'commercial']],
            function (v) {
                console.log('Filter type=' + v);

                AXIS.setAgg('value');
                AXIS.setSource(dr2.getLastValue());
                AXIS.setOwner(v);
                AXIS.changed();
            }, false);

}

// cs = new CollumSelector();
// cs.setSource('co2');
// cs.setAgg('avg');
// cs.setOwner('commercial');
// console.log(cs.getName());
// cs.setSource('other');
// cs.setAgg('total');
// cs.setOwner('total');
// console.log(cs.getName());
