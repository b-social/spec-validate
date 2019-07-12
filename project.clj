(defproject b-social/spec-validate "0.1.9-SNAPSHOT"
  :description "A clojure.spec based validation library."
  :url "https://github.com/b-social/spec-validate"
  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[valip "0.2.0"]
                 [clj-time "0.15.1"]
                 [clojurewerkz/money "1.10.0"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [com.googlecode.libphonenumber/libphonenumber "8.10.0"]]
  :plugins [[lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]
            [lein-cloverage "1.0.13"]
            [lein-eftest "0.5.3"]
            [lein-shell "0.5.0"]]
  :profiles {:shared {:dependencies [[org.clojure/clojure "1.10.0"]
                                     [eftest "0.5.3"]]}
             :dev    [:shared]
             :test   [:shared]}
  :eftest {:multithread? false}
  :repl-options {:init-ns spec-validate.core}
  :deploy-repositories {"releases" {:url   "https://repo.clojars.org"
                                    :creds :gpg}}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["vcs" "push"]]
  :aliases {"test" ["eftest" ":all"]})
