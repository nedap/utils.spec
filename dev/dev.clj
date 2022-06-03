(ns dev
  (:require
   [clj-java-decompiler.core :refer [decompile]]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.test :refer [run-all-tests run-tests]]
   [clojure.tools.namespace.repl :refer [clear refresh refresh-dirs set-refresh-dirs]]
   [criterium.core :refer [quick-bench]]
   [lambdaisland.deep-diff]))

(set-refresh-dirs "src" "test" "dev")

(defn suite []
  (refresh)
  (run-all-tests #".*\.nedap\.utils\.spec.*"))

(defn unit []
  (refresh)
  (run-all-tests #"unit\.nedap\.utils\.spec.*"))

(defn slow []
  (refresh)
  (run-all-tests #"integration\.nedap\.utils\.spec.*"))

(defn diff [x y]
  (-> x
      (lambdaisland.deep-diff/diff y)
      (lambdaisland.deep-diff/pretty-print)))

(defn format-and-lint-branch! [& {:keys [branch in-background?]
                                  :or   {branch         "main"
                                         in-background? false}}]
  (refresh)
  ((requiring-resolve 'formatting-stack.branch-formatter/format-and-lint-branch!)
   :target-branch  branch
   :in-background? in-background?))

(defn format-and-lint-project!
  "Formats the whole project."
  [& {:keys [in-background?]
      :or   {in-background? false}}]
  (refresh)
  ((requiring-resolve 'formatting-stack.project-formatter/format-and-lint-project!) :in-background? in-background?))
