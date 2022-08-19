(ns leiningen.ns-sort
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.core.main :as lein-main]
            [clojure.string :as str])
  (:import (java.io File)))


;; =====================================================================================================================
;; Sort :require block
;; =====================================================================================================================
(defn format-ns
  "Format ns block"
  [data]
  (let [sb (StringBuilder. (str "(ns " (second data)))]
    (doseq [item (drop 2 data)]
      (cond
        (= :require (first item)) (do (.append sb \newline)
                                      (.append sb "  (:require ")
                                      (.append sb (second item))
                                      (doseq [req (drop 2 item)]
                                        (.append sb \newline)
                                        (.append sb "            ")
                                        (.append sb req))
                                      (.append sb ")"))

        (= :import (first item)) (do (.append sb \newline)
                                     (.append sb "  (:import ")
                                     (.append sb (second item))
                                     (doseq [req (drop 2 item)]
                                       (.append sb \newline)
                                       (.append sb "           ")
                                       (.append sb req))
                                     (.append sb ")"))
        (string? item) (do (.append sb \newline)
                           (.append sb "  ")
                           (.append sb
                                    (str "\"" (string/escape item {\" "\\\""}) "\"")

                                    ))
        :else (do (.append sb \newline)
                  (.append sb "  ")
                  (.append sb (str item)))))
    (.append sb ")")
    (.toString sb)))


(defn sort-fn [form]
  (if (sequential? form)
    (-> form first str)
    (str form)))


(defn sort-requires
  "Sort requires.
  Priority: 1. project namespaces
            2. 3-td party dependency namespaces"
  [title requires]
  (let [main-ns         (first (string/split (str title) #"\."))
        items           (group-by #(string/starts-with? (sort-fn %) main-ns)
                                  requires)
        sorted-requires (concat
                          (sort-by sort-fn (get items true))
                          (sort-by sort-fn (get items false)))]
    sorted-requires))


(defn make-replace-table
  "Sometimes there can be some hints in the definition, e.g.:
  `(ns ^:dev/once my-app.core
     ...`
   So we need to save such `^:dev/once` hints.
   Constructing replacement table:

   {#`flow-constructor.app`  =>  `^:dev/once flow-constructor.app`
   ...}
  "
  [s]
  (let [table (->> s
                   (re-seq #"(\^:[^\s]+)\s+([^\s]+)")
                   (map (fn [[full _ replacement]]
                          [(re-pattern replacement) full]))
                   (into {}))]
    table))


(defn replace-with-table
  "Apply replacement table to string"
  [s table]
  (reduce (fn [res [pat rep]]
            (str/replace res pat rep))
          s
          table))


;; (update-ns (slurp (io/file "src/leiningen/b.txt")))
(defn update-ns
  "Parse ns string block and update ns block"
  [s]
  (let [replace-table   (make-replace-table s)
        data            (read-string s)
        title           (second data)
        requires        (first (filter #(and (sequential? %)
                                             (= :require (first %)))
                                       data))
        requires-sorted (concat [:require]
                                (sort-requires title (rest requires)))
        sorted-data     (map
                          (fn [item]
                            (if (and (sequential? item)
                                     (= :require (first item)))
                              requires-sorted
                              item))
                          data)]

    ;; if the order is the same, keep old code format
    (if-not (= data sorted-data)
      (replace-with-table (format-ns sorted-data) replace-table)
      s)))


;; =====================================================================================================================
;; Handle files
;; =====================================================================================================================
(defn update-code
  "Update code string"
  [code]
  (let [ns-start (.indexOf code "(ns")
        ns-end   (loop [start ns-start cnt 0]
                   (cond
                     (= \( (.charAt code start)) (recur (inc start) (inc cnt))
                     (= \) (.charAt code start)) (if (zero? (dec cnt))
                                                   (inc start)
                                                   (recur (inc start) (dec cnt)))
                     :else (recur (inc start) cnt)))
        ns-data  (subs code ns-start ns-end)
        prefix   (subs code 0 ns-start)
        postfix  (subs code ns-end)]
    (if (string/includes? ns-data ";")
      code
      (str prefix
           (update-ns ns-data)
           postfix))))


(defn sort-file
  "Read, update and write to file"
  [^File file]
  (try (let [data (slurp file)]
         (spit file (update-code data)))
       (catch Exception e
         (leiningen.core.main/warn (str "Cannot update file: " (.getAbsolutePath file)) e))))


(defn sort-path
  "Filter for only .clj, .cljs, .cljc files"
  [path]
  (let [files (file-seq (io/file path))
        files (filter #(and (or (string/ends-with? (.getAbsolutePath %) ".clj")
                                (string/ends-with? (.getAbsolutePath %) ".cljs")
                                (string/ends-with? (.getAbsolutePath %) ".cljc"))
                            (false? (.isDirectory %)))
                      files)]
    (doseq [file files]
      (sort-file file))))


(defn ns-sort
  "Sort :require block in each namespace found in src folders."
  [project & args]
  (let [source-paths (:source-paths project)]
    (doseq [source-path source-paths]
      (sort-path source-path)))
  (leiningen.core.main/info "Done."))
