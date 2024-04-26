---
description: >-
  moclojer is written in the Clojure programming language and is provided for
  distribution in some formats.
---

# Run it mocker server

## Environment vars

* `XDG_CONFIG_HOME`: to fetch the default moclojer configuration file *(default: `$HOME/.config`)*
* `MOCLOJER_ENV`: define the environment where the moclojer is running, we accept `prod` or `dev` *(default: `prod`)*

## Clojure

Has an alias created in `edn` with the name _“run”_

```shell
CONFIG=moclojer.yml clojure -X:run
```

> moclojer uses `XDG_CONFIG_HOME` to fetch the default moclojer configuration file, if you want to set a different directory you must use the `-c` or `CONFIG` environment variable

## Binary

You don’t have the binary file yet? [Download it here](https://github.com/avelino/moclojer/releases/latest). The moclojer is distributed as follows:

* Binary format: `moclojer_<OS>` - _in binary format (you don’t need anything additional on your operating system to run it)_
  * Linux `moclojer_Linux`
  * macOS `moclojer_macOS`
* `moclojer.jar` - _in java format (you need to install java to run it)_
* Docker image - _in docker format (you need to install docker to run it)_

After creating the file you must run moclojer passing the configuration file by the `CONFIG` environment variable:

```shell
CONFIG=moclojer.yml moclojer # binary filename
```

## **jar file**

```shell
CONFIG=moclojer.yml java -jar moclojer.jar
```

## **Docker**

```shell
docker pull ghcr.io/avelino/moclojer:latest
docker run -it \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/avelino/moclojer:latest
```

to use the `edn` format, you must pass the following parameters to docker:

`-e CONFIG=moclojer.edn -v $(pwd)/moclojer.edn:/app/moclojer.edn`
