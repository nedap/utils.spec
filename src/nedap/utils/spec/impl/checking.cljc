(ns nedap.utils.spec.impl.checking
  (:require
   [clojure.test :refer [is]]))

(def global-state-default-value
  {:nedap.utils.spec.api/perform-checks-unconditionally? false
   :nedap.utils.spec.api/only-warn?                      false})

(defn triple-get [m1 m2 m3 k]
  {:pre [(keyword? k)]}
  (get m1
       k
       (get m2
            k
            (get m3 k))))

(defn determine [{:keys [global-settings
                         context-settings
                         symbol-settings
                         v]}]
  (let [always? (triple-get symbol-settings context-settings global-settings :nedap.utils.spec.api/perform-checks-unconditionally?)
        warn?   (triple-get symbol-settings context-settings global-settings :nedap.utils.spec.api/only-warn?)]
    {:always? always?
     :warn?   warn?}))

(defn append
  {:test (fn []
           (is (= '(1 2 3 4)
                  (append 4 '(1 2 3)))))}
  [x coll]
  {:pre [(sequential? coll)]}
  (->> coll
       (reverse)
       (cons x)
       (reverse)))
