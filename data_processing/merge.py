import csv
import os
import json
from operator import itemgetter
import re

# input
input_data_folder = os.path.join(os.path.dirname(__file__), 'data')
filename_buurten = os.path.join(input_data_folder, 'buurten.geojson')
re_filename_inputdata = re.compile(r'^.+(\d{4}).*.csv$')
# output
output_data_folder = os.path.abspath(os.path.join(os.path.dirname(os.path.dirname(__file__)), 'pdata'))
if not os.path.exists(output_data_folder):
    os.makedirs(output_data_folder)
filename_output_data = os.path.join(output_data_folder, 'data.csv')
filename_output_wijken = os.path.join(output_data_folder, 'wijken.json')
filename_output_buurten = os.path.join(output_data_folder, 'buurten.geojson')

wijken_typos = {
    'Burgen en Hosten': 'Burgen en Horsten',
    'Van Stolkprk./Schev.Bosjes': 'Van Stolkpark en Scheveningse Bosjes',
    'Kraayenstein': 'Kraayenstein en Vroondaal',
    'Tedingerbuurt': 'Tedingerbroek',
    'Oostenduinen': 'Oostduinen',
    'Parkbuurt oosteinde': 'Parkbuurt Oosteinde',
}
def normalize_wijknaam(wijknaam: str):
    if wijknaam in wijken_typos:
        return wijken_typos[wijknaam]
    if wijknaam.endswith('-zuid'):
        return wijknaam[0:-5] + '-Zuid'
    if wijknaam.endswith('-noord'):
        return wijknaam[0:-6] + '-Noord'
    if wijknaam.endswith('-oost'):
        return wijknaam[0:-5] + '-Oost'
    if wijknaam.endswith('-west'):
        return wijknaam[0:-5] + '-West'
    if wijknaam.endswith('-midden'):
        return wijknaam[0:-7] + '-Midden'
    if wijknaam.endswith('e.o.'):
        return wijknaam[0:-4] + 'en omgeving'
    return wijknaam

## Wijken geo data
wijken = json.load(open(filename_buurten))
wijkcodes = {}
for f in wijken['features']:
    wijknaam = normalize_wijknaam(f['properties']['BUURTNAAM'])
    f['properties']['BUURTNAAM'] = wijknaam
    wijkcodes[wijknaam] = f['properties']['WIJKBUURTCODE']
json.dump(wijken, open(filename_output_buurten, mode='w+'), indent=1)

## Energy data
wijknamen_found = []
data = []
prev_header = None

# 0 Buurt,Totaal aantal Vastgoedobjecten,Totaal aantal Vastgoedobjecten Elektra,Totaal aantal Vastgoedobjecten Gas,
# 4 Totaal CO2 uitstoot (kg),Totaal verbruik Elektra (kWh),Totaal verbruik Gas (m3),Aantal Vastgoedobjecten Particulier,
# 8 Aantal Vastgoedobjecten Elektra Particulier,Aantal Vastgoedobjecten Gas Particulier,CO2 uitstoot Particulier (kg),
# 11 Verbruik Elektra Particulier (kWh),Verbruik Gas Particulier (m3),Gemiddelde CO2 uitstoot Particulier (kg),
# 14 Gemiddelde verbruik Elektra Particulier (kWh),Gemiddelde verbruik Gas Particulier (m3),Aantal Vastgoedobjecten Zakelijk,
# 17 Aantal Vastgoedobjecten Elektra Zakelijk,Aantal Vastgoedobjecten Gas Zakelijk,CO2 uitstoot Zakelijk (kg),
# 20 Verbruik Elektra Zakelijk (kWh),Verbruik Gas Zakelijk (m3),Gemiddelde CO2 uitstoot Zakelijk (kg),
# 23 Gemiddelde verbruik Elektra Zakelijk (kWh),Gemiddelde verbruik Gas Zakelijk (m3),Aantal Vastgoedobjecten Opwek Zonne-energie KV,
# 26 Opwek Zonne-energie KV (kWh),Aantal Vastgoedobjecten Opwek Overig,Opwek Overig (kWh)

for filename in os.listdir(input_data_folder):
    match = re_filename_inputdata.match(filename)
    if match:
        year = match.group(1)
        reader = csv.reader(open(os.path.join(input_data_folder, filename)))

        header = next(reader)
        if prev_header is None:
            prev_header = header
        elif header != prev_header:
            raise Exception("Header is different!")

        for line in reader:
            pdata = []
            placename = normalize_wijknaam(line[0]) # first is string
            if placename not in wijkcodes:
                print("Place missing in geo data: %s" % placename)
                placecode = -1
            else:
                wijknamen_found.append(placename)
                placecode = wijkcodes[placename]

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

            data.append([placecode, placename, year] + pdata)

data.sort(key=itemgetter(0, 1))

with open(filename_output_data, 'w+', encoding='utf8', newline='\n') as fp:
    writer = csv.writer(fp, delimiter=';', dialect='excel')
    header = ['wijkbuurtcode'] + prev_header[:1] + ['Jaar'] + prev_header[1:]
    writer.writerow(header)
    for data_line in data:
        writer.writerow(data_line)


not_linked = [w for w in wijkcodes if w not in wijknamen_found]
if len(not_linked) > 0:
    print("Geodata place missing in data: %s" % not_linked)
wijkcodes_to_name = {c: w for w,c in wijkcodes.items()}
with open(filename_output_wijken, mode='w+') as fp:
    json.dump(wijkcodes_to_name, fp, indent=1)
