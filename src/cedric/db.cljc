(ns cedric.db
  (:require
   #?(:clj [cedric.persistence.mem]
      :cljs [cedric.persistence.mem :refer [Mem]]))
  #?(:clj (:import (cedric.persistence.mem Mem))))

(defn make [impl]
  (case impl
    :mem (Mem. (atom nil))
    (throw (ex-info "Not implemented" {:impl impl}))))
