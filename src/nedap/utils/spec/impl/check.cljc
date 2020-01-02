(ns nedap.utils.spec.impl.check
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [expound.alpha :as expound])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.check])))

(def ^:dynamic *cljs?* false)

#?(:clj
   (defmacro check!
     [& args]
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
                  (let [should-print-literally?# (keyword? spec#)]
                    (throw (ex-info "Validation failed" (cond-> {:spec         (if should-print-literally?#
                                                                                 spec#
                                                                                 spec-quoted#)
                                                                 :faulty-value x-quoted#
                                                                 :explanation  (~explain spec# x#)}
                                                          should-print-literally?# (assoc :spec-quoted spec-quoted#))))))))
          true))))
