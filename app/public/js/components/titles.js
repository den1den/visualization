/*global d3,SelectionManager,collum_names,propertyKey,typeName*/
function DataTypeTitle(rootId, dataType) {
    var el = d3.select(rootId);
    setTitle(dataType);

    SelectionManager.addChangeListener(dataType.getCSType(), function (newChangeObject, previousChangeObject) {
        setTitle(newChangeObject.value);
    });
    function setTitle(dataType){
        if (dataType === null) {
            el.html("None set");//.style("color", "#999");
        } else {
            el.html(dataType.getTitle());//.style("color", "#999");
        }
    }
}

function SelectionTitle(rootId) {
    var el = d3.select(rootId);
    el.text("Den Haag (city)");

    SelectionManager.addChangeListener("selection", function (newChangeObject, previousChangeObject) {
        setTitle(newChangeObject.value);
    });
    function setTitle(dataType){
        var selectedText;
        if (dataType === null) {
            selectedText = "Nothing selected";
        } else if (dataType.data === null) {
            selectedText = "Den Haag (city)";
        } else {
            selectedText = dataType.data.properties[propertyKey[dataType.level]] + " (" + typeName[dataType.level].toLowerCase() + ")";
        }
        el.html(selectedText);
    }
}