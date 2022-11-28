(ns nedap.utils.spec.impl.check
  (:require
   #?(:clj [clojure.stacktrace :refer [print-stack-trace]])
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [clojure.string :as str]
   [expound.alpha :as expound])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.check])))

#?(:clj
   (defn print-stack-frames
     "numbers of stack frames to print before raising the exception"
     []
     (Long/parseLong (System/getProperty "nedap.utils.spec.print-stack-frames" "0"))))

#?(:clj
   (defn print-symbol
     "Returns the fully qualified symbol to used to print a spec failure.

     The reporter is to be used as a fallback if the exception may be lost."
     []
     (symbol (System/getProperty "nedap.utils.spec.print-symbol" "clojure.core/println"))))

#?(:clj
   (defmacro check!
     [& args]
     (let [cljs     (-> &env :ns some?)
           valid    (if cljs
                      'cljs.spec.alpha/valid?
                      'clojure.spec.alpha/valid?)
           explain  (if cljs
                      'cljs.spec.alpha/explain-str
                      'clojure.spec.alpha/explain-str)
           ex       (gensym "exception__")
           do-print (print-symbol)]
       `(do
          (doseq [[spec# x# spec-quoted# x-quoted#] ~(mapv (fn [[a b]]
                                                             [a
                                                              b
                                                              (list 'quote a)
                                                              (list 'quote b)])
                                                           (partition 2 args))]
            (when-not (~valid spec# x#)
              (cond-> (expound/expound-str spec# x#)
                      (not= x# x-quoted#)       (str/replace-first "should satisfy"
                                                                   (str "evaluated from\n\n  "
                                                                        (pr-str x-quoted#)
                                                                        "\n\nshould satisfy"))
                      (not= spec# spec-quoted#) (str/replace-first "-------------------------"
                                                                   (str "evaluated from\n\n  "
                                                                        (pr-str spec-quoted#)
                                                                        "\n\n-------------------------"))
                      true                      (~do-print))
              (let [~ex (ex-info "Validation failed"
                                 ;; :spec and :faulty-value are legacy keys without strong associated semantics.
                                 ;; However, programs may depend strongly on them. Please don't remove them.
                                 {:spec                spec-quoted#
                                  :spec-object         spec#
                                  :quoted-spec         spec-quoted#
                                  :faulty-value        x-quoted#
                                  :faulty-value-object x#
                                  :quoted-faulty-value x-quoted#
                                  :explanation         (~explain spec# x#)})]
                ~(when-not cljs
                   `(when (pos? (print-stack-frames))
                      (~do-print (with-out-str (print-stack-trace ~ex (print-stack-frames))))))
                (throw ~ex))))
          true))))
