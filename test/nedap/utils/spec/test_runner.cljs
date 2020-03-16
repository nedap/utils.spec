(ns nedap.utils.spec.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [nedap.utils.test.api :refer-macros [run-tests]]
   [unit.nedap.utils.spec.api]
   [unit.nedap.utils.spec.api.check]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.spec.api
   'unit.nedap.utils.spec.api.check))

(set! *main-cli-fn* -main)
