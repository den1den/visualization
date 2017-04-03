#
# requires: npm install topojson
#
import csv
import json
import os
import re
import subprocess
from operator import itemgetter

# input
input_data_folder = os.path.join(os.path.dirname(__file__), 'data-raw')

# output
output_data_folder = os.path.join(os.path.dirname(__file__), 'data-processed')

geo2topo_command = os.path.abspath(
    os.path.join(os.path.dirname(__file__), 'node_modules', 'topojson', 'node_modules', '.bin', 'geo2topo.cmd'))

def processGeoJson(filename, process_properties):
    raw = json.load(open(os.path.join(input_data_folder, filename)))
    features = []
    for f in raw['features']:
        features.append({
            'type': f['type'],
            'geometry': f['geometry'],
            'properties': process_properties(f['properties'])
        })
    processed = {
        'type': raw['type'],
        'features': features,
    }
    output_json_filename = os.path.join(output_data_folder, filename)
    json.dump(processed, open(output_json_filename, mode='w+'), indent=1)
    geojson_to_topojson(output_json_filename)


def geojson_to_topojson(geojson_filename):
    subprocess.run([geo2topo_command, '-o', geojson_filename[:-7] + 'topojson', geojson_filename])


# Buurten
########################################################################################################################
buurtcodes = {}
buurt_in_wijk = {}

buurten_typos = {
    'Burgen en Hosten': 'Burgen en Horsten',
    'Van Stolkprk./Schev.Bosjes': 'Van Stolkpark en Scheveningse Bosjes',
    'Kraayenstein': 'Kraayenstein en Vroondaal',
    'Tedingerbuurt': 'Tedingerbroek',
    'Oostenduinen': 'Oostduinen',
    'Parkbuurt oosteinde': 'Parkbuurt Oosteinde',
}


def normalize_buurtnaam(buurtnaam: str):
    if buurtnaam in buurten_typos:
        return buurten_typos[buurtnaam]
    if buurtnaam.endswith('-zuid'):
        return buurtnaam[0:-5] + '-Zuid'
    if buurtnaam.endswith('-noord'):
        return buurtnaam[0:-6] + '-Noord'
    if buurtnaam.endswith('-oost'):
        return buurtnaam[0:-5] + '-Oost'
    if buurtnaam.endswith('-west'):
        return buurtnaam[0:-5] + '-West'
    if buurtnaam.endswith('-midden'):
        return buurtnaam[0:-7] + '-Midden'
    if buurtnaam.endswith('e.o.'):
        return buurtnaam[0:-4] + 'en omgeving'
    return buurtnaam


def process_buurt_codes(properties):
    buurtnaam = normalize_buurtnaam(properties['BUURTNAAM'])
    buurtcode = int(properties['BUURTCODE'])
    wijkcode = int(properties['WIJKCODE'])
    buurtcodes[buurtnaam] = buurtcode
    buurt_in_wijk[buurtcode] = wijkcode
    return None


def process_buurt_features(properties):
    buurtnaam = normalize_buurtnaam(properties['BUURTNAAM'])
    buurtcode = int(properties['BUURTCODE'])
    wijkcode = int(properties['WIJKCODE'])
    buurtcodes[buurtnaam] = buurtcode
    buurt_in_wijk[buurtcode] = wijkcode
    return {
        'buurtnaam': buurtnaam,
        'buurtcode': buurtcode,
        'wijkcode': wijkcode,
        'stadsdeelcode': wijk_in_stadsdeel[wijkcode],
        'data': csv_dict[buurtcode]
    }


def process_buurten():
    processGeoJson('buurten.geojson', process_buurt_codes)


def write_buurten():
    processGeoJson('buurten.geojson', process_buurt_features)


# Wijken
########################################################################################################################
wijkcodes = {}
wijk_in_stadsdeel = {}


def process_wijk_features(properties):
    wijknaam = properties['WIJKNAAM']
    wijkcode = int(properties['WIJKCODE'])
    stadsdeelcode = int(properties['STADSDEELCODE'])
    buurtcodes[wijknaam] = wijkcode
    wijk_in_stadsdeel[wijkcode] = stadsdeelcode
    return {
        'wijknaam': wijknaam,
        'wijkcode': wijkcode,
        'stadsdeelcode': stadsdeelcode,
    }


def process_wijken():
    processGeoJson('wijken.geojson', process_wijk_features)


# Stadsdelen
########################################################################################################################
stadsdeelcodes = {}


def process_stadsdeel_features(properties):
    stadsdeelnaam = normalize_buurtnaam(properties['STADSDEELNAAM'])
    stadsdeelcode = int(properties['STADSDEELCODE'])
    stadsdeelcodes[stadsdeelnaam] = stadsdeelcode
    return {
        'stadsdeelnaam': stadsdeelnaam,
        'stadsdeelcode': stadsdeelcode
    }


