(ns unit.nedap.utils.spec.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api :as sut]
   [nedap.utils.spec.impl.checking :as impl.checking])
  #?(:cljs (:require-macros [unit.nedap.utils.spec.api :refer [failure-report]])))

(def validation-failed #"Validation failed")

(deftest check!
  (let [f (fn [x y]
            {:pre  [(sut/check! int? x
                                boolean? y)]
             :post [(sut/check! string? %)]}
            (when y
              (str x)))]
    (is (f 42 true))
    (testing ":post"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f 42 false)))))
    (testing ":pre"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f :not-an-int true))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f 42 :not-a-boolean)))))))

(spec/def ::age int?)

(spec/def ::user (spec/keys :req-un [::age]))

#?(:clj
   (defmacro failure-report []
     (-> (str "test/unit/nedap/utils/spec/"
              (if (-> &env :ns nil?)
                "failure_clj"
                "failure_cljs"))
         slurp)))

(deftest coerce-map-indicating-invalidity
  (are [x y] (= y (sut/coerce-map-indicating-invalidity ::user
                                                        x))
    {:age "1"}   {:age 1}
    {:age "one"} {:age           "one"
                  ::sut/invalid? true}))

(deftest checking
  #?(:clj
     (testing "macroexpands to reasonable-looking, known-good forms"
       (are [input expected] (= expected
                                (macroexpand-1 (list 'nedap.utils.spec.api/checking
                                                     input
                                                     'string? 'a
                                                     'string? 'b)))

         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      false}
         '(do
            (clojure.core/assert (nedap.utils.spec.impl.check/check! true string? a))
            (clojure.core/assert (nedap.utils.spec.impl.check/check! true string? b))
            true)

         {:nedap.utils.spec.api/perform-checks-unconditionally? true
          :nedap.utils.spec.api/only-warn?                      false}
         '(do
            (nedap.utils.spec.impl.check/check! true string? a)
            (nedap.utils.spec.impl.check/check! true string? b)
            true)

         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      true}
         '(do
            (nedap.utils.spec.impl.check/check! false string? a)
            (nedap.utils.spec.impl.check/check! false string? b)
            true))))

  (testing "Basic functionality"
    (are [options expectations] (let [f (fn [x y]
                                          {:pre  [(sut/checking options
                                                    int? x
                                                    boolean? y)]
                                           :post [(sut/checking options
                                                    string? %)]}
                                          (when y
                                            (str x)))]
                                  (is (f 42 true))
                                  (case expectations
                                    :assertions-thrown
                                    (do
                                      (testing ":post"
                                        (is (thrown-with-msg? #?(:clj Exception
                                                                 :cljs js/Error) validation-failed (with-out-str
                                                                                                     (f 42 false)))))
                                      (testing ":pre"
                                        (is (thrown-with-msg? #?(:clj Exception
                                                                 :cljs js/Error) validation-failed (with-out-str
                                                                                                     (f :not-an-int true))))
                                        (is (thrown-with-msg? #?(:clj Exception
                                                                 :cljs js/Error) validation-failed (with-out-str
                                                                                                     (f 42 :not-a-boolean))))))
                                    :assertions-not-thrown
                                    (let [o #?(:clj  (java.io.StringWriter.)
                                               :cljs (goog.string/StringBuffer.))]
                                      (binding [sut/*warn* o
                                                *out* o
                                                #?(:cljs cljs.core/*print-fn*) #?(:cljs (fn [x]
                                                                                          (-> o (.append x))))]
                                        (f 42 false)
                                        (f :not-an-int true)
                                        (f 42 :not-a-boolean)
                                        (is (= (failure-report)
                                               (str o)))))))
      {:nedap.utils.spec.api/perform-checks-unconditionally? false
       :nedap.utils.spec.api/only-warn?                      false}
      :assertions-thrown

      {:nedap.utils.spec.api/perform-checks-unconditionally? true
       :nedap.utils.spec.api/only-warn?                      false}
      :assertions-thrown

      {:nedap.utils.spec.api/perform-checks-unconditionally? false
       :nedap.utils.spec.api/only-warn?                      true}
      :assertions-not-thrown))

  #?(:clj
     (testing "Priority among levels"
       (are [global-state options v expected] (= expected
                                                 (let [_ (reset! sut/settings global-state)
                                                       ret (macroexpand-1 (list 'nedap.utils.spec.api/checking
                                                                                options
                                                                                'boolean?
                                                                                v))]
                                                   (reset! sut/settings impl.checking/global-state-default-value)
                                                   ret))

         ;; default behavior
         impl.checking/global-state-default-value
         {}
         'thing
         '(do
            (clojure.core/assert
             (nedap.utils.spec.impl.check/check! true boolean? thing))
            true)

         ;; metadata
         impl.checking/global-state-default-value
         {}
         (with-meta 'thing {:nedap.utils.spec.api/perform-checks-unconditionally? true})
         '(do
            (nedap.utils.spec.impl.check/check! true boolean? thing)
            true)

         ;; metadata
         impl.checking/global-state-default-value
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      false}
         (with-meta 'thing {:nedap.utils.spec.api/only-warn? true})
         '(do
            (nedap.utils.spec.impl.check/check! false boolean? thing)
            true)

         ;; options
         impl.checking/global-state-default-value
         {:nedap.utils.spec.api/perform-checks-unconditionally? true
          :nedap.utils.spec.api/only-warn?                      false}
         'thing
         '(do
            (nedap.utils.spec.impl.check/check! true boolean? thing)
            true)

         ;; tests that metadata wins over options
         impl.checking/global-state-default-value
         {:nedap.utils.spec.api/perform-checks-unconditionally? true
          :nedap.utils.spec.api/only-warn?                      false}
         (with-meta 'thing {:nedap.utils.spec.api/perform-checks-unconditionally? false})
         '(do
            (clojure.core/assert
             (nedap.utils.spec.impl.check/check! true boolean? thing))
            true)

         ;; tests that metadata wins over options
         impl.checking/global-state-default-value
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      true}
         (with-meta 'thing {:nedap.utils.spec.api/only-warn? false})
         '(do
            (clojure.core/assert
             (nedap.utils.spec.impl.check/check! true boolean? thing))
            true)

         ;; global settings
         {:nedap.utils.spec.api/perform-checks-unconditionally? true
          :nedap.utils.spec.api/only-warn?                      false}
         {}
         'thing
         '(do
            (nedap.utils.spec.impl.check/check! true boolean? thing)
            true)

         ;; global settings
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      true}
         {}
         'thing
         '(do
            (nedap.utils.spec.impl.check/check! false boolean? thing)
            true)

         ;; tests that options win over settings
         {:nedap.utils.spec.api/perform-checks-unconditionally? true
          :nedap.utils.spec.api/only-warn?                      false}
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      false}
         'thing
         '(do
            (clojure.core/assert
             (nedap.utils.spec.impl.check/check! true boolean? thing))
            true)

         ;; tests that options win over settings
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      true}
         {:nedap.utils.spec.api/perform-checks-unconditionally? false
          :nedap.utils.spec.api/only-warn?                      false}
         'thing
         '(do
            (clojure.core/assert
             (nedap.utils.spec.impl.check/check! true boolean? thing))
            true)))))
