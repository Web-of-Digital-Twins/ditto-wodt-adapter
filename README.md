# Ditto Web of Digital Twins Adapter

![Release](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/actions/workflows/build-and-deploy.yml/badge.svg?style=plastic)
[![License: Apache License](https://img.shields.io/badge/License-Apache_License_2.0-yellow.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![Version](https://img.shields.io/github/v/release/Web-of-Digital-Twins/ditto-wodt-adapter?style=plastic)

A simple middleware prototype that allows to expose [Eclipse Ditto](https://eclipse.dev/ditto/) Digital Twins (or Things) as HWoDT-compliant DTs.

## Usage
### 1. Start Eclipse Ditto
First of all you need to start Eclipse Ditto, for example via Docker Compose, and create the Ditto Thing you want to expose as a WoDT Digital Twin.

### 2. Configuration
Before starting the adapter you need to create a `.yml` file that contains the ontology mappings. You can find an example in [src/main/resources/ontology_example.yml](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/blob/main/src/main/resources/ontology_example.yaml).
Alternatively, the configuration of the _domain tags_ can be equally achieved by setting them directly when creating the Ditto Thing in Eclipse Ditto, specifying a _Thing Model_ like the one in [this example](https://gist.github.com/AndreaGiulianelli/4047cacbb5a2bf1691ce11959ea94eb6).

Then, to start the adapter, you need to specify the following environment variables:
- `DITTO_URL`: the URL of the Eclipse Ditto instance.
- `DITTO_OBSERVATION_ENDPOINT`: the WebSocket endpoint of the Eclipse Ditto instance.
- `DITTO_USERNAME`: the username of a valid user on the Eclipse Ditto instance.
- `DITTO_PASSWORD`: the corresponding password of the user on the Eclipse Ditto instance.
- `DITTO_THING_ID`: the Eclipse Ditto Thing ID to expose as a WoDT DT.
- `YAML_ONTOLOGY_PATH`: the path of the ontology configuration.
- `PLATFORM_URI`: [**optional**] the WoDT Platform URI to automatically register in, if present. 
- `PHYSICAL_ASSET_ID`: the ID of the corresponding Physical Asset.
- `DIGITAL_TWIN_URI`: the Digital Twin URI that will be exposed (it includes also the final exposed port).
- `DIGITAL_TWIN_EXPOSED_PORT`: the port to actually expose for the adapter.
- `DIGITAL_TWIN_VERSION`: the version of the Digital Twin to expose.

### 3. Adapter start
You can start the adapter using the provided docker image. To start it via a docker container you need to:
1. Provide a `.env` file with all the environment variables described above
2. Run the container with the command:
   ```bash
   docker run ghcr.io/web-of-digital-twins/ditto-wodt-adapter:<version>
   ```
   1. Provide a port mapping to the `DIGITAL_TWIN_EXPOSED_PORT`.
   2. Create a volume to pass the ontology file. 
   3. If you want to pass an environment file whose name is different from `.env` use the `--env-file <name>` parameter.
    
Alternatively, you can obviously start the adapter directly via Gradle.