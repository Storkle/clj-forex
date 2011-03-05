
(clojure.core/use 'nstools.ns)
(ns+ forex.gui 
     (:clone clj.core)
     (:import (javax.swing JScrollPane JFrame JPanel JTextArea
                           JLabel JButton SwingUtilities))
     (:use forex.util.general forex.util.gui clojure.contrib.miglayout)
     (:use forex.util.core
           forex.util.emacs
           forex.util.log)  
     (:use forex.module.error
           forex.module.ea 
           forex.module.indicator
           forex.module.account forex.module.account.utils
           [clj-time.core :exclude [extend start]])
     (:require
      [forex.module.service :as backend]))

