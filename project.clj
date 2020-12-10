(defproject ice-melting-sim "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha4"]
                 [quil "3.1.0"]]
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
  :native-image {:name "ice-melting-sim"
                 :graal-bin "graalvm-ce-java8-20.3.0/Contents/Home/"
                 :opts ["--no-server"
                        "--no-fallback"]}
  :profiles {:uberjar {:aot :all
                       :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}}
  :main "ice-melting-sim.core"
)
