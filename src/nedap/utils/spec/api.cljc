(ns nedap.utils.spec.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.impl.check]
   [nedap.utils.spec.impl.checking :as impl.checking]
   [spec-coerce.core :as coerce])
  #?(:cljs (:require-macros [nedap.utils.spec.api :refer [check!]])))

#?(:clj
   (defmacro check!
     "Asserts validity, explaining the cause otherwise. Apt for :pre conditions.

  `args` is a sequence of spec-val pairs."
     [& args]
     {:pre [(-> args count even?)]}
     `(nedap.utils.spec.impl.check/check! true ~@args)))

(defn coerce-map-indicating-invalidity
  "Tries to coerce the map `m` according to spec `spec`.

  If the coercion isn't possible, `::invalid? true` is associated to the map."
  [spec m]
  ;; Very important: specs must be passed as keywords or symbols,
  ;; but never 'inline' as any other kind of objects.
  ;; Else spec-coerce will fail to coerce things.
  {:pre [(check! qualified-ident? spec
                 map?             m)]}
  (let [m (coerce/coerce spec m)]
    (cond-> m
      (not (spec/valid? spec m)) (assoc ::invalid? true))))

(spec/def ::perform-checks-unconditionally? boolean?)

(spec/def ::only-warn? boolean?)

(spec/def ::global-settings (spec/and (spec/keys :req [::perform-checks-unconditionally?
                                                       ::only-warn?])
                                      (fn [m]
                                        (->> m
                                             keys
                                             sort
                                             (= (list ::only-warn?
                                                      ::perform-checks-unconditionally?))))))

(spec/def ::context-options (spec/and (spec/keys :opt [::perform-checks-unconditionally?
                                                       ::only-warn?])))

(def settings (atom impl.checking/global-state-default-value
                    :validator (fn [m]
                                 (check! ::global-settings m))))

(def ^:dynamic *warn* *out*)

#?(:clj
   (defmacro checking
     "Builds a series `#'check!` calls with fine-grained control over:

  - whether the checks are performed; and
  - whether the checks should result in an exception being thrown, or simply println to `#'*warn*` instead."
     {:style/indent 1}
     [options & args]
     {:pre [(check! ::context-options      options
                    #(-> % count even?)    args
                    #(-> % count pos?)     args
                    (spec/coll-of symbol?) (->> args (partition 2) (map second)))]}
     (->> args
          (partition 2)
          (map (fn [[spec v]]
                 (let [{:keys [always? warn?]} (impl.checking/determine {:global-settings  @settings
                                                                         :context-settings options  ;; XXX must be translated from speced.def lingo. maybe validate here
                                                                         :symbol-settings  (meta v) ;; XXX same
                                                                         :v                v})]
                   (assert (not (and always? warn?))) ;; XXX think if there's some legit way to get into this state
                   (let [the-check (list `nedap.utils.spec.impl.check/check! (not warn?) spec v)]
                     (if (or always? warn?)
                       the-check
                       (list `assert the-check))))))
          (impl.checking/append true)
          (apply list `do))))