def process_stadsdelen():
    processGeoJson('stadsdeel.geojson', process_stadsdeel_features)


# CSV Data
########################################################################################################################
# 0 Buurt,Totaal aantal Vastgoedobjecten,Totaal aantal Vastgoedobjecten Elektra,Totaal aantal Vastgoedobjecten Gas,
# 4 Totaal CO2 uitstoot (kg),Totaal verbruik Elektra (kWh),Totaal verbruik Gas (m3),Aantal Vastgoedobjecten Particulier,
# 8 Aantal Vastgoedobjecten Elektra Particulier,Aantal Vastgoedobjecten Gas Particulier,CO2 uitstoot Particulier (kg),
# 11 Verbruik Elektra Particulier (kWh),Verbruik Gas Particulier (m3),Gemiddelde CO2 uitstoot Particulier (kg),
# 14 Gemiddelde verbruik Elektra Particulier (kWh),Gemiddelde verbruik Gas Particulier (m3),Aantal Vastgoedobjecten Zakelijk,
# 17 Aantal Vastgoedobjecten Elektra Zakelijk,Aantal Vastgoedobjecten Gas Zakelijk,CO2 uitstoot Zakelijk (kg),
# 20 Verbruik Elektra Zakelijk (kWh),Verbruik Gas Zakelijk (m3),Gemiddelde CO2 uitstoot Zakelijk (kg),
# 23 Gemiddelde verbruik Elektra Zakelijk (kWh),Gemiddelde verbruik Gas Zakelijk (m3),Aantal Vastgoedobjecten Opwek Zonne-energie KV,
# 26 Opwek Zonne-energie KV (kWh),Aantal Vastgoedobjecten Opwek Overig,Opwek Overig (kWh)
csv_data = []
csv_dict = {}
header = None


def process_row(line, year):
    buurtnaam = normalize_buurtnaam(line[0])  # first is string

    if buurtnaam not in buurtcodes:
        raise Exception("Place missing in geo data: %s" % buurtnaam)

    buurtcode = buurtcodes[buurtnaam]
    # if buurtcode not in buurt_info:
    #     buurt_info[buurtcode] = {}

    pdata = []
    for i in range(1, len(line)):
        d = line[i]
        if d == "Geen Data":
            d = -1
        elif d == "Afgeschermd":
            d = -2
        elif ',' in d:
            d = float(d.replace(',', '.'))
        else:
            d = int(d)
        pdata.append(d)

    csv_data.append([buurtcode, year] + pdata)


def process_csvs():
    re_filename_inputdata = re.compile(r'^.*(\d{4})\.csv$')
    for filename in os.listdir(input_data_folder):
        match = re_filename_inputdata.match(filename)
        if match:
            year = match.group(1)
            reader = csv.reader(open(os.path.join(input_data_folder, filename)))

            next_header = next(reader)
            global header
            if header is None:
                header = next_header
            elif next_header != header:
                raise Exception("Header is different!")

            for line in reader:
                process_row(line, year)

    csv_data.sort(key=itemgetter(0, 1))

    for line in csv_data:
        buurt = line[0]
        jaar = line[1]
        datas = line[2:]
        if buurt not in csv_dict:
            csv_dict[buurt] = {}
        csv_dict[buurt][jaar] = datas

    # Write to CSV file
    with open(os.path.join(output_data_folder, 'data.csv'), 'w+', encoding='utf8', newline='\n') as fp:
        writer = csv.writer(fp, delimiter=';', dialect='excel')
        next_header = ['buurtcode', 'Jaar'] + header[1:]
        writer.writerow(next_header)
        for data_line in csv_data:
            writer.writerow(data_line)

    not_linked = [buurt for buurt in buurtcodes.values() if buurt not in csv_dict]
    if len(not_linked) > 0:
        print("Wijk missing in data file: %s" % not_linked)


def combine_geojsons(filename):

    output_geojson_filename = os.path.abspath(os.path.join(output_data_folder, filename))
    subprocess.run([geo2topo_command,
                    'stadsdeel='+os.path.join(output_data_folder, 'stadsdeel.geojson'),
                    'wijken='+os.path.join(output_data_folder, 'wijken.geojson'),
                    'buurten='+os.path.join(output_data_folder, 'buurten.geojson'),
                    '>',
                    output_geojson_filename
    ])
    print('combined file written to %s' % output_geojson_filename)


# Execute
if __name__ == '__main__':
    if not os.path.exists(output_data_folder):
        os.makedirs(output_data_folder)
    process_stadsdelen()
    process_wijken()
    process_buurten()

    process_csvs()

    write_buurten()

    combine_geojsons('combined.topojson')
