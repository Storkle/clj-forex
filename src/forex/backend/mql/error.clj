(clojure.core/use 'nstools.ns)
(ns+ forex.backend.mql.error
     (:clone clj.core) 
     (:require
      clojure.contrib.core
      [forex.util.fiber.mbox :as m]
      [clojure.contrib.logging :as l]) 
     (:import (java.io DataInputStream ByteArrayInputStream))
     (:use
      forex.backend.mql.utils
      forex.util.emacs 
      forex.util.core forex.util.general
      forex.util.zmq forex.util.log
      forex.util.spawn))

(def +error-clj-service-queue+ -4)
(def +error-clj-service-dead+ -3)
(def +error-socket-repeat+ -2)
 
(def mql-service-errors
  {-4 "Metatrader side of socket service is not alive"
   -3 "Socket service is not alive"
   -2 "Failed to send message"
   -1 "Unknown protocol sent"})
    
(def mql-runtime-errors
  {4000 "No error"
   4001 "Wrong function pointer"
   4002 "Array index out of range"
   4003 "No memory for call stack"
   4004 "Recursive stack overflow"
   4005 "Not enough stack for param"
   4006 "No memory for param string"
   4007 "No memory for temp string"
   4008 "Not initialized string"
   4009 "Not initialized arraystring"
   4010 "No memory for arraystring"
   4011 "Too long string"
   4012 "Remainder from zero divide"
   4013 "Zero divide"
   4014 "Unknown command"
   4015 "Wrong jump (never generated error)"
   4016 "Not initialized array"
   4017 "Dll calls not allowed"
   4018 "Cannot load library"
   4019 "Cannot call function"
   4020 "External calls not allowed"
   4021 "No memory for returned string"
   4022 "System is busy (never generated error)"
   4050 "Invalid function parameters count"
   4051 "Invalid function parameter value"
   4053 "Some array error"
   4054 "Incorrect series array using"
   4055 "Custom indicator error"
   4056 "Arrays are incompatible"
   4057 "Global variables processing error"
   4058 "Global variable not found"
   4059 "Function is not allowed in testing mode"
   4060 "Function is not confirmed"
   4061 "Send mail error"
   4062 "String parameter expected"
   4063 "Integer parameter expected"
   4064 "Double parameter expected"
   4065 "Array as parameter expected"
   4066 "Requested history data in updating state"
   4067 "Some error in trading function"
   4099 "End of file"
   4100 "Some file error"
   4101 "Wrong file name"
   4102 "Too many opened files"
   4103 "Cannot open file"
   4104 "Incompatible access to a file"
   4105 "No order selected"
   4106 "Unknown symbol"
   4107 "Invalid price"
   4108 "Invalid ticket"
   4109 "Trade is not allowed."
   4110 "Longs are not allowed"
   4111 "Shorts are not allowed"
   4200 "Object exists already"
   4201 "Unkown object property"
   4202 "Object does not exist"
   4203 "Unkown object type"
   4204 "No object name"
   4205 "Object coordinates error"
   4206 "No specified subwindow"
   4207 "Some error in object function"})
(def mql-trade-server-errors
  {0 "No error returned"
   1 "No error returned, but result is unknown"
   2 "Common error"
   3 "Invalid trade parameters"
   4 "Trade server is busy"
   5 "Old version of the client terminal"
   6 "No connection with trade server"
   7 "Not enough rights"
   8 "Too frequent requests"
   9 "Malfunctional trade operation"
   64 "Account disabled"
   65 "Invalid account"
   128 "Trade timeout"
   129 "Invalid price"
   130 "Invalid stops"
   131 "Invalid trade volume"
   132 "Market is closed"
   133 "Trade is disabled"
   134 "Not enough money"
   135 "Price changed"
   136 "Off quotes"
   137 "Broker is busy"
   138 "Requote"
   139 "Order is locked"
   140 "Long positions only allowed"
   141 "Too many requests"
   145 "Modification denied because order too close to market"
   146 "Trade context is busy"
   147 "Expirations are denied by broker"
   148 "Too many orders"
   149 "Hedging prohibited"
   150 "Trade prohibited by FIFO"})

(defn mql-error-to-str [e]
  (or (when-let [it (get mql-service-errors e)]
	(format "Clj-forex service error %s - %s" e it))
      (when-let [it (get mql-runtime-errors e)]
	(format "Runtime error %s - %s" e it))
      (when-let [it  (get mql-trade-server-errors e)]
	(format "Trade server error %s - %s" e it))
      (format "Unkown error %s" e)))

(defrecord MqlError [e]
  Object
  (toString [this]
	    (format "<MqlError: %s>" (mql-error-to-str e)))) 
(defn new-mql-error [e]
  (MqlError. e))
(defn e? [a]
  (instance? MqlError a))

(defmethod clojure.core/print-method MqlError [o w]
  (.write w (.toString o)))