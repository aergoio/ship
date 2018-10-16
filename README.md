[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![LoC](https://tokei.rs/b1/github/aergoio/ship)](https://github.com/aergoio/ship)
[![Travis_ci](https://travis-ci.org/aergoio/ship.svg?branch=master)](https://travis-ci.org/aergoio/ship)
[![codecov.io](http://codecov.io/github/aergoio/ship/coverage.svg?branch=master)](http://codecov.io/github/aergoio/ship?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/b863c5d4835cd5c4c528/maintainability)](https://codeclimate.com/github/aergoio/ship/maintainability)

# Introduction
The ship makes your development rapid. It suggests the cycle to import, refer, and upload packages.

The ship provides the next:
* Initialize project
* Install package from github.com
* Build project
* Unit test project
* Publish to local repository
* Incremental build
* Deploy to aergo chain
* Runner to execute or query contract

## Modules
The repository contains next:
* core
* web

# Project structure
<pre>
$USER_HOME/
  +.aergo_modules/

$PROJECT_HOME/
  | + xxxx/
  + aergo.json
</pre>
* aergo.json - project description file
* .aergo_modules - modules to be downloaded by ship   

## Command
* ship init
* ship install aergoio/athena-343
* ship build
  * ship build --watch
  * ship build --watch --port 8080
* ship test
* ship publish

# Configuration(aergo.json)
* name: project name
* source: source file to build
* target: result when you type 'ship build'
* dependencies: package dependencies
* tests: test cases to run
* endpoint: endpoint to deploy or run (Default value is "localhost:7845")

## Examples
```json
{
  "name": "bylee/examples",
  "source": "src/main/lua/main.lua",
  "target": "app.lua",
  "dependencies": ["aergoio/athena-343"],
  "test": ["src/test/lua/test-main.lua"],
  "endpoint": "remotehost:3030"
}
```

# Integration
TBD

# Build
## Prerequisites
* [JDK 8](http://openjdk.java.net/)
* [NPM](https://www.npmjs.com/)

## Clone
```console
$ git clone https://github.com/aergoio/heraj.git
```

## Build and package
* Clean
```console
$ ./build.sh clean
```

* Install deps
```console
$ ./build.sh deps
```

* Create ship web ui
```console
$ ./build.sh npm
```

* Run gradle
```console
$ ./build.sh gradle
```

* Assemble distributions
```console
$ ./build.sh assemble
```

# Test
## Kind of test
### Unit test
They are classes with 'Test' suffix.

### Integration test
They are classes with 'IT' suffix meaning integration test.

### Benchmark test
They are classes with 'Benchmark' suffix, which using jmh.

## Run tests
```console
$ ./build.sh test
```

# Documentation
We provides next in https://aergoio.github.io/ship
* JavaDoc
* Test Coverage

## How to build documents
```console
$ ./build.sh docs
```

# Contribution
