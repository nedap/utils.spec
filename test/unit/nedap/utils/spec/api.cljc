(ns unit.nedap.utils.spec.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.string :as str])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [spec-coerce.core]
   [nedap.utils.spec.api :as sut]))

(def validation-failed #"Validation failed")

(defn speced-f [x y]
  {:pre  [(sut/check! int? x
                      boolean? y)]
   :post [(sut/check! string? %)]}
  (when y
    (str x)))

(deftest check!
  (is (speced-f 42 true))
  (testing ":post"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (speced-f 42 false)))))
  (testing ":pre"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (speced-f :not-an-int true))))
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (speced-f 42 :not-a-boolean)))))
  #?(:clj
     (testing "prints stacktraces honoring 'nedap.utils.spec.print-stack-frames'-property"
       (are [frames expected] (= expected
                                 (-> (with-out-str
                                       (System/setProperty "nedap.utils.spec.print-stack-frames" (str frames))
                                       (try (speced-f 42 :not-a-boolean) (catch Exception _)))
                                     (str/split #"\n at ") ;; first stacktrace starts with ' at '
                                     (nth 1 "")
                                     (str/split-lines)))
         2 ["unit.nedap.utils.spec.api$speced_f.invokeStatic (api.cljc:11)"
            "    unit.nedap.utils.spec.api$speced_f.invoke (api.cljc:10)"]
         1 ["unit.nedap.utils.spec.api$speced_f.invokeStatic (api.cljc:11)"]
         0 [""]))))

(spec/def ::age int?)

(spec/def ::user (spec/keys :req-un [::age]))

(deftest coerce-map-indicating-invalidity
  (are [x y] (= y (sut/coerce-map-indicating-invalidity ::user
                                                        x))
    {:age "1"}   {:age 1}
    {:age "one"} {:age           "one"
                  ::sut/invalid? true}))
