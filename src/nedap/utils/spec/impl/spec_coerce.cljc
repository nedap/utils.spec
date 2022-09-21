(ns nedap.utils.spec.impl.spec-coerce)

(defmacro when-spec-coerce-available? [& body]
  (when (try
          (requiring-resolve 'spec-coerce.core/coerce)
          true
          (catch Exception _
            false))
    `(do ~@body)))
