
(defproject clj-forex "0.1-SNAPSHOT"
  :description "non graphical library for automated trading for forex market with various backends like metatrader"
  ;; :warn-on-reflection true  
  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]           
                 [matchure "0.10.0"]
                 [clj-time "0.2.0-SNAPSHOT"] 
                 [nstools "0.2.4"]
                 [com.miglayout/miglayout "3.7.4"]
		 [commons-codec "1.4"]]   
  :dev-dependencies [[autodoc "0.7.1"]
		     [native-deps "1.0.5"]
                     [swank-clojure "1.3.0-SNAPSHOT"]]  
  :native-dependencies [[org.clojars.starry/jzmq-native-deps "2.0.10.4"]]
  :main ^:skip-aot forex.interface.main
  :repositories {"miglayout" "http://www.miglayout.com/mavensite/"} 
  ;; :run-aliases {:izpack forex.dev.install} 
  ;; :jvm-opts      ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8030"]
  ;;:aot :all
  )
 
