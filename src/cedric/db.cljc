(ns cedric.db
  (:require
   #?(:clj [cedric.persistence.mem]
      :cljs [cedric.persistence.mem :refer [Mem]]))
  #?(:clj (:import (cedric.persistence.mem Mem))))

(defn make [impl]
  (case impl
    :mem (Mem. (atom nil))
    (throw (ex-info "Not implemented" {:impl impl}))))

(comment
  (defonce db (make :mem))

  (cedric.persistence/create!
   db
   {:entity-attribute :user/id}
   [{:user/name "Abraham"} {:user/name "Bianca"}])

  (cedric.persistence/query db {:entity? #{[:user/id 0]}})

  (cedric.persistence/query db {:entity-attr? #{:user/id}})

  (cedric.persistence/query db {:entity-val? #{0}})

  (cedric.persistence/destroy!
    db
    {:entity-attribute :user/id}
    [{:user/id 0} {:user/id 1}])

  )
