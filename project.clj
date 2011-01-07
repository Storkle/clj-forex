(defproject clj-forex "0.1"
  :description "non graphical library for automated trading for forex market with various backends like metatrader"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [clj-time "0.2.0-SNAPSHOT"]		
                 [mmemail "1.0.1"]
		 [clojureql "1.0.0-beta2-SNAPSHOT"]
		 
                 ;;clj-forex 
                 [org.clojars.starry/clj-forex-utils "0.2"]
		 [org.clojars.starry/clj-forex-indicators "0.1"]]   
   :dev-dependencies [[native-deps "1.0.5"]
		      [swank-clojure "1.3.0-SNAPSHOT"]]
   :native-dependencies [[org.clojars.starry/jzmq-native-deps "2.0.10"]]
   :jvm-opts      ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8030"]
  ;;:main forex.binding
  ;;:aot :all
   )   


