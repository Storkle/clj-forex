(clojure.core/use 'nstools.ns)
(ns+ forex.dev.install
     (:clone clj.core)
     (:use 
      clj.io
      clojure.contrib.prxml
      clojure.contrib.shell)) 
  
(defn panels [& args]
  [:panels
   (for [classname args]
     [:panel {:classname classname}])])
(defn pfile [name target]
  [:file {:src name :targetdir target}])
(defn izpack-compile [izpack-dir xml base-dir]
  (sh (format "%s/bin/compile" izpack-dir) xml "-b" base-dir :return-map true))
(defn prompt [prompt]
  (print (format "%s: " prompt))
  (flush )
  (read-line))

(defn write-xml []
  (prxml
   [:installation
    {:version "1.0"}
    ;;LOCALE
    [:locale [:langpack {:iso3 "eng"}]]
    ;;INFO
    [:info
     [:appname "clj-forex"]
     [:appversion 0.1]
     [:authors
      [:author {:name "seth burleigih" :email "email"}]]
     [:url "http://www.clj-forex.org"]]
    ;;GUI PREFS
    [:guiprefs {:width "640" :height "480" :resizable "yes"}]
    ;;VARIABLES
    [:variables
     [:variable {:name "enabled?" :value "true"}]]
    ;;RESOURCES
    [:resources
     [:res {:id "LicencePanel.licence" :src "LICENSE"}]
     [:res {:id "InfoPanel.info" :src "README"}]]
    ;;PANELS
    (panels "HelloPanel" "LicencePanel" "TargetPanel"
	    "PacksPanel" "InstallPanel" 
	    "FinishPanel")
    ;;PACKS 
    [:packs
     [:pack {:name "Base" :required "yes"}
      [:description "The Base files"]
      (pfile "README" "$INSTALL_PATH")
      (pfile "LICENSE" "$INSTALL_PATH")
      (pfile "project.clj" "$INSTALL_PATH")
      [:fileset {:dir "src" :targetdir "$INSTALL_PATH/src"}  
       [:include {:name "**"}]]
      [:fileset {:dir "lib" :targetdir "$INSTALL_PATH/lib"}
       [:include {:name "**"}]]
      [:fileset {:dir "pictures" :targetdir "$INSTALL_PATH/pictures"}
       [:include {:name "**"}]]
      [:fileset {:dir "native" :targetdir "$INSTALL_PATH/native"}
       [:include {:name "**"}]]]]
    ;;NATIVE
    (comment
      [:native {:type "izpack" :name "ShellLink.dll"}]
      [:native {:type "3rdparty" :name "COIOSHelper.dll" :stage "both"}
       [:os {:family "windows"}]])]))  
(defn -main 
  ([] (-main "/home/seth/.opt/IzPack"))
  ([izdir]        
     (println "writing install.xml") 
     (with-out-writer "install.xml"
       (binding [*prxml-indent* 2]
	 (write-xml)))
     (println "running izpack ...")
     (let [result (izpack-compile 
		   izdir 		 
		   "install.xml" 
		   ".")]
       (println (:out result))
       (println (:err result)))))
;;    (delete-file-recursively "dev/izpack/" true)
;;     (make-parents "install.xml")
(comment
  (println "copying files")
  (copy-dir "pictures" "dev/izpack/pictures")
  (copy-dir "src" "dev/izpack/src")
  (copy-dir "native" "dev/izpack/native")
  (copy-dir "lib" "dev/izpack/lib")
  (copy "LICENSE" "dev/izpack/LICENSE") 
  (copy "README" "dev/izpack/README")
  (copy "project.clj" "dev/izpack/project.clj"))