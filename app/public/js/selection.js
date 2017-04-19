function selectionChange(source, newSelected, newSelectedLevel) {
    SelectionManager.fireChange({
        type: "selection",
        source: source,
        data: newSelected,
        level: newSelectedLevel
    });
}
function dataTypeChange(source, newSelectedDataType) {
    SelectionManager.fireChange({
        type: newSelectedDataType.getCSType(),
        source: source,
        index: newSelectedDataType.getIndex()
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
        fireChange: function (selectionChange) {
            console.log("fireSelectionChange(type=" + selectionChange.type + ", source=" + selectionChange.source + ")");
            var newSelection = selectionChange;
            var previousSelection = (lastSelections[newSelection.type] ? lastSelections[newSelection.type] : null);
            lastSelections[newSelection.type] = newSelection;
            getChangeListener(selectionChange.type).forEach(function (onChange) {
                onChange(newSelection, previousSelection);
            });
        },
        addChangeListener: function (type, onChange) {
            // onChange(newSelection, previousSelection)
            var types = (typeof type === "string") ? [type] : type;
            types.forEach(function (type) {
                getChangeListener(type).push(onChange);
            });
        }
    };
})();