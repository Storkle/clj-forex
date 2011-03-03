
;;TODO: still a work in progress. for now, lets just execute a function in user
;;ea ns, and that function will prompt user and run ea!
 
(ns forex.interface.ea-new
  (:use forex.util.log clojure.contrib.with-ns
        forex.module.ea
        clojure.contrib.miglayout forex.util.emacs 
        forex.util.gui)
  (:use forex.interface.gui) 
  (:import 
   (java.awt Dimension Font event.MouseListener
             event.MouseAdapter Toolkit event.ActionListener event.KeyListener)
   (javax.swing JComboBox JPopupMenu JMenuItem
                ImageIcon
                table.AbstractTableModel 
                JScrollPane JTable
                JFrame JPanel JTextField JLabel JButton
                SwingUtilities)))

(defn ns-args [ns]
  (ns-metas ns (fn [s] (:arg (meta s)))))

(defn ea-new-gui []
  (when-let [it (and (:selector gui)
                     (.getSelectedItem (:selector gui)))]
    (when-let [ns (find-ns (symbol it))]
      (println "selection is " ns))))
(defn var-name [a]
  (last (.split (str a) "/")))
;;TODO: throw slot value not working!s + add metadata to jlabel,etc
(defn read-eval [a]
  (let [a (try (read-string (.getText a))
               (catch Exception e (.getText a)))]
    (if (or (string? a) (symbol? a)) (str a) (eval a))))

(defn on-start [frame panel]
  (println "result is " (keys (components panel))))
  
(defn generate-ea-gui [ns] 
  (let [args (ns-args ns)
        panel (JPanel.) frame (jframe (str (ns-name ns)))]
    (doto frame
      (.add (apply miglayout panel
                   (concat
                    (mapcat (fn [[key val]]
                              (let [label (JLabel. (var-name key))]
                                [label (JTextField. 30)         
                                 :wrap]))
                            args)
                    [(doto (JButton. "start")
                       (add-action-listener (fn [e] (on-start frame panel))))
                     "push,growx,span,center"])))
      (.pack)
      (.setVisible true)))) 

(defn run-init-gui []
  (let [ns (symbol (.getSelectedItem (:selector gui)))]
   (if-let [it (get (ns-map ns) 'init-gui)]
     (if (fn? (var-get it))
       (binding [*ns* (the-ns ns)]
         ((var-get it))) 
       (inform "symbol init-gui in namespace %s is not a function" ns ))
     (inform "%s does not have an init-gui function" ns)))) 

(alter-var-root
 #'forex.interface.gui/gui-key-map
 merge {\n #'run-init-gui})


