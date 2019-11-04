(ns nedap.utils.spec.impl.check
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [expound.alpha :as expound])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.check])))

(def ^:dynamic *cljs?* false)

(defn throw-validation-error [data]
  (throw (ex-info "Validation failed" data)))

#?(:clj
   (defmacro warn
     [data]
     `(binding [*out* nedap.utils.spec.api/*warn*]
        (println ~data))))

#?(:clj
   (defmacro check!
     {:style/indent 1}
     [throw? & args]
     {:pre [(boolean? throw?)]}
     (let [cljs (-> &env :ns some?)
           valid (if cljs
                   'cljs.spec.alpha/valid?
                   'clojure.spec.alpha/valid?)
           explain (if cljs
                     'cljs.spec.alpha/explain-str
                     'clojure.spec.alpha/explain-str)]
       `(do
          (doseq [[spec# x# spec-quoted# x-quoted#] ~(mapv (fn [[a b]]
                                                             [a
                                                              b
                                                              (list 'quote a)
                                                              (list 'quote b)])
                                                           (partition 2 args))]
            (or (~valid spec# x#)
                (do
                  (cond-> (expound.alpha/expound-str spec# x#)
                    (not= x# x-quoted#)       (clojure.string/replace-first "should satisfy"
                                                                            (str "evaluated from\n\n  "
                                                                                 (pr-str x-quoted#)
                                                                                 "\n\nshould satisfy"))
                    (not= spec# spec-quoted#) (clojure.string/replace-first "-------------------------"
                                                                            (str "evaluated from\n\n  "
                                                                                 (pr-str spec-quoted#)
                                                                                 "\n\n-------------------------"))
                    true                      println)
                  (~(if throw?
                      `throw-validation-error
                      `warn) {:spec         spec-quoted#
                              :faulty-value x-quoted#
                              :explanation  (~explain spec# x#)}))))
          true))))
