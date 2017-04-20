/**
 * Created by Dennis on 27-1-2017.
 */
/*global d3,topojson,neq,dataTypeChange,math*/
var typeName = ["City part", "District", "Neighborhood"];
var objectKey = ["stadsdeel", "wijken", "buurten"];
var propertyKey = ["stadsdeelnaam", "wijknaam", "buurtnaam"];
var propertyCodeKey = ["stadsdeelcode", "wijkcode", "buurtcode"];
var householdfilter = [["Total", "total"], ["With electricity", "electricity"], ["With gas", "gas"], ["With solar", "solar"], ["With other source", "other"]];

var yearColors = ["#feedde", "#fdbe85", "#fd8d3c", "#e6550d", "#a63603"];
yearColors = ["#fdd0a2", "#fdae6b", "#fd8d3c", "#e6550d", "#a63603"];

function TopoJsonData() {
    var _data = null;
    this.get = function (callback) {
        d3.json('combined.topojson', function (error, resp) {
            if (error) {
                throw error;
            }
            console.log("Loaded combined.topojson");
            _data = resp;
            callback();
        });
    };
    this.getMesh = function (index) {
        return topojson.mesh(_data, _data.objects[objectKey[index]], neq);
    };
    this.getFeatureCollection = function (index) {
        return topojson.feature(_data, _data.objects[objectKey[index]]);
    };
    this.withAggregate = function (feature, index) {
        if (index === 2) {
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
                if (k === key) {
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
}

function loadDatas(callback) {
    d3.queue()
        .defer(d3.json, '/stadsdeel.topojson')
        .defer(d3.json, '/wijken.topojson')
        .defer(d3.json, '/buurten-simple.topojson')
        .awaitAll(function (error, datas) {
            if (error) { throw error; }
            console.log("Data loaded");
            callback(datas);
        });
}

var collum_names_nl = [
        "Totaal aantal Vastgoedobjecten", "Totaal aantal Vastgoedobjecten Elektra", "Totaal aantal Vastgoedobjecten Gas",
        "Totaal CO2 uitstoot (kg)", "Totaal verbruik Elektra (kWh)", "Totaal verbruik Gas (m3)",
        "Aantal Vastgoedobjecten Particulier", "Aantal Vastgoedobjecten Elektra Particulier", "Aantal Vastgoedobjecten Gas Particulier",
        "CO2 uitstoot Particulier (kg)", "Verbruik Elektra Particulier (kWh)", "Verbruik Gas Particulier (m3)",
        "Gemiddelde CO2 uitstoot Particulier (kg)", "Gemiddelde verbruik Elektra Particulier (kWh)", "Gemiddelde verbruik Gas Particulier (m3)", "Aantal Vastgoedobjecten Zakelijk", "Aantal Vastgoedobjecten Elektra Zakelijk", "Aantal Vastgoedobjecten Gas Zakelijk", "CO2 uitstoot Zakelijk (kg)", "Verbruik Elektra Zakelijk (kWh)", "Verbruik Gas Zakelijk (m3)", "Gemiddelde CO2 uitstoot Zakelijk (kg)", "Gemiddelde verbruik Elektra Zakelijk (kWh)", "Gemiddelde verbruik Gas Zakelijk (m3)", "Aantal Vastgoedobjecten Opwek Zonne-energie KV", "Opwek Zonne-energie KV (kWh)", "Aantal Vastgoedobjecten Opwek Overig", "Opwek Overig (kWh)"
    ],
    collum_names = [
        "Real estates", // 0 filter=combined, aggr=total, source=
        "Real estates with electricity",
        "Real estates with gas",
        "Co2 emissions (kg)", // 3
        "Power consumption (kWh)",
        "Gas consumption (m3)",

        "Average Co2 emissions (kg)", // 6 (CUSTOM ADDED)
        "Average power consumption (kWh)",
        "Average gas consumption (m3)", // (END CUSTOM ADDED)

        "Private real estates", // 9 filter=private
        "Private real estates with electricity",
        "Private real estates with gas",
        "Private Co2 emissions (kg)",
        "Private power consumption (kWh)",
        "Private gas consumption (m3)",

        "Average private Co2 emissions (kg)", // 15
        "Average private power consumption (kWh)",
        "Average private gas consumption (m3)",

        "Commercial real estates", // 18 filter=commercial
        "Commercial real estates with electricity",
        "Commercial real estates with gas",
        "Commercial Co2 emissions (kg)",
        "Commercial power consumption (kWh)",
        "Commercial gas consumption (m3)",

        "Average commercial Co2 emissions (kg)", // 24
        "Average commercial power consumption (kWh)",
        "Average commercial gas consumption (m3)",

        "Real estates with solar energy", // 27 filter=combined, solar energy
        "Solar energy production (kWh)",

        "Real estates with other power production", // 29 other
        "Other production (kWh)"
    ],
    collum_tags = [
        "real_estates",
        "real_estates_electricity",
        "real_estates_gas",
        "co2",
        "power",
        "gas",
        "co2_avg",
        "power_avg",
        "gas_avg",
        "real_estates_private",
        "real_estates_private_electricity",
        "real_estates_private_gas",
        "co2_private",
        "power_private",
        "gas_private",
        "co2_private_avg",
        "power_private_avg",
        "gas_private_avg",
        "real_estates_commercial",
        "real_estates_commercial_electricity",
        "real_estates_commercial_gas",
        "co2_commercial",
        "power_commercial",
        "gas_commercial",
        "co2_commercial_avg",
        "power_commercial_avg",
        "gas_commercial_avg",
        "real_estates_solar",
        "solar",
        "real_estates_other",
        "other"
    ];

function DataType(xy, defaults){
    var _filter_type,
        _source,
        _aggr,
        _index;

    this.setOwner = function (filter_type) {
        if (filter_type === "all" || filter_type === "total") {
            _filter_type = 0;
        } else if (filter_type === "private") {
            _filter_type = 9;
        } else if (filter_type === "commercial") {
            _filter_type = 18;
        } else {
            throw new Error("setOner(" + filter_type + ") not possible");
        }
        _index = -1;
    };

    this.setSource = setSource;
    function setSource(source) {
        if (source === "co2" || source === "total") {
            _source = 0;
        } else if (source === "electricity") {
            _source = 1;
        } else if (source === "gas") {
            _source = 2;
        } else if (source === "solar") {
            _source = 27;
        } else if (source === "other") {
            _source = 29;
        } else {
            throw new Error("setSource(" + source + ") not possible");
        }
        _index = -1;
    }

    this.setAgg = setAgg;
    function setAgg(aggr) {
        if (aggr === "count" || aggr === "total") {
            _aggr = 0;
        } else if (aggr === "value") {
            _aggr = 3;
        } else if (aggr === "avg") {
            _aggr = 6;
        } else {
            throw new Error("setAgg(" + aggr + ") not possible");
        }
        _index = -1;
    }

    function constructIndex() {
        if (_source === 27 || _source === 29) {
            if (_filter_type === 0) {
                if (_aggr === 0) {
                    return _source;
                } else if (_aggr === 3) {
                    return _source + 1;
                }
            }
        } else if (_source !== -1) {
            if (_aggr !== -1) {
                if (_filter_type !== -1) {
                    return _filter_type + _aggr + _source;
                }
            }
        }
        throw new Error("No valid config _source=" + _source + ", _owner=" + _filter_type + ", _aggr=" + _aggr);
    }

    function getType(){
        return "data-"+xy;
    }
    this.getCSType = getType;

    function getIndex() {
        if (_index === -1) {
            _index = constructIndex();
            console.log("getIndex(_source=" + _source + ", _filter_type=" + _filter_type + ", _aggr=" + _aggr+") = " + _index);
        }
        return _index;
    }
    this.getIndex = getIndex;

    this.reset = function () {
        if (defaults) {
            this.setOwner(defaults.owner);
            setSource(defaults.source);
            setAgg(defaults.agg);
        } else {
            _filter_type = -1;
            _source = -1;
            _aggr = -1;
            _index = -1;
        }
        _evalString = null;
    };
    this.reset();

    this.getDataValue = function(data) {
        var index = getIndex(),
            val;
        if(index === -2){
            // eval function
            val = (function () {
                "use strict";
                var FEATURES = data;
                var mathEval = eval(_evalString);
                var result = math.eval(mathEval);
                console.log("_evalString="+_evalString + " =?> "+ result); //TODO
                return result;
            })();
        } else {
            if (_aggr === 6) {
                val = data[getDataIndex(index - 3)] / data[getDataIndex(index - 6)]; //avg
            } else {
                val = data[getDataIndex(index)];
            }
        }
        if (!val) {
            throw new Error("Strange value on " + collum_names[index]);
        }
        return val;
    };

    this.getTag = function(){
        return "[" + collum_tags[getIndex()] + "]";
    };

    this.getIndexFromTag = function (tag) {
        return collum_tags.indexOf(tag);
    };

    function getDataIndex(index) {
        if (index >= 6) {
            index -= 3;
        }
        return index;
    }

    var _evalString = null;
    this.setFunction = function (evalString) {
        _evalString = evalString;
        _index = -2;
    };
}

var interval = (function () {
    var years = [];
    return {
        a: function (b) {

        }
    };
})();
