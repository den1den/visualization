/*global d3,SelectionManager,dataTypeChange*/
/*global DataType,DataTypeTitle,householdfilter*/
function DataTypeSelector(rootId, dataType) {
    var root = d3.select(rootId),
        title = new DataTypeTitle(rootId + "-title", dataType),
        functionEval;

    function commitValueChange(){
        dataTypeChange("DataTypeSelector", dataType);
    }

    // Construct the elements HTML
    (function() {
        var avg = false,
            dr0 = new DropDownMenu(root, "Data",
                [["Real estate", 0], ["Measurements", 1]],
                function (v) {
                    var changed;
                    if (v === 0) {
                        changed = dr1.show();
                        dr2.hide();
                    } else {
                        changed = dr2.show();
                        dr1.hide();
                    }
                    if (changed) {
                        dataType.reset();
                    }
                }, true),
            dr1 = new DropDownMenu(root, "Filter",
                householdfilter,
                function (v) {
                    //agg == 0
                    dataType.setAgg("count");
                    dataType.setSource(v);
                    dataType.setOwner("all");

                    functionEval.setEvalString(dataType.getTag());
                    commitValueChange();
                }, false),
            dr2 = new DropDownMenu(root, "Energy type",
                [["CO<sub>2</sub>", "co2"], ["Electricity", "electricity"], ["Gas", "gas"], ["Solar", "solar"], ["Other", "other"]],
                function (v) {
                    dr3.setValue("all");
                    if(v === "solar" || v === "other"){
                        dr3.hide();
                    } else {
                        dr3.show();
                    }
                }, false),
            dr3 = new DropDownMenu(root, "Filter",
                [["All", "all"], ["Private", "private"], ["Commercial", "commercial"]],
                function (v) {
                    dataType.setAgg("value");
                    dataType.setSource(dr2.getLastValue());
                    dataType.setOwner(v);

                    functionEval.setEvalString(dataType.getTag());
                    commitValueChange();
                }, false);
        functionEval = new FunctionWriter(root, function (v) {
            dataType.setFunction(v);
            commitValueChange();
        });
    })();

    this.getValueFromData = dataType.getDataValue;
}

function FunctionWriter(root, onChange) {
    var input = root.append("div")
        .append("input")
        .on("input", function () {
            var evalString = parseEvalString(this.value);
            if(evalString) {
                onChange(evalString);
            }
        });

    //http://2ality.com/2014/01/eval.html
    function parseEvalString(evalString, feature) {
        console.log("parseEvalString(" + evalString + ")");
        var vars = [],
            re = /\[[^\]]+]/i,
            x;
        while (true) {
            x = re.exec(evalString);
            if(x === null){
                break;
            }
            var varName = x[0].substring(1, x[0].length - 1);
            vars.push(varName);
            evalString = evalString.substring(0, x.index) + varName + evalString.substring(x.index + x[0].length);
        }
        var output = "";
        vars.filter(function(v, i, a){ return a.indexOf(v) === i;}).forEach(function(vaR){
            var dataIndex = collum_tags.indexOf(vaR);
            //var val = feature[dataIndex];
            var val = "FEATURES["+dataIndex+"]";
            output += "var "+vaR+" = "+val+"; ";
        });
        output += evalString;
        try {
            var FEATURES = [];
            for(var i = 0; i < collum_tags.length; i++) {FEATURES.push(1);}
            eval(output);
            return output;
        } catch (e){
            console.log("Could not eval(" + output + ") " + e.message);
            return null;
        }
    }
    this.setEvalString = function (str) {
        input.property("value", str);
    };
    input.property("value", "[co2]/[real_estates]*2(");
}
function ToggleMenu(root, options, onChange) {
    var buttons = root.append("div")
        .attr("data-toggle", "buttons");
    options.forEach(function (o) {
        buttons.append("label")
            .attr("class", "btn btn-primary")
            .on("click", function () {
                onChange(o[1]);
            })
            .html(o[0])
            .append("input")
            .attr("type", "radio");
    });
}
function DropDownMenu(root, name, values, onChange, visible) {
    var _visible = visible,
        dropdown = root.append("div")
        .attr("class", "dropdown");
    dropdown.append("button")
        .attr("class", "btn btn-secondary dropdown-toggle")
        .attr("type", "button")
        .attr("data-toggle", "dropdown")
        .html(name);
    var dropdownOptions = dropdown.append("div")
        .attr("class", "dropdown-menu");
    values.forEach(function (v) {
        dropdownOptions.append("button")
            .attr("class", "dropdown-item")
            .html(v[0])
            .on("click", function () {
                setValue(v[1]);
            });
    });
    if (!_visible) {
        hide();
    }

    function show() {
        var r = !_visible;
        dropdown.style("display", "inherit");
        _visible = true;
        return r;
    }
    this.show = show;

    function hide() {
        var r = _visible;
        dropdown.style("display", "none");
        _visible = false;
        return r;
    }
    this.hide = hide;

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