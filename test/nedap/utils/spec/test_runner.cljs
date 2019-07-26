(ns nedap.utils.spec.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [run-tests]]
   [unit.nedap.utils.spec.api]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.spec.api))

(set! *main-cli-fn* -main)
