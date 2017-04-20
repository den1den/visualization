function selectionChange(source, newSelected, newSelectedLevel) {
    SelectionManager.fireChange({
        type: "selection",
        source: source,
        value: {
            data: newSelected,
            level: newSelectedLevel
        }
    });
}
function dataTypeChange(source, newSelectedDataType) {
    SelectionManager.fireChange({
        type: newSelectedDataType.getCSType(),
        source: source,
        value: newSelectedDataType
    });
}
var SelectionManager = (function () {
    var changeListeners = {};
    function getChangeListener(type) {
        if(!changeListeners[type]) {
            changeListeners[type] = [];
        }
        return changeListeners[type];
    }
    var lastSelections = {};
    return {
        fireChange: function (newChangeObject) {
            console.log("fireSelectionChange(type=" + newChangeObject.type + ", source=" + newChangeObject.source + ", change=" + newChangeObject.value + ")");
            var previousChangeObject = (lastSelections[newChangeObject.type] ? lastSelections[newChangeObject.type] : null);
            lastSelections[newChangeObject.type] = newChangeObject;
            getChangeListener(newChangeObject.type).forEach(function (onChange) {
                onChange(newChangeObject, previousChangeObject);
            });
        },
        addChangeListener: function (type, onChange) {
            // onChange(newChangeObject, previousChangeObject)
            var types = (typeof type === "string") ? [type] : type;
            types.forEach(function (type) {
                getChangeListener(type).push(onChange);
            });
        }
    };
})();