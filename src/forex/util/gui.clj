
(ns forex.util.gui
  (:require swank.swank)
  (:use forex.util.general clojure.contrib.miglayout forex.util.emacs forex.util.log)
  (:import 
   (java.awt Dimension Font event.MouseListener
             event.MouseAdapter Toolkit event.ActionListener event.KeyListener)
   (javax.swing JComboBox JPopupMenu JMenuItem
                ImageIcon
                table.AbstractTableModel
                JScrollPane JTable
                JFrame JPanel JTextField JLabel JButton
                SwingUtilities)))

(import javax.swing.JOptionPane)

(defn add-watcher [var key f]
  (add-watch
   var key
   (bound-fn [& args]
	     (try (apply f args)
		  (catch Exception e
                              
		    (severe "caught exception in watcher %s: %s"
			    [var key] e)
		    (.printStackTrace e))))))

(defn inform [msg & args]
  (JOptionPane/showMessageDialog
   nil (apply format (str msg) args) "!" JOptionPane/INFORMATION_MESSAGE)) 
(defn prompt
  ([msg & args]     
     (let [no-default true
           p (if no-default 
               (JOptionPane/showOptionDialog (JPanel.)
                                             (apply format (str msg) args) "???"
                                             JOptionPane/YES_NO_OPTION
                                             JOptionPane/QUESTION_MESSAGE
                                             nil
                                             (into-array  ["Yes" "No"])
                                             "No")
               (JOptionPane/showConfirmDialog
                nil (apply format (str msg) args)
                "???" JOptionPane/YES_NO_OPTION))]
       (if (= JOptionPane/YES_OPTION p)
         true
         false))))


(defn create-image [path]
  (.getImage (Toolkit/getDefaultToolkit) path))
(defn create-icon [path]
  (try
    (ImageIcon. (create-image path))
    (catch Exception e
      (warn "couldnt create image %s" path)
      nil)))

(defn- get-fn [a] (if (var? a) (var-get a) a))
(defn- call-fn [a & args]
  (thread
   (let [f (get-fn a)]
     (if (fn? f) 
       (apply f args)
       (warn "in listener, in var %s,val %s is not a function" a f)))))

(defn add-key-listener [obj f]
  (when (fn? (get-fn f))
    (.addKeyListener obj (proxy [KeyListener] []
                           (keyReleased [e] (call-fn f e))
                           (keyPressed [e])
                           (keyTyped [e])))))

(defn add-action-listener [item val]
  (when (or (and (var? val) (fn? (var-get val))) (fn? val))
    (let [f (bound-fn [e] (call-fn val e))]
      (.addActionListener item
                          (proxy [ActionListener] nil 
                            (actionPerformed [e]
                                             (try
                                               (f e)
                                               (catch Exception e
                                                 (severe "error in action listener %s" e)))))))))
(defn add-popup-listener [obj f]
  (when  (fn? (get-fn f))
    (let [f (bound-fn [e] (call-fn f e))]
     (.addMouseListener
      obj
      (proxy [MouseAdapter] nil
        (mousePressed [e] (when (.isPopupTrigger e) (f e)))
        (mouseReleased [e] (when (.isPopupTrigger e) (f e)))))))
  obj)
;;TODO; println stack trace
(defmacro invoke-later [& args]
  `(SwingUtilities/invokeLater (bound-fn []
                                         (try (do ~@args)
                                                
                                              (catch Exception e#
                                                (.printStackTrace e#)
                                                (println e#))))))


