/*global d3,SelectionManager,collum_names,propertyKey,typeName*/
function DataTypeTitle(rootId, dataType) {
    var el = d3.select(rootId);
    setTitle(dataType.getIndex());

    SelectionManager.addChangeListener(dataType.getCSType(), function (newSelection, previousSelection) {
        setTitle(newSelection.index);
    });
    function setTitle(index){
        if (index === -1) {
            el.html("None set").style("color", "#999");
        } else {
            el.html(collum_names[index]).style("color", "#000");
        }
    }
}

function SelectionTitle(rootId) {
    var el = d3.select(rootId);
    el.text("Den Haag (city)");

    SelectionManager.addChangeListener("selection", function (newSelection, previousSelection) {
        setTitle(newSelection.data);
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
        el.text(selectedText);
    }
}