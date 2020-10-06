(ns nedap.utils.spec.impl.check
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.stacktrace])
   [expound.alpha :as expound])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.check])))

(defn print-stack-frames
  "numbers of stack frames to print before raising the exception

  note: only available in clj"
  []
  #?(:cljs 0
     :clj (Long/parseLong (System/getProperty "nedap.utils.spec.print-stack-frames" "0"))))

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
                  (let [ex# (ex-info "Validation failed"
                                     ;; :spec and :faulty-value are legacy keys without strong associated semantics.
                                     ;; However programs may depend strongly on them. Please don't remove them.
                                     {:spec                spec-quoted#
                                      :spec-object         spec#
                                      :quoted-spec         spec-quoted#
                                      :faulty-value        x-quoted#
                                      :faulty-value-object x#
                                      :quoted-faulty-value x-quoted#
                                      :explanation         (~explain spec# x#)})]
                    (when (and (not ~cljs)
                               (pos? (print-stack-frames)))
                      (clojure.stacktrace/print-stack-trace ex# (print-stack-frames)))
                    (throw ex#)))))
          true))))
