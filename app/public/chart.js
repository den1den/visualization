/**
 * Created by Dennis on 29-1-2017.
 */

Chart = function (csx, csy) {
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var width = 500;
    var height = 500;

    var xAxis = d3.scaleTime().range([0, width]);
    var yAxis = d3.scaleLinear().range([height, 0]);

    var svg = d3.select("#chart")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);
    var root = svg
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    function getXFn() {
        return function (d) {
            return csx.getValue(d);
        }
    }

    function getYFn() {
        return function (d) {
            return csy.getValue(d);
        }
    }

    function getXCoordFn() {
        var x = getXFn();
        return function (d) {
            return xAxis(x(d));
        }
    }

    function getYCoordFn() {
        var y = getYFn();
        return function (d) {
            return yAxis(y(d));
        }
    }

    this.setChartData = function (data) {
        var valueLine = d3.line().x(getXFn()).y(getYFn());

        // Scale the range of the data
        xAxis.domain(d3.extent(data, getXFn()));
        yAxis.domain([0, d3.max(data, getYFn())]);

        root.selectAll('*').delete();

        root.append('path')
            .data([data])
            .attr('class', 'line')
            .attr('d', valueLine);

        root.selectAll('dot')
            .data(data)
            .enter()
            .append('circle')
            .attr("r", 5)
            .attr("cx", getXCoordFn())
            .attr("cy", getYCoordFn());

        // Add the X Axis
        root.append("g")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.axisBottom(xAxis));

        // Add the Y Axis
        root.append("g")
            .call(d3.axisLeft(yAxis));
    }
};

CollumSelector = function () {
    var collum_names_nl = [
        'Totaal aantal Vastgoedobjecten', 'Totaal aantal Vastgoedobjecten Elektra', 'Totaal aantal Vastgoedobjecten Gas',
        'Totaal CO2 uitstoot (kg)', 'Totaal verbruik Elektra (kWh)', 'Totaal verbruik Gas (m3)',
        'Aantal Vastgoedobjecten Particulier', 'Aantal Vastgoedobjecten Elektra Particulier', 'Aantal Vastgoedobjecten Gas Particulier',
        'CO2 uitstoot Particulier (kg)', 'Verbruik Elektra Particulier (kWh)', 'Verbruik Gas Particulier (m3)',
        'Gemiddelde CO2 uitstoot Particulier (kg)', 'Gemiddelde verbruik Elektra Particulier (kWh)', 'Gemiddelde verbruik Gas Particulier (m3)', 'Aantal Vastgoedobjecten Zakelijk', 'Aantal Vastgoedobjecten Elektra Zakelijk', 'Aantal Vastgoedobjecten Gas Zakelijk', 'CO2 uitstoot Zakelijk (kg)', 'Verbruik Elektra Zakelijk (kWh)', 'Verbruik Gas Zakelijk (m3)', 'Gemiddelde CO2 uitstoot Zakelijk (kg)', 'Gemiddelde verbruik Elektra Zakelijk (kWh)', 'Gemiddelde verbruik Gas Zakelijk (m3)', 'Aantal Vastgoedobjecten Opwek Zonne-energie KV', 'Opwek Zonne-energie KV (kWh)', 'Aantal Vastgoedobjecten Opwek Overig', 'Opwek Overig (kWh)'
    ];

    var collum_names = [
        'Real estates', //0
        'Real estates with electricity',
        'Real estates with gas',
        'Co2 emissions (kg)',
        'Power consumption (kWh)',
        'Gas consumption (m3)',

        'Private real estates', //6
        'Private real estates with electricity',
        'Private real estates with gas',
        'Private Co2 emissions (kg)',
        'Private power consumption (kWh)',
        'Private gas consumption (m3)',

        'Average private Co2 emissions (kg)', //12
        'Average private power consumption (kWh)',
        'Average private gas consumption (m3)',

        'Commercial real estates', //15
        'Commercial real estates with electricity',
        'Commercial real estates with gas',
        'Commercial Co2 emissions (kg)',
        'Commercial power consumption (kWh)',
        'Commercial gas consumption (m3)',

        'Average commercial Co2 emissions (kg)',//21
        'Average commercial power consumption (kWh)',
        'Average commercial gas consumption (m3)',

        'Real estates with solar energy', //24 (private+commercial)
        'Solar energy production (kWh)',

        'Real estates with other power production', //26 (private+commercial)
        'Other production (kWh)'
    ];

    var _owner = 0;
    this.setOwner = function (owner) {
        if (owner === 'private') {
            _owner = 6;
        } else if (owner === 'commercial') {
            _owner = 15;
        } else if (owner === 'all' || owner === 'total') {
            _owner = 0;
        } else {
            throw new Exception(owner);
        }
        _index = -1;
    };
    var _source = -1;
    this.setSource = function (source) {
        if (source === 'co2' || source === 'total') {
            _source = 0;
        } else if (source === 'electricity') {
            _source = 1;
        } else if (source === 'gas') {
            _source = 2;
        } else if (source === 'solar') {
            _source = 24;
        } else if (source === 'other') {
            _source = 26;
        } else {
            throw new Exception(source);
        }
        _index = -1;
    };
    var _aggr = -1;
    this.setAgg = function (aggr) {
        if (aggr === 'count' || aggr === 'total') {
            _aggr = 0;
        } else if (aggr === 'value') {
            _aggr = 3;
        } else if (aggr === 'avg') {
            _aggr = 6;
        } else {
            throw new Exception(aggr);
        }
        _index = -1;
    };

    var _index = -1;
    var getIndex = function () {
        if (_source === 24 || _source === 26) {
            if (_owner === 0) {
                if (_aggr === 0) {
                    return _source;
                } else if (_aggr === 3) {
                    return _source + 1;
                }
            }
        } else if (_aggr == 6) {
            if (_owner !== 0) {
                return _owner + _aggr + _source;
            }
        } else {
            return _owner + _aggr + _source;
        }
        throw new Error("No valid config");
    };
    this.getIndex = function () {
        if (_index === -1) {
            _index = getIndex();
        }
        return _index;
    };
    this.getName = function () {
        return collum_names[this.getIndex()];
    };
    this.getValue = function (data) {
        return data[this.getIndex()];
    };
};

// cs = new CollumSelector();
// cs.setSource('co2');
// cs.setAgg('avg');
// cs.setOwner('commercial');
// console.log(cs.getName());
// cs.setSource('other');
// cs.setAgg('total');
// cs.setOwner('total');
// console.log(cs.getName());
