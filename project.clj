(defproject inductive-study "0.1.0-SNAPSHOT"
  :description "A tool for inductive [bible] study"
  :url "https://github.com/jeremyrsellars/inductive-study"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.7.3"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [com.keminglabs/cljx "0.6.0"]
            [com.cemerick/clojurescript.test "0.3.1"]]

  :source-paths ["src" "test"]

  :profiles {:dev {:dependencies [[criterium "0.4.3"]
                                  [com.keminglabs/cljx "0.6.0"]]
                   :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}
                   :hooks [cljx.hooks]}}
  :cljx {:builds [{:source-paths ["src" "test"]
                   :output-path "target/node/classes"
                   :rules {:filetype "cljs"
                           :features #{"node" "cljs"}
                           :transforms []}}
                  {:source-paths ["src" "test"]
                   :output-path "target/browser/classes"
                   :rules {:filetype "cljs"
                           :features #{"browser" "cljs"}
                           :transforms []}}
                  ]}
  :hooks [cljx.hooks]
  :cljsbuild {:builds [{:id "node"
                        :source-paths ["target/node/classes"]
                        :compiler {:output-to "out/inductive.js"
                                   :output-dir "out"
                                   :externs ["resources/externs/process.js"]
                                   :optimizations :simple
                                   :source-map "out/inductive.map"
                                   :target :nodejs}}
                       {:id "browser"
                        :source-paths ["target/browser/classes"]
                        :externs ["public/javascripts/htmlparser.min.js"]
                        :compiler {:output-to "public/js/inductive.js"
                                   :output-dir "public/js"
                                   :optimizations :simple
                                   :source-map "public/js/inductive.map"}}
                       ]})
