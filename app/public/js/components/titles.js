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
        setTitle(newSelection);
    });
    function setTitle(newSelection){
        var selectedText;
        if (newSelection === null) {
            selectedText = "Nothing selected";
        } else if (newSelection.data === null) {
            selectedText = "Den Haag (city)";
        } else {
            selectedText = newSelection.data.properties[propertyKey[newSelection.level]] + " (" + typeName[newSelection.level].toLowerCase() + ")";
        }
        el.text(selectedText);
    }
}