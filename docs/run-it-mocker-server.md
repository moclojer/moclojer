---
description: >-
  moclojer is written in the Clojure programming language and is provided for
  distribution in some formats.
---

# Run it mocker server

### Clojure

Has an alias created in `edn` with the name _"run"_

```shell
CONFIG=moclojer.yml clojure -X:run
```

### Binary

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

### **jar file**

```shell
CONFIG=moclojer.yml java -jar moclojer.jar
```

### **Docker**

```shell
docker pull ghcr.io/avelino/moclojer:latest
docker run -it \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/avelino/moclojer:latest
```

to use the `edn` format, you must pass the following parameters to docker:

`-e CONFIG=moclojer.edn -v $(pwd)/moclojer.edn:/app/moclojer.edn`
