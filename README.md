![LDBC_LOGO](https://raw.githubusercontent.com/wiki/ldbc/ldbc_snb_datagen/images/ldbc-logo.png)

LDBC SNB Data Generator
----------------------

[![Build Status](https://circleci.com/gh/ldbc/ldbc_snb_datagen.svg?style=svg)](https://circleci.com/gh/ldbc/ldbc_snb_datagen)

:scroll: If you wish to cite the LDBC SNB, please refer to the [documentation repository](https://github.com/ldbc/ldbc_snb_docs#how-to-cite-ldbc-benchmarks).

:warning: There are two versions of this repository:
* To generate data for Interactive SF1-1000, use the non-default [`stable` branch](https://github.com/ldbc/ldbc_snb_datagen/tree/stable) which uses Hadoop.
* For the Interactive workload's larger data sets (SF3000+) and for the BI workload, use the [`dev` branch](https://github.com/ldbc/ldbc_snb_datagen/) which uses Spark. This is an experimental implementation. :warning: **Parameter generation is currently disabled for this branch and will be back in Dec 2020.**

The LDBC SNB Data Generator (Datagen) is the responsible of providing the datasets used by all the LDBC benchmarks. This data generator is designed to produce directed labeled graphs that mimic the characteristics of those graphs of real data. A detailed description of the schema produced by Datagen, as well as the format of the output files, can be found in the latest version of official [LDBC SNB specification document](https://github.com/ldbc/ldbc_snb_docs).

[Generated small data sets](https://ldbc.github.io/ldbc_snb_datagen/) are deployed by the CI.

`ldbc_snb_datagen` is part of the [LDBC project](http://www.ldbcouncil.org/).
`ldbc_snb_datagen` is GPLv3 licensed, to see detailed information about this license read the `LICENSE.txt` file.

## Quick start

### Configuration

Initialize the `params.ini` file as needed. For example, to generate the basic CSV files, issue:

```bash
cp params-csv-basic.ini params.ini
```

The following options are available:

* `generator.scaleFactor`: determines the data set size, options: `0.003` (for tests), `0.1`, `0.3`, `1`, `3`, `10`, `30`, ..., `1000`. Larger ones coming soon (Dec 2020/Jan 2021)
* `serializer.format`, options:
  * `CsvBasic`
  * `CsvMergeForeign`
  * `CsvComposite`
  * `CsvCompositeMergeForeign`
  * RDF/Turtle serializers are currently not supported.
* `generator.mode`, options:
  * `interactive`: used for the benchmarks
  * `rawdata`: used for debugging, includes explicit deletion date timestamps, edge weights used to select deletions, etc. This mode is only compatible with the `CsvBasic` serializer.

### Build the JAR
Make sure you hava Java 8 (JDK) installed and the `$JAVA_HOME` environment variable points to its location. You might find [SDKMAN](https://sdkman.io/) useful.
To assemble the JAR file, run:

```bash
tools/build.sh
```

### Install tools
Some of the build utilities are written in Python. To use them, you have to create a Python virtual environment
and install the dependencies.

E.g with pyenv
```bash
pyenv virtualenv 3.7.7 ldbc_datagen_tools
echo "3.7.7/envs/ldbc_datagen_tools" > .python-version
pip install -U pip -r tools/requirements.txt
```
### Running locally

Download and extract Spark 2.4.x:

```bash
curl https://downloads.apache.org/spark/spark-2.4.7/spark-2.4.7-bin-hadoop2.7.tgz | sudo tar -xz -C /opt/
export SPARK_HOME="/opt/spark-2.4.7-bin-hadoop2.7"
export PATH="$SPARK_HOME/bin":"$PATH"
```

Run the benchmarks locally with the following script:

```bash
tools/run.py ./target/ldbc_snb_datagen-0.4.0-SNAPSHOT-jar-with-dependencies.jar params.ini
```

There are some configuration options like setting parallelism or number of cores, try `--help`.

### Docker image

SNB Datagen images are available via [Docker Hub](https://hub.docker.com/r/ldbc/datagen/) (currently outdated).

Alternatively, the image can be built with the provided Dockerfile. To build, execute the following command from the repository directory:

```bash
tools/docker-build.sh
```

See [Build the JAR](#build-the-jar) to build the library. Then, run the following:

```bash
tools/docker-run.sh
```

This produce its output to the `out/social_network` directory

### Elastic MapReduce

We provide scripts to run Datagen on AWS EMR. See the README in the [`tools/emr`](tools/emr) directory for details.

## Parameter generation

The parameter generator is currently being reworked (see [relevant issue](https://github.com/ldbc/ldbc_snb_datagen/issues/83)) and no parameters are generated by default.
However, the legacy parameter generator is still available. To use it, run the following commands:

```bash
mkdir substitution_parameters
# for Interactive
paramgenerator/generateparams.py out/build/ substitution_parameters
# for BI
paramgenerator/generateparamsbi.py out/build/ substitution_parameters
```

## Larger scale factors

The scale factors SF3k are currently being fine-tuned, both regarding optimizing the generator and also for tuning the distributions.

## Graph schema

The graph schema is as follows:

![](https://raw.githubusercontent.com/ldbc/ldbc_snb_docs/dev/figures/schema-comfortable.png)

## Troubleshooting

* When running the tests, they might throw a `java.net.UnknownHostException: your_hostname: your_hostname: Name or service not known` coming from `org.apache.hadoop.mapreduce.JobSubmitter.submitJobInternal`. The solution is to add an entry of your machine's hostname to the `/etc/hosts` file: `127.0.1.1 your_hostname`.
* If you are using Docker and Spark runs out of space, make sure that there is enough free space in `/tmp` (or override it using the `TEMP_DIR` variable in the Dockerfile and rebuild the image) and also that Docker itself has enough space to store its containers.
To move the location of the Docker containers to a larger disk, stop Docker, edit (or create) the `/etc/docker/daemon.json` file and add `{ "data-root": "/path/to/new/docker/data/dir" }`, then sync the old folder if needed, and restart Docker. (See [more detailed instructions](https://www.guguweb.com/2019/02/07/how-to-move-docker-data-directory-to-another-location-on-ubuntu/)).
