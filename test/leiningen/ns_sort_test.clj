(ns leiningen.ns-sort-test
  (:require [clojure.test :refer :all]
            [leiningen.ns-sort :as ns-sort]))


(deftest format-ns-test
  (is (= (ns-sort/format-ns '(ns
                               leiningen.ns-sort
                               (:require [clojure.java.io :as io]
                                         [clojure.string :as string])
                               (:import (java.io File))))
         "(ns leiningen.ns-sort
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.io File)))")))


(deftest sort-requires-test
  (is (= (ns-sort/sort-requires 'my-server '[[schema.core :as s]
                                             [compojure.api.sweet :refer [GET POST]]
                                             [ring.util.http-response :refer [content-type ok]]
                                             [my-server.db.api.anomalies :as db-anomalies]
                                             [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
                                             [ring.swagger.json-schema :refer [describe]]])

         '[[my-server.db.api.anomalies :as db-anomalies]
           [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
           [compojure.api.sweet :refer [GET POST]]
           [ring.swagger.json-schema :refer [describe]]
           [ring.util.http-response :refer [content-type ok]]
           [schema.core :as s]])))


(deftest update-ns-test
  (is (= (ns-sort/update-ns "(ns my-server
  (:require [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
            [compojure.api.sweet :refer [GET POST]]
            [ring.util.http-response :refer [content-type ok]]
            [ring.swagger.json-schema :refer [describe]]
            [my-server.db.api.anomalies :as db-anomalies]
            [schema.core :as s]))")

         "(ns my-server
  (:require [my-server.db.api.anomalies :as db-anomalies]
            [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
            [compojure.api.sweet :refer [GET POST]]
            [ring.swagger.json-schema :refer [describe]]
            [ring.util.http-response :refer [content-type ok]]
            [schema.core :as s]))"))

  ;; testing hints preservation
  (let [ns-with-hints "(ns ^:dev/once flow-constructor.app\n  (:require [flow-constructor.core :as core]\n            [cljs.spec.alpha :as s]\n            [expound.alpha :as expound]\n            [devtools.core :as devtools]))"
        ns-sorted     "(ns ^:dev/once flow-constructor.app\n  (:require [flow-constructor.core :as core]\n            [cljs.spec.alpha :as s]\n            [devtools.core :as devtools]\n            [expound.alpha :as expound]))"]
    (is (= (ns-sort/update-ns ns-with-hints) ns-sorted))))


(deftest update-code-test
  (is (= (ns-sort/update-code (slurp "test/data/unsorted.clj-test"))
         (slurp "test/data/sorted.clj-test"))))