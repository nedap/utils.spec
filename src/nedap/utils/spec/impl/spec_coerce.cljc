(ns nedap.utils.spec.impl.spec-coerce)

(defn spec-coerce-available? []
  (try
    (require 'spec-coerce.core)
    true
    (catch Exception _
      false)))
