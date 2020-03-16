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

(def sample-spec-object (spec/spec #{1 2 3}))

(deftest reported-ex-data

  (are [passed-spec expected] (testing [passed-spec expected]
                                (try
                                  (with-out-str
                                    (backed-by-dynamic-spec-checking passed-spec "NaN"))
                                  (is false)
                                  (catch ExceptionInfo e
                                    (let [actual (-> e ex-data (select-keys [:spec-object
                                                                             :quoted-spec
                                                                             :faulty-value-object
                                                                             :quoted-faulty-value]))]
                                      (is (= expected actual)))))
                                true)
    ::number           {:spec-object         ::number
                        :quoted-spec         'spec
                        :faulty-value-object "NaN"
                        :quoted-faulty-value 'x}
    number?            {:spec-object         number?
                        :quoted-spec         'spec
                        :faulty-value-object "NaN"
                        :quoted-faulty-value 'x}
    #{1 2 3}           {:spec-object         #{1 2 3}
                        :quoted-spec         'spec
                        :faulty-value-object "NaN"
                        :quoted-faulty-value 'x}
    sample-spec-object {:spec-object         sample-spec-object
                        :quoted-spec         'spec
                        :faulty-value-object "NaN"
                        :quoted-faulty-value 'x}))
