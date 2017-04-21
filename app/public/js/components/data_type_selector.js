/*global d3,SelectionManager,dataTypeChange*/
/*global DataType,DataTypeTitle,householdfilter*/
var filterVals = [
    {title: "All", value: "all"}, {title: "Private", value: "private"}, {title: "Commercial", value: "commercial"}
];
function DataTypeSelector(rootId, dataType) {
    var root = d3.select(rootId),
        title = new DataTypeTitle(rootId + "-title", dataType);

    function commitSelectionValueChange(){
        functionEval.setEvalString(dataType.getTag());
        dataTypeChange("DataTypeSelector", dataType);
    }

    function commitInputValueChange(){
        dataTypeChange("DataTypeSelector", dataType);
    }

    // Construct the elements HTML
    var avg = false,
        dr0 = new DropDownMenu(root,
            "Data",
            [{title: "Real estate", value: 0}, {title: "Measurements", value: 1}],
            function (v) {
                var changed;
                if (v === 0) {
                    changed = realEstateSource.show();
                    energySource.hide();
                    selectAvg.hide();
                    energyFilter.hide();
                } else {
                    changed = energySource.show();
                    selectAvg.show();
                    realEstateSource.hide();
                    realEstateFilter.hide();
                }
                if (changed) {
                    dataType.reset();
                }
            },
            true),
        realEstateSource = new DropDownMenu(root,
            "Type",
            [
                {title: "All", value: "all"}, {title: "With electricity", value: "electricity"}, {title: "With gas", value: "gas"}, {title: "With solar", value: "solar"}, {title: "With other source", value: "other"}
            ],
            function (v) {
                if (v === "all" || v === "electricity" || v === "gas") {
                    // keep owner
                    dataType.setDTV(null, v, "count");
                    realEstateFilter.show();
                } else {
                    // set owner to "all"
                    dataType.setDTV("all", v, "count");
                    realEstateFilter.hide();
                    realEstateFilter.setIndex(0);
                }

                commitSelectionValueChange();
            },
            false),
        realEstateFilter = new DropDownMenu(root,
            "Filter",
            filterVals,
            function(v){
                dataType.setDTV(v, null, null);

                commitSelectionValueChange();
            },
            false
        ),
        selectAvg = new SelectionBox(root,
            "Average", //
            false,
            function (v) {
                dataType.setDTV(null, null, (v ? "avg" : "value"));
                if(energySource.isChanged()){
                    commitSelectionValueChange();
                }
            },
            false
        ),
        energySource = new DropDownMenu(root,
            "Energy type",
            [
                {title: "CO<sub>2</sub>", value: "co2"}, {title: "Electricity", value: "electricity"}, {title: "Gas", value: "gas"}, {title: "Solar", value: "solar"}, {title: "Other", value: "other"}
            ],
            function (v) {
                energyFilter.setIndex(0);
                if(v === "solar" || v === "other"){
                    energyFilter.hide();
                } else {
                    energyFilter.show();
                }
                dataType.setDTV(null, v, null);

                commitSelectionValueChange();
            },
            false),
        energyFilter = new DropDownMenu(root,
            "Filter", // Energy filter
            filterVals,
            function (v) {
                dataType.setDTV(v, null, null);

                commitSelectionValueChange();
            },
            false),
        functionEval = new FunctionWriter(root, "Expression", function (v) {
            dataType.setFunction(v);
            commitInputValueChange();
        }),
        zeroSelection = new SelectionBox(root, "Zero", dataType.isZero(), function (v) {
            dataType.setZero(v);
            commitInputValueChange();
        },
        true);

    functionEval.setEvalString(dataType.getTag());
}
function FunctionWriter(root, title, onChange) {
    var group = root.append("div")
        .attr("class", "form-group function-writer");
    var label = group.append("label")
        .attr("class", "form-control-label")
        .attr("for", "function-writer-"+FunctionWriter.fncount)
        .text(title),
        input = group.append("input")
            .attr("type", "text")
            .attr("class", "form-control")
            .attr("id", "function-writer-"+FunctionWriter.fncount)
        .on("input", function () {
            setStatic(false);
            var r = parseEvalString(this.value);
            if(typeof r === "string") {
                setSuccess(true);
                onChange(r);
            } else if(r !== null) {
                setWarning("Could not find parameters: "+r.map(function (v) {
                    return "\"" + v + "\"";
                    }));
            } else {
                setDanger(true);
            }
        })
        .on("click", function () {
            setStatic(false);
        }),
        feedbackTxt = group.append("div").attr("class", "form-control-feedback");
    group.append("small").attr("class", "form-text text-muted").text("Help text");

    function parseEvalString(input) {
        console.log("parseEvalString(" + input + ")");
        var vars = [],
            re = /\[[^\]]*]/i,
            x,
            evalString = input;
        while (true) {
            x = re.exec(evalString);
            if(x === null) {
                break;
            }
            var pre = evalString.substring(0, x.index),
                varName = x[0].substring(1, x[0].length - 1),
                post = evalString.substring(x.index + x[0].length);
            vars.push(varName);
            evalString = pre + "\"+" + varName + "+\"" + post; // replace `pre[x]post` by `pre"+x+"post`
        }
        /**
         * Output contains a string that generates the mathematical expressions
         * Example: var co2 = FEATURES[0]; var y = FEATURES[6]; "" + co2 + " - " + y + "^2"
         * `eval()` will then return: "9000-4343^2"
         * Execution math.eval() will then return -18852649 (so actually parsing the ^ as power instead of XOR)
         */
        var output = "";
        // set up vars
        var invalidVars = vars.filter(function(v, i, a){ return a.indexOf(v) === i;}).filter(function(vaR){
            var varDataIndex = DataType.getDataIndexOfVar(vaR);
            if(varDataIndex === null){
                return true;
            }
            var fRef = "FEATURES["+varDataIndex+"]";
            output += "var " + vaR + " = " + fRef + "; ";
            return false;
        });
        if(invalidVars.length > 0){
            console.log("parseEvalString: could not find vars: " + invalidVars.toString());
            return invalidVars;
        }
        // append evalString
        output += "\"" + evalString + "\";";
        try {
            // Test if it works
            var FEATURES = [];
            for(var i = 0; i < DataType.dataElementsCount; i++) {FEATURES.push(1);}
            var mathString = eval(output);
            var result = math.eval(mathString);
            return output;
        } catch (e){
            console.log("parseEvalString: could not eval(" + output + ") " + e.message);
            return null;
        }
    }
    this.setEvalString = function (str) {
        input.property("value", str);
        setStatic(true);
    };

    function setStatic(s){
        input.classed("static", s);
        if(s === true){
            setWarning(false);
            setSuccess(false);
            setDanger(false);
        }
    }

    function setSuccess(s){
        if(s === true){
            setWarning(false);
            setDanger(false);
        }
        group.classed("has-success", s);
        input.classed("form-control-success", s);
    }

    function setWarning(w){
        if(w === false) {
            feedbackTxt.style("display", "none");
        } else {
            setSuccess(false);
            setDanger(false);
            feedbackTxt.text(w);
            feedbackTxt.style("display", "block");
        }
        group.classed("has-warning", !!w);
        input.classed("form-control-warning", !!w);
    }

    function setDanger(d){
        if(d === true){
            setSuccess(false);
            setWarning(false);
        }
        group.classed("has-danger", d);
        input.classed("form-control-danger", d);
    }

    FunctionWriter.fncount++;
}
FunctionWriter.fncount = 0;
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
function SelectionBox(root, title, initiallyChecked, onChange, visible) {
    var _visible = visible,
        _isChanged = false,
        _checked = initiallyChecked,
        checkbox = root.append("div")
        .attr("class", "form-check");
    var label = checkbox.append("label")
        .attr("class", "form-check-label");
    label.append("input")
        .attr("class", "form-check-input")
        .attr("type", "checkbox")
        .property("checked", initiallyChecked);
    label.append("span").text(title);
    label.on("change", function () {
        _checked = !_checked;
        _isChanged = true;
        onChange(_checked);
    });
    if (!_visible) {
        hide();
    }

    function show() {
        var r = !_visible;
        checkbox.style("display", "inherit");
        _visible = true;
        return r;
    }
    this.show = show;

    function hide() {
        var r = _visible;
        checkbox.style("display", "none");
        _visible = false;
        return r;
    }
    this.hide = hide;

    this.isChanged = function () {
        return _isChanged;
    };
}
function DropDownMenu(root, title, values, onChange, visible) {
    var _visible = visible,
        _isChanged = false,
        group = root.append("div")
        .attr("class", "form-group"),
        dropdown = group.append("div")
        .attr("class", "dropdown");
    var button = dropdown.append("button")
        .attr("class", "btn btn-secondary dropdown-toggle")
        .attr("type", "button")
        .attr("data-toggle", "dropdown")
        .html(title),
        dropdownOptions = dropdown.append("div")
        .attr("class", "dropdown-menu");
    values.forEach(function (v, i) {
        dropdownOptions.append("button")
            .attr("class", "dropdown-item")
            .html(v.title)
            .on("click", function () {
                setIndex(i);
            });
    });
    if (!_visible) {
        hide();
    }
    this.isChanged = false;

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

    var v = null;
    this.getLastValue = function () {
        return v.value;
    };
    function setIndex(i) {
        v = values[i];
        button.html(v.title);
        _isChanged = true;
        onChange(v.value);
    }
    this.setIndex = setIndex;

    this.isChanged = function () {
        return _isChanged;
    };
}