/**
 * Created by Dennis on 29-1-2017.
 */

// If the drag behavior prevents the default click,
// also stop propagation so we don’t click-to-zoom.
function stopped() {
    if (d3.event.defaultPrevented) d3.event.stopPropagation();
}

function neq(a, b) {
    return a !== b;
}

function wrap(funca, funcb) {
    return function (arg) {
        return funca(funcb(arg));
    }
}
function sortByProperty(key) {
    return function (a, b) {
        if (a.properties[key] < b.properties[key]) {
            return -1;
        } else if (a.properties[key] > b.properties[key]) {
            return 1;
        }
        return 0;
    }
}