(defproject clj-forex "0.0.1"
  :description "communicate with metatrader to trade forex in clojure"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 ;;zeromq
                 [org.zeromq/jzmq "2.0.9"]
                 [org.clojars.mikejs/clojure-zmq "2.0.7-SNAPSHOT"]
                 ;;mail
                 [mmemail "1.0.1"]

                 ]
  :dev-dependencies [[native-deps "1.0.4"] [swank-clojure "1.2.1"]]
  :native-dependencies [[org.clojars.storkle/zmq-native "1.0.0"]]
 
  ;:main forex.binding
  
 )
