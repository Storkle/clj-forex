
(defproject clj-forex "0.1"
  :description "non graphical library for automated trading for forex market with various backends like metatrader"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [clj-time "0.2.0-SNAPSHOT"]
                 ;;zeromq            
                 [org.clojars.mikejs/clojure-zmq "2.0.7-SNAPSHOT"]		
                 ;;mail
                 [mmemail "1.0.1"]
                 ;;clj-forex 
                 [org.clojars.starry/clj-forex-utils "0.1"]
		 [org.clojars.starry/clj-forex-indicators "0.1"]]   
   :dev-dependencies [[native-deps "1.0.5"]
		      [swank-clojure "1.3.0-SNAPSHOT"]]
   :native-dependencies [[org.clojars.starry/jzmq-native-deps "2.0.9.0"]]
  ;;:main forex.binding
  ;;:aot :all
   )  


