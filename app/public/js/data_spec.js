/**
 * Created by Dennis on 27-1-2017.
 */
/*global d3,topojson,neq,dataTypeChange,math*/
var typeName = ["City part", "District", "Neighborhood"];
var objectKey = ["stadsdeel", "wijken", "buurten"];
var propertyKey = ["stadsdeelnaam", "wijknaam", "buurtnaam"];
var propertyCodeKey = ["stadsdeelcode", "wijkcode", "buurtcode"];

function TopoJsonData() {
    var _data = null;
    this.get = function (callback) {
        d3.json('combined.topojson', function (error, resp) {
            if (error) {
                throw error;
            }
            console.log("Loaded combined.topojson");
            _data = resp;
            callback();
        });
    };
    this.getMesh = function (index) {
        return topojson.mesh(_data, _data.objects[objectKey[index]], neq);
    };
    this.getFeatureCollection = function (index) {
        return topojson.feature(_data, _data.objects[objectKey[index]]);
    };
    this.withAggregate = function (feature, index) {
        if (index === 2) {
            return feature;
        }
        var new_data = [],
            datagroup = _data.objects[objectKey[2]];
        if (index === -1) {
            //all
            datagroup.geometries.forEach(function (g) {
                new_data.push(g.properties.data);
            });
            return {
                'properties': {
                    'data': join(new_data)
                }
            };
        } else {
            var key = feature.properties[propertyCodeKey[index]];
            datagroup.geometries.forEach(function (g) {
                var k = g.properties[propertyCodeKey[index]];
                if (k === key) {
                    new_data.push(g.properties.data);
                }
            });
            feature.properties.data = join(new_data);
            return feature;
        }
    };
    function join(property_datas) {
        var new_data = [], i, year, newdict, newval,
            getSum = function (i) {
                var sum = 0, x, foundKnown = false;
                for (x = 0; x < property_datas.length; x++) {
                    var val = property_datas[x][year][i];
                    if (val > 0) {
                        // Only sum known values
                        sum += property_datas[x][year][i];
                        foundKnown = true;
                    }
                }
                if (foundKnown) {
                    return sum;
                } else {
                    return null;
                }
            };
        for (i = 0; i < 28; i++) {
            newdict = {};
            for (year = 2011; year <= 2015; year++) {

                if ((i >= 12 && i <= 14) || (i >= 21 && i <= 23)) {
                    // avg
                    var sum = getSum(i - 3);
                    var n = getSum(i - 6);
                    newval = sum === null ? null : (n === null ? null : (sum / n));
                } else {
                    // summation
                    newval = getSum(i);
                }
                if (!new_data[year]) {
                    new_data[year] = [];
                }
                new_data[year].push(newval);
            }
        }
        return new_data;
    }
}

function loadDatas(callback) {
    d3.queue()
        .defer(d3.json, '/stadsdeel.topojson')
        .defer(d3.json, '/wijken.topojson')
        .defer(d3.json, '/buurten-simple.topojson')
        .awaitAll(function (error, datas) {
            if (error) { throw error; }
            console.log("Data loaded");
            callback(datas);
        });
}

var collum_names_nl = [
        "Totaal aantal Vastgoedobjecten", "Totaal aantal Vastgoedobjecten Elektra", "Totaal aantal Vastgoedobjecten Gas",
        "Totaal CO2 uitstoot (kg)", "Totaal verbruik Elektra (kWh)", "Totaal verbruik Gas (m3)",
        "Aantal Vastgoedobjecten Particulier", "Aantal Vastgoedobjecten Elektra Particulier", "Aantal Vastgoedobjecten Gas Particulier",
        "CO2 uitstoot Particulier (kg)", "Verbruik Elektra Particulier (kWh)", "Verbruik Gas Particulier (m3)",
        "Gemiddelde CO2 uitstoot Particulier (kg)", "Gemiddelde verbruik Elektra Particulier (kWh)", "Gemiddelde verbruik Gas Particulier (m3)", "Aantal Vastgoedobjecten Zakelijk", "Aantal Vastgoedobjecten Elektra Zakelijk", "Aantal Vastgoedobjecten Gas Zakelijk", "CO2 uitstoot Zakelijk (kg)", "Verbruik Elektra Zakelijk (kWh)", "Verbruik Gas Zakelijk (m3)", "Gemiddelde CO2 uitstoot Zakelijk (kg)", "Gemiddelde verbruik Elektra Zakelijk (kWh)", "Gemiddelde verbruik Gas Zakelijk (m3)", "Aantal Vastgoedobjecten Opwek Zonne-energie KV", "Opwek Zonne-energie KV (kWh)", "Aantal Vastgoedobjecten Opwek Overig", "Opwek Overig (kWh)"
    ],
    collum_names = [
        "Real estates", // 0 filter=combined, aggr=total, source=
        "Real estates with electricity",
        "Real estates with gas",
        "CO<sub>2</sub> emissions (kg)", // 3
        "Power consumption (kWh)",
        "Gas consumption (m3)",

        "Average Co2 emissions (kg)", // 6 (CUSTOM ADDED)
        "Average power consumption (kWh)",
        "Average gas consumption (m3)", // (END CUSTOM ADDED)

        "Private real estates", // 9 filter=private
        "Private real estates with electricity",
        "Private real estates with gas",
        "Private Co2 emissions (kg)",
        "Private power consumption (kWh)",
        "Private gas consumption (m3)",

        "Average private Co2 emissions (kg)", // 15
        "Average private power consumption (kWh)",
        "Average private gas consumption (m3)",

        "Commercial real estates", // 18 filter=commercial
        "Commercial real estates with electricity",
        "Commercial real estates with gas",
        "Commercial Co2 emissions (kg)",
        "Commercial power consumption (kWh)",
        "Commercial gas consumption (m3)",

        "Average commercial Co2 emissions (kg)", // 24
        "Average commercial power consumption (kWh)",
        "Average commercial gas consumption (m3)",

        "Real estates with solar energy", // 27 filter=combined, solar energy
        "Solar energy production (kWh)",
        "Average solar energy production (kWh)", // CUSTOM ADDED

        "Real estates with other power production", // 30 other
        "Other production (kWh)",
        "Average other energy production (kWh)" // CUSTOM ADDED
    ],
    htmlsafe_collum_names = collum_names.slice(),
    collum_tags = [
        "N",
        "N_elec",
        "N_gas",
        "co2",
        "power",
        "gas",
        "co2]/[N",
        "power]/[N",
        "gas]/[N",
        "N_private",
        "N_private_elec",
        "N_private_gas",
        "co2_private",
        "power_private",
        "gas_private",
        "co2_private]/[N_private",
        "power_private]/[N_private_elec",
        "gas_private]/[N_private_gas",
        "N_comm",
        "N_comm_elec",
        "N_comm_gas",
        "co2_comm",
        "power_comm",
        "gas_comm",
        "co2_comm]/[N_comm",
        "power_comm]/[N_comm_elec",
        "gas_comm]/[N_comm_gas",
        "N_solar",
        "solar",
        "solar]/[N",
        "N_other",
        "other",
        "other]/[N"
    ];
htmlsafe_collum_names[3] =  "CO2 emissions (kg)";