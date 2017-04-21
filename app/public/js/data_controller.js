
function DataType(xy, defaults){
    var _owner = null,
        _source = null,
        _aggr = null,
        _colIndex = -1,
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
        _colIndex = -1;
        console.log("setDTV to "+toString());
        return changed;
    };

    function constructColIndex(){
        var i;
        if(_source==="solar" || _source==="other"){
            if(_owner !== "all"){
                return false;
            }
            i = (_source==="solar" ? 27 : 30); // solar or other
            if(_aggr==="count"){
                _colIndex = i;
            } else if (_aggr==="value") {
                _colIndex = i + 1;
            } else if (_aggr==="avg") {
                _colIndex = i + 2;
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
                    _colIndex = i;
                } else if(_source === "electricity"){
                    _colIndex = i + 1;
                } else if (_source === "gas"){
                    _colIndex = i + 2;
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
                    _colIndex = i;
                } else if (_source === "electricity"){
                    _colIndex = i + 1;
                } else if (_source === "gas"){
                    _colIndex = i + 2;
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

    function getColIndex() {
        if (_colIndex === -1) {
            if(!constructColIndex()){
                constructColIndex();
                throw new Error("Could not construct ColIndex of "+toString());
            } else {
                console.log("Constructed ColIndex of "+toString());
            }
        }
        return _colIndex;
    }
    this.getColIndex = getColIndex;
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
            _colIndex = -1;
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
            if (val !== null && isNaN(val)) {
                console.log("Strange value on function " + mathEval);
                return null;
            }
        } else if (selected <= 1) {
            var colIndex = getColIndex();
            if (_aggr === "avg") {
                var realI, estatesI;
                if(_source === "solar" || _source === "other"){
                    realI = getDataIndex(colIndex - 2);
                    estatesI = getDataIndex(colIndex - 1);
                } else {
                    realI = getDataIndex(colIndex - 3);
                    estatesI = getDataIndex(colIndex - 6);
                    if(_owner !== "all"){
                        // Also provided in data set ...
                        if(data[getDataIndex(colIndex)] !== Math.round(data[realI] / data[estatesI])) {
                            console.log("DEBUG: data mismatch on cols("+realI+"/"+estatesI+" = "+getDataIndex(colIndex)+") -> " + data[getDataIndex(colIndex)] + " =?= " + (data[realI] / data[estatesI]));
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
                val = data[getDataIndex(colIndex)];
                if(val < 0){
                    console.log("DEBUG: Value is missing");
                    return null;
                }
            }
            if (isNaN(val)) {
                throw new Error("Strange value on " + collum_names[colIndex]);
            }
        } else {
            throw new Error("Could not get value on unspecified DataType");
        }
        return val;
    };
    function getDataIndex(colIndex){
        if(colIndex < 0 || colIndex > 31){
            return null;
        }
        if(colIndex > 5){
            if(colIndex > 28){
                if(colIndex > 31){
                    colIndex--;
                }
                colIndex--;
            }
            colIndex -= 3;
        }
        return colIndex;
    }
    DataType.getDataIndex = getDataIndex;

    this.getTag = function(){
        return "[" + collum_tags[getColIndex()] + "]";
    };

    this.setFunction = function (evalString) {
        _evalString = evalString;
        selected = 2;
    };

    this.getUsedType = function () {
        return selected;
    };

    function toString() {
        return "DataType(_source=" + _source + ", _owner=" + _owner + ", _aggr=" + _aggr + ", selected=" + selected + ", _index=" + _colIndex + ", _evalString=" + _evalString + ")";
    }
}
DataType.getDataIndexOfVar = function (vaR) {
    var i;
    if((i=collum_tags.indexOf(vaR)) === -1){
        return null;
    }
    return DataType.getDataIndex(i);
};
DataType.dataElementsCount = collum_names.length - 5;

var YearSelection = (function () {
    var years = [2011, 2012, 2013, 2014, 2015],
        selected = [true, true, true, true, true];
    var types = ["sum", "std", "avg"];
    var type = types[0];
    function getSelected(){
        return years.filter(function (y, i) {
            return selected[i];
        });
    }
    function toString(){
        return "YearSelection(" + getSelected() + ")";
    }
    return {
        setSelected: function (index, s) {
            selected[index] = s;
        },
        isSelected: function (index) {
            return selected[index];
        },
        getYears: function () {
            return years;
        },
        getSelectedYears: getSelected,
        setType: function (t) {
            type = t;
        },
        aggOverYears: function (datas) {
            var vals = [],
                i, y;
            for(i = 0; i < DataType.dataElementsCount; i++){
                vals.push(0);
            }
            var selectedYears = getSelected();
            for(i = 0; i < vals.length; i++){
                var sum = 0, foundKnown = false;;
                for(y = 0; y < selectedYears.length; y++){
                    var val = datas[selectedYears[y]][i];
                    if(val !== null) {
                        sum += val;
                        foundKnown = true;
                    }
                }
                if (!foundKnown) {
                    vals[i] = null;
                } else {
                    if (type === "sum") {
                        vals[i] = sum;
                    } else if (type === "avg") {
                        vals[i] = sum / selectedYears.length;
                    } else if (type === "std") {
                        var avg = sum / selectedYears.length;
                        vals[i] = 0;
                        for (y = 0; y < selectedYears.length; y++) {
                            vals[i] += Math.pow(datas[selectedYears[y]][i] - avg, 2);
                        }
                        vals[i] /= selectedYears;
                    }
                }
            }
            return vals;
        },
        toString: toString
    };
})();
