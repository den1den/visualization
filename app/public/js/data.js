/**
 * Created by Dennis on 27-1-2017.
 */
/*global d3,topojson,neq,dataTypeChange,math*/
var typeName = ["City part", "District", "Neighborhood"];
var objectKey = ["stadsdeel", "wijken", "buurten"];
var propertyKey = ["stadsdeelnaam", "wijknaam", "buurtnaam"];
var propertyCodeKey = ["stadsdeelcode", "wijkcode", "buurtcode"];

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
        "Average solar energy production (kWh)", // CUSTOM ADDED

        "Real estates with other power production", // 30 other
        "Other production (kWh)",
        "Average other energy production (kWh)" // CUSTOM ADDED
    ],
    collum_tags = [
        "N",
        "N_elec",
        "N_gas",
        "co2",
        "power",
        "gas",
        "co2]/[N",
        "power]/[N",
        "gas]/[N",
        "N_private",
        "N_private_elec",
        "N_private_gas",
        "co2_private",
        "power_private",
        "gas_private",
        "co2_private]/[N_private",
        "power_private]/[N_private_elec",
        "gas_private]/[N_private_gas",
        "N_comm",
        "N_comm_elec",
        "N_comm_gas",
        "co2_comm",
        "power_comm",
        "gas_comm",
        "co2_comm]/[N_comm",
        "power_comm]/[N_comm_elec",
        "gas_comm]/[N_comm_gas",
        "N_solar",
        "solar",
        "solar]/[N",
        "N_other",
        "other",
        "other]/[N"
    ];

function DataType(xy, defaults){
    var _owner = null,
        _source = null,
        _aggr = null,
        _index = -1,
        _evalString = null;

    var selected = 1;
    // selected === 1 => index
    // selected === 2 => eval string

    this.setDTV = function(owner, source, aggr){
        var changed = false;
        if(owner !== null){
            _owner = owner;
            changed = true;
        }
        if(source !== null){
            _source = source;
            changed = true;
        }
        if(aggr !== null){
            _aggr = aggr;
            changed = true;
        }
        _index = -1;
        console.log("setDTV to "+toString());
        return changed;
    };

    function constructIndex(){
        var i;
        if(_source==="solar" || _source==="other"){
            if(_owner !== "all"){
                return false;
            }
            i = (_source==="solar" ? 27 : 30); // solar or other
            if(_aggr==="count"){
                _index = i;
            } else if (_aggr==="value") {
                _index = i + 1;
            } else if (_aggr==="avg") {
                _index = i + 2;
            } else {
                return false;
            }
        } else {
            if (_owner === "all") {
                i = 0;
            } else if (_owner === "private") {
                i = 9;
            } else if (_owner === "commercial") {
                i = 18;
            } else {
                return false;
            }
            if(_aggr === "count"){
                if(_source === "all") {
                    _index = i;
                } else if(_source === "electricity"){
                    _index = i + 1;
                } else if (_source === "gas"){
                    _index = i + 2;
                } else {
                    return false;
                }
            } else {
                if (_aggr === "value") {
                    i += 3;
                } else if (_aggr === "avg"){
                    i += 6;
                } else {
                    return false;
                }
                if(_source === "co2"){
                    _index = i;
                } else if (_source === "electricity"){
                    _index = i + 1;
                } else if (_source === "gas"){
                    _index = i + 2;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    function getType(){
        return "data-"+xy;
    }
    this.getCSType = getType;

    function getIndex() {
        if (_index === -1) {
            if(!constructIndex()){
                constructIndex();
                throw new Error("Could not construct index of "+toString());
            } else {
                console.log("Constructed index of "+toString());
            }
            var index = _index;
        }
        return _index;
    }
    this.getIndex = getIndex;
    this.setToIndex = function(){
        selected = 1;
    };

    this.reset = function () {
        if (defaults) {
            this.setDTV(defaults.owner, defaults.source, defaults.agg);
        } else {
            _owner = null;
            _source = null;
            _aggr = null;
            _index = -1;
        }
        _evalString = null;
        selected = 1;
    };
    this.reset();

    this.getDataValue = function(data) {
        var val;
        if(selected === 2) {
            // eval function
            var mathEval;
            val = (function () {
                "use strict";
                var FEATURES = data;
                mathEval = eval(_evalString);
                var result = math.eval(mathEval);
                //console.log("_evalString=`" + _evalString + "` = " + result); //TODO
                return result;
            })();
            if (isNaN(val)) {
                throw new Error("Strange value on function " + mathEval);
            }
        } else if (selected <= 1) {
            var index = getIndex();
            if (_aggr === 6) {
                var real = data[getDataIndex(index - 3)];
                if(real < 0){
                    return null;
                }
                var estates = data[getDataIndex(index - 6)];
                if(estates < 0){
                    throw new Error("Amount of real estates missing!");
                }
                val = real / estates; //avg
            } else {
                val = data[getDataIndex(index)];
                if(val < 0){
                    console.log("Value is missing");
                    return null;
                }
            }
            if (isNaN(val)) {
                throw new Error("Strange value on " + collum_names[index]);
            }
        } else {
            throw new Error("Could not get value on unspecified DataType");
        }
        return val;
    };

    this.getTag = function(){
        return "[" + collum_tags[getIndex()] + "]";
    };

    function getDataIndex(index) {
        if (index >= 6) {
            index -= 3;
        }
        return index;
    }

    this.setFunction = function (evalString) {
        _evalString = evalString;
        setToFunction();
    };
    function setToFunction(){
        selected = 2;
    }
    this.setToFunction = setToFunction;

    function toString() {
        return "DataType(_source=" + _source + ", _owner=" + _owner + ", _aggr=" + _aggr + ", selected=" + selected + ", _index=" + _index + ", _evalString=" + _evalString + ")";
    }
}
DataType.getDataIndexOfVar = function (vaR) {
    var i;
    if((i=collum_tags.indexOf(vaR)) === -1){
        return false;
    }
    return i;
};

var interval = (function () {
    var years = [];
    return {
        a: function (b) {

        }
    };
})();
