(ns nedap.utils.spec.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.impl.check]
   #?(:clj [nedap.utils.spec.impl.spec-coerce :refer [when-spec-coerce-available?]]))
  #?(:cljs (:require-macros [nedap.utils.spec.api :refer [check!]]
                            [nedap.utils.spec.impl.spec-coerce :refer [when-spec-coerce-available?]])))

#?(:clj
   (defmacro check!
     "Asserts validity, explaining the cause otherwise. Apt for :pre conditions.

     Reporting can be controlled by setting `nedap.utils.spec.print-symbol`.

  `args` is a sequence of spec-val pairs."
     [& args]
     {:pre [(-> args count even?)]}
     `(nedap.utils.spec.impl.check/check! ~@args)))

(when-spec-coerce-available?
 (defn coerce-map-indicating-invalidity
   "Tries to coerce the map `m` according to spec `spec`.

   If the coercion isn't possible, `::invalid? true` is associated to the map."
   [spec m]
   ;; Very important: specs must be passed as keywords or symbols,
   ;; but never 'inline' as any other kind of objects.
   ;; Else spec-coerce will fail to coerce things.
   {:pre [(check! qualified-ident? spec
                  map? m)]}
   (let [m ((resolve 'spec-coerce.core/coerce) spec m)]
     (cond-> m
             (not (spec/valid? spec m)) (assoc ::invalid? true)))))
