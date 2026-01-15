(defproject b-social/spec-validate "0.1.12"
  :description "A clojure.spec based validation library."
  :url "https://github.com/b-social/spec-validate"

  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[commons-validator "1.10.0"]
                 [clj-time "0.15.1"]
                 [clojurewerkz/money "1.10.0"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [com.googlecode.libphonenumber/libphonenumber "8.12.51"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]
            [lein-cloverage "1.1.1"]
            [lein-eftest "0.5.8"]
            [lein-shell "0.5.0"]
            [lein-codox "0.10.7"]
            [lein-cljfmt "0.6.4"]]

  :profiles {:shared {:dependencies [[org.clojure/clojure "1.11.2"]
                                     [eftest "0.5.8"]]}
             :dev    [:shared]
             :test   [:shared]}

  :cloverage
  {:ns-exclude-regex [#"^user"]}

  :codox
  {:namespaces  [#"^spec-validate\."]
   :output-path "docs"
   :doc-paths   ["docs"]
   :source-uri  "https://github.com/b-social/spec-validate/blob/{version}/{filepath}#L{line}"}

  :cljfmt {:indents ^:replace {#".*" [[:inner 0]]}}

  :eftest {:multithread? false}

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org"
               :username :env/clojars_username
               :password :env/clojars_password}}

  :release-tasks
  [["shell" "git" "diff" "--exit-code"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["codox"]
   ["changelog" "release"]
   ["shell" "sed" "-E" "-i" "" "s/\"[0-9]+\\.[0-9]+\\.[0-9]+\"/\"${:version}\"/g" "README.md"]
   ["shell" "git" "add" "."]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["vcs" "push"]]

  :aliases {"test" ["eftest" ":all"]})
