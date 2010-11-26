(defproject clj-forex "0.0.1"
  :description "non graphical library for automated trading for forex market with various backends like metatrader"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 ;;zeromq
                 [org.zeromq/jzmq "2.0.9"]
                 [org.clojars.mikejs/clojure-zmq "2.0.7-SNAPSHOT"]
                 ;;mail
                 [mmemail "1.0.1"]

                 ]
  :java-source-path [["src/forex/indicator"]]
  :dev-dependencies [[native-deps "1.0.4"] [swank-clojure "1.2.1"]
                     [lein-javac "1.2.1-SNAPSHOT"]]
  :native-dependencies [[org.clojars.storkle/zmq-native "1.0.0"]]
  ;:main forex.binding  
 )
