(defproject om-tut "0.1.0-SNAPSHOT"
  :description "A tool for inductive [bible] study"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.5.0"]]

  :plugins [[lein-cljsbuild "1.0.2"]
            [com.keminglabs/cljx "0.4.0"]]

  :source-paths ["src"]

  :profiles {:dev {:dependencies [[criterium "0.4.1"]
                                  [com.keminglabs/cljx "0.4.0"]]
                   :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}
                   :hooks [cljx.hooks]}}
  :cljx {:builds [{:source-paths ["src"]
                   :output-path "target/browser/classes"
                   :rules {:filetype "cljs"
                           :features #{"browser" "cljs"}
                           :transforms []}}
                  {:source-paths ["src"]
                   :output-path "target/node/classes"
                   :rules {:filetype "cljs"
                           :features #{"node"}
                           :transforms []}}]}
  :hooks [cljx.hooks]
  :cljsbuild {:builds [{:id "browser"
                        :source-paths ["target/browser/classes"]
                        :compiler {:output-to "public/js/inductive.js"
                                   :output-dir "public/js"
                                   :optimizations :simple
                                   :source-map "public/js/inductive.map"}}
                       {:id "node"
                        :source-paths ["target/node/classes"]
                        :compiler {:output-to "out/inductive.js"
                                   :output-dir "out/node"
                                   :externs ["resources/externs/process.js"]
                                   :optimizations :simple
                                   :source-map "out/inductive.map"
                                   :target :nodejs}}]})
