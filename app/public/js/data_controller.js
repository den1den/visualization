
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
            if (_aggr === "avg") {
                var realI, estatesI;
                if(_source === "solar" || _source === "other"){
                    realI = getDataIndex(index - 2);
                    estatesI = getDataIndex(index - 1);
                } else {
                    realI = getDataIndex(index - 3);
                    estatesI = getDataIndex(index - 6);
                    if(_owner !== "all"){
                        // Also provided in data set ...
                        if(data[getDataIndex(index)] !== Math.round(data[realI] / data[estatesI])) {
                            console.log("DEBUG: data mismatch on cols("+realI+"/"+estatesI+" = "+getDataIndex(index)+") -> " + data[getDataIndex(index)] + " =?= " + (data[realI] / data[estatesI]));
                        }
                    }
                }
                var real = data[realI],
                    estates = data[estatesI];
                if (real < 0) {
                    return null;
                }
                if (estates < 0) {
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
    function getDataIndex(index){
        if(index > 5){
            if(index > 28){
                if(index > 31){
                    index--;
                }
                index--;
            }
            index -= 3;
        }
        return index;
    }

    this.getTag = function(){
        return "[" + collum_tags[getIndex()] + "]";
    };

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

var YearSelection = (function () {
    var years = [2011, 2012, 2013, 2014, 2015],
        selected = [true, true, true, true, true];
    var types = ["sum", "dev", "avg"];
    var type = types[0];
    return {
        setSelected: function (index, selected) {
            selected[index] = true;
        },
        getYears: function () {
            return years;
        },
        setType: function (t) {
            type = t;
        },
        agg: function (datas) {
            var vals = [],
                i;
            for(i = 0; i < collum_names.length - 5; i++){
                vals.push(0);
            }
            for(var y = 0; y < years.length; y++){
                if(selected[y] === true){
                    for (i = 0; i < datas[y].length; i++){

                    }
                }
            }
        }
    };
})();
