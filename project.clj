;; Please don't bump the library version by hand - use ci.release-workflow instead.
(defproject com.nedap.staffing-solutions/utils.spec "1.5.0-alpha1"
  ;; Please keep the dependencies sorted a-z.
  :dependencies [[expound "0.8.4"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/spec.alpha "0.2.194"]]

  :managed-dependencies [[com.cognitect/transit-clj "1.0.324"]
                         [com.fasterxml.jackson.core/jackson-core "2.12.4"]
                         [com.google.code.findbugs/jsr305 "3.0.2"]
                         [com.google.errorprone/error_prone_annotations "2.1.3"]
                         [org.clojure/core.rrb-vector "0.1.2"]
                         [org.clojure/tools.reader "1.3.6"]]

  :exclusions [com.nedap.staffing-solutions/utils.spec]

  :description "clojure.spec utilities"

  :url "https://github.com/nedap/utils.spec"

  :min-lein-version "2.0.0"

  :license {:name "EPL-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :signing {:gpg-key "releases-staffingsolutions@nedap.com"}

  :repositories {"github" {:url      "https://maven.pkg.github.com/nedap/*"
                           :username "github"
                           :password :env/github_token}}

  :deploy-repositories {"clojars" {:url      "https://clojars.org/repo"
                                   :username :env/clojars_user
                                   :password :env/clojars_pass}}
  :target-path "target/%s"

  :test-paths ["src" "test"]

  :monkeypatch-clojure-test false

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-pprint "1.1.2"]]

  ;; Please don't add `:hooks [leiningen.cljsbuild]`. It can silently skip running the JS suite on `lein test`.
  ;; It also interferes with Cloverage.

  :cljsbuild {:builds {"test" {:source-paths ["src" "test"]
                               :compiler     {:main          nedap.utils.spec.test-runner
                                              :output-to     "target/out/tests.js"
                                              :output-dir    "target/out"
                                              :target        :nodejs
                                              :optimizations :none}}}}

  ;; A variety of common dependencies are bundled with `nedap/lein-template`.
  ;; They are divided into two categories:
  ;; * Dependencies that are possible or likely to be needed in all kind of production projects
  ;;   * The point is that when you realise you needed them, they are already in your classpath, avoiding interrupting your flow
  ;;   * After realising this, please move the dependency up to the top level.
  ;; * Genuinely dev-only dependencies allowing 'basic science'
  ;;   * e.g. criterium, deep-diff, clj-java-decompiler

  ;; NOTE: deps marked with #_"transitive" are there to satisfy the `:pedantic?` option.
  :profiles {:dev      {:dependencies [[cider/cider-nrepl "0.16.0" #_"formatting-stack needs it"]
                                       [com.clojure-goes-fast/clj-java-decompiler "0.2.1"]
                                       [com.stuartsierra/component "0.4.0"]
                                       [com.taoensso/timbre "4.10.0"]
                                       [criterium "0.4.5"]
                                       [formatting-stack "4.6.0"]
                                       [lambdaisland/deep-diff "0.0-29"]
                                       [medley "1.3.0"]
                                       [org.clojure/core.async "0.5.527"]
                                       [org.clojure/math.combinatorics "0.1.1"]
                                       [org.clojure/test.check "0.10.0-alpha3"]
                                       [org.clojure/tools.namespace "1.2.0"]]
                        :plugins      [[lein-cloverage "1.1.1"]]
                        :source-paths ["dev"]
                        :repl-options {:init-ns dev}}

             :provided {:dependencies [[org.clojure/clojurescript "1.10.597"]
                                       [spec-coerce "1.0.0-alpha9"]]}

             :check    {:global-vars {*unchecked-math* :warn-on-boxed
                                      ;; avoid warnings that cannot affect production:
                                      *assert*         false}}

             :test     {:dependencies [[com.nedap.staffing-solutions/utils.test "1.9.0"]]
                        :jvm-opts     ["-Dclojure.core.async.go-checking=true"
                                       "-Duser.language=en-US"]}

             :ncrw     {:global-vars    {*assert* true} ;; `ci.release-workflow` relies on runtime assertions
                        :source-paths   ^:replace []
                        :test-paths     ^:replace []
                        :resource-paths ^:replace []
                        :plugins        ^:replace []
                        :dependencies   ^:replace [[com.nedap.staffing-solutions/ci.release-workflow "1.14.1"]]}

             :ci       {:pedantic? :abort
                        :jvm-opts  ["-Dclojure.main.report=stderr"]}})
