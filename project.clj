;;TODO: need to AOT compile 
(defproject clj-forex "0.1"
  :description "non graphical library for automated trading for forex market with various backends like metatrader"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 ;;zeromq
                 [org.zeromq/jzmq "2.0.9"]
                 [org.clojars.mikejs/clojure-zmq "2.0.7-SNAPSHOT"]
		 [clj-time "0.2.0-SNAPSHOT"]
                 ;;mail
                 [mmemail "1.0.1"]
                 ;;clj-forex 
                 [clj-forex-utils "0.1"] [clj-forex-indicators "0.1"]
                 ]
  :dev-dependencies [[native-deps "1.0.4"] [swank-clojure "1.3.0-SNAPSHOT"]]
  :native-dependencies [[org.clojars.storkle/zmq-native "1.0.0"]]
  ;:main forex.binding  
 )
