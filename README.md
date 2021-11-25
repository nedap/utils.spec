# nedap.utils.spec [![CircleCI](https://circleci.com/gh/nedap/utils.spec.svg?style=svg&circle-token=650c50f0b2b888dd6c44d3c7b0ea7bcdf80aee14)](https://circleci.com/gh/nedap/utils.spec)

Utilities for [clojure.spec](https://github.com/clojure/spec.alpha).

## Installation

#### Coordinates

```clojure
[com.nedap.staffing-solutions/utils.spec "1.3.1"]
```

> Note that self-hosted ClojureScript (e.g. Lumo) is unsupported at the moment.

#### Production setup

* In JVM Clojure, set [`*assert*`](https://github.com/technomancy/leiningen/blob/9981ae9086a352caf13a42bff4a7e43faa850452/sample.project.clj#L286) to `false`.

* In ClojureScript, set [`:elide-asserts`](https://clojurescript.org/reference/compiler-options#elide-asserts) to `true`.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

In cases where a `check!` spec-failure is printed, but the stacktrace is swallowed, one can debug the origin by setting the java property `nedap.utils.spec.print-stack-frames` to a positive number to print that amount of stackframes before the exception is raised. 
It's generally recommended not to swallow these exceptions in dev/test environments.

## License

Copyright © Nedap

This program and the accompanying materials are made available under the terms of the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0)
