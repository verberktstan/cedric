# cedric
Cedric Event DRIven datapersistence Companion

## Run tests

Run clj tests with the :test alias on the clojure cli.
Assumes you have clojure installed.

```clojure -X:test```

Run cljs tests with node, compile test build with npm.
Assumes you have node and npm installed.

```npm install -g shadow-cljs && npx shadow-cljs compile test && node out/node-tests.js```
