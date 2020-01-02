(ns unit.nedap.utils.spec.api.check
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api :as sut])
  #?(:clj (:import (clojure.lang ExceptionInfo))))

(spec/def ::number number?)

(defn backed-by-dynamic-spec-checking
  "A function that performs an `assert` using a dynamic spec.

  This challenges our `check!` helper: it shouldn't report the `'spec` symbol as the relevant spec,
  but the underlying spec value instead."
  [spec x]
  (assert (sut/check! spec x))
  x)

(deftest reporting
  (are [desc passed-spec reported-spec reported-quoted-value reported-raw-value]
      (testing desc
        (try
          (with-out-str
            (backed-by-dynamic-spec-checking passed-spec "NaN"))
          (is false)
          (catch ExceptionInfo e
            (let [{:keys [spec spec-quoted spec-raw]} (-> e ex-data)]
              (is (= reported-spec
                     spec))
              (is (= reported-quoted-value
                     spec-quoted))
              (is (= reported-raw-value
                     spec-raw)))))
        true)
    "A `::number` spec is reported as a `::number` (literal keyword),
    `'spec` is reported as `:spec-quoted`.
     and nothing is reported as `:spec-raw`"
    ::number ::number 'spec nil

    "A `#{1 2 3}` spec is reported as `#{1 2 3}` (literal set),
    `'spec` is reported as `:spec-quoted`.
     and nothing is reported as `:spec-raw`"
    #{1 2 3} #{1 2 3} 'spec nil

    ;; note that the following is not ideal,
    ;; but we cannot possibly get the clean `'number?` symbol out of the `number?` function literal
    "A `number?` spec is reported as `'spec` (quoted symbol),
     nothing is reported as `:spec-quoted`,
     and a function literal is reported as `:spec-raw`"
    number?  'spec    nil   number?))
