(ns forex.console
  (:gen-class
   :extends java.util.logging.ConsoleHandler
   :constructors {[java.io.OutputStream] []}
   :post-init set-out
   :init init))
;;auto get state


(defn -init
  [stream]
  [[] nil])
(defn -set-out [this stream]
  (.setOutputStream this stream))
 