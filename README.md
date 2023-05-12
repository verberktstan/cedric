# cedric
Cedric Event DRIven datapersistence Companion
## Usage

Require the db and persistence namespaces:

```
(ns your.ns
  (:require
    [cedric.db :as db]
    [cedric.persistence :as persistence]))
```

Creating a in-memory database:

```
(defonce db (db/make :mem))
```

Create items:

```
(persistence/create!
  db
  {:entity-attribute :user/id}
  [{:user/name "Abraham"} {:user/name "Bianca"}])
```

Query items:

```
(persistence/query db {:entity? #{[:user/id 0]}})
(persistence/query db {:entity-attr? #{:user/id}})
(persistence/query db {:entity-val? #{1}})
```

Destroy items:

```
(persistence/destroy! db {:entity-attribute :user/id} [{:user/id 0}])
```

## Run tests

Run clj tests with the :test alias on the clojure cli.
Assumes you have clojure installed.

```clojure -X:test```

Run cljs tests with node, compile test build with npm.
Assumes you have node and npm installed.

```npm i shadow-cljs && npx shadow-cljs compile test && node out/node-tests.js```
