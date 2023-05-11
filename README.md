# cedric
Cedric Event DRIven datapersistence Companion

Assumes you have clojure, yarn and node installed.

## Run tests
Run clj tests with the :test alias on the clojure cli.
```clojure -X:test```

Run cljs tests with node, compile test build with yarn.
```yarn shadow-cljs compile test && node out/node-tests.js```
