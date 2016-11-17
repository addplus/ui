; To inform IntelliJ explicitely about deftask, set-env!, task-options!
(require '[boot.core :refer :all]
         '[boot.task.built-in :refer :all])

(def +version+ "0.1.0-SNAPSHOT")

(task-options!
  pom    {:project     'hoplon/ui
          :version     +version+
          :description "a cohesive layer of composable abstractions over the dom."
          :url         "https://github.com/hoplon/ui"
          :scm         {:url "https://github.com/hoplon/ui"}
          :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :source-paths #{"src"}
  :test-paths   #{"tst"}
  :dependencies '[[boot/core                 "2.6.0"]
                  [onetom/boot-lein-generate "0.1.3"          :scope "test"]
                  [org.clojure/clojure       "1.9.0-alpha13"  :scope "provided"]
                  [org.clojure/clojurescript "1.9.293"        :scope "provided"]
                  [adzerk/boot-cljs          "1.7.228-2"      :scope "test"]
                  [adzerk/boot-reload        "0.4.12"         :scope "test"]
                  [adzerk/bootlaces          "0.1.13"         :scope "test"]
                  [tailrecursion/boot-static "0.0.1-SNAPSHOT" :scope "test"]
                  [hoplon/hoplon             "6.0.0-alpha17"]
                  [cljsjs/markdown           "0.6.0-beta1-0"]

                  [binaryage/devtools "0.8.2"                 :scope "test"]
                  [binaryage/dirac "0.8.4"                    :scope "test"]
                  [powerlaces/boot-cljs-devtools "0.1.2"      :scope "test"]]
  :repositories  [["clojars"       "https://clojars.org/repo/"]
                  ["maven-central" "https://repo1.maven.org/maven2/"]])

(require 'boot.lein)
(boot.lein/generate)

(require
  '[adzerk.bootlaces          :refer :all]
  '[adzerk.boot-cljs          :refer [cljs]]
  '[adzerk.boot-reload        :refer [reload]]
  '[hoplon.boot-hoplon        :refer [hoplon]]
  '[tailrecursion.boot-static :refer [serve]]
  '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]])

(bootlaces! +version+)

(def devtools-config
  {:features-to-install           [:formatters :hints :async]
   :dont-detect-custom-formatters true})

(task-options!
  serve {:port 5000}
  reload {:on-jsload 'hoplon.index/page}
  cljs-devtools {:dirac-opts {:nrepl-server {:port 5001}
                              :nrepl-tunnel {:port 5002}}}
  cljs {:optimizations    :none
        :compiler-options {:parallel-build  true
                           :external-config {:devtools/config      devtools-config
                                             :dirac.runtime/config {:agent-port 5002}}}})

(deftask develop []
  (comp (watch) (speak) (build-jar)))

(deftask deploy []
  (comp (speak) (build-jar) (push-snapshot)))

(deftask test []
  (as-> (get-env) $
        (clojure.set/union (:source-paths $) (:test-paths $))
        (set-env! :source-paths $))
  (comp (watch) (speak) (hoplon) (reload) (cljs-devtools) (cljs) (serve)))
