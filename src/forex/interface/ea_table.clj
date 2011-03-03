
(ns forex.interface.ea-table
  (:require swank.swank) 
  (:use forex.util.log 
        clojure.contrib.miglayout forex.util.emacs 
        forex.util.gui forex.interface.tray)
  (:use forex.module.ea forex.interface.gui)
  (:import
   (java.awt
    Dimension Font event.MouseListener
    event.MouseAdapter Toolkit event.ActionListener event.KeyListener)
   (javax.swing JComboBox JPopupMenu JMenuItem
                ImageIcon
                table.AbstractTableModel 
                JScrollPane JTable
                JFrame JPanel JTextField JLabel JButton
                SwingUtilities))) 
 
(defonce ea-table-gui  nil)
(defn- italic [a] (format "<html><i>%s</i><html>" a))
(defn- color [a color]
  (format "<html><color=%s><b>%s</b></color><html>" color a))




(defn table-model [titles data]
  (proxy [AbstractTableModel] []
    (getRowCount [] (count data))
    (getColumnCount [] (count titles))
    (getValueAt [row column] (nth (nth data  row) column))
    (getColumnClass [_] String)
    (getColumnName [col] (nth titles col))))


(defn ea-table-model [eas] 
  (table-model
   [(italic "Name") (italic "Symbol") (italic "Period") (italic "Running?") (italic "Args")]
   (map (fn [ea] 
          [(:type ea) (:symbol ea) (:period ea)
           (let [exit @(:exit ea)]
             (cond
              (and (alive? ea) (= exit false)) (color true "blue")
              true  (if-not (= true  exit)
                      (color false "red")
                      false)))
           (:args ea)
           ea])
        eas)))

(defn update-table [table eas]
  (locking table
   (.setModel table (ea-table-model eas))))

(defn update-ea-table
  ([] (update-ea-table false))
  ([force]
     (let [{:keys [table frame]} ea-table-gui]
       (when (and table frame (or force (.isVisible frame))) 
         (update-table table @*eas*)))))

(defn add-ea-table-watch []
  (add-hook #'ea-on-start-hook #'update-ea-table)
  ;;TODOD: better add-to-list, not repeat, maybe per *ns*?
  (add-hook #'ea-on-exit-hook #'update-ea-table)
  (add-watcher *eas* "table update" (fn [& args]
                                      (update-ea-table))))

;;
(defn gui-new-table [frame]
  (let [eas @*eas*
        table (JTable. (ea-table-model eas))
        panel
        (miglayout (JPanel.)
                   (JScrollPane. table) 
                   "span,grow" :wrap 
                   (doto  (JButton. "refresh")
                     (add-action-listener
                      (fn [e] (update-table table @*eas*))))
                   (doto (JButton. "stop")
                     (add-action-listener
                      (fn [e]
			;;TODO: locking on a table is bad if youre going to have a prompt - will lock up stopping of eas, etc. x
                        (locking table 
                          (let [i (.getSelectedRow table)
                                ea (when (>= i 0) (.getValueAt (.getModel table)
                                                               i (.getColumnCount table)))]
                            (when (and (>= i 0)
                                       (prompt "do you really want to stop ea %s" i))
                              (stop ea))))))) 
                   (doto (JButton. "clear")
                     (add-action-listener
                      (fn [e]
                        (when (prompt "Do you really want to clear stopped eas?" true)
                          (update-table
                           table
                           (swap! *eas* (fn [old]
                                          (filter alive? old)))))))))]
    (alter-var-root  #'ea-table-gui (constantly {:table table :frame frame}))
    panel))
 
(defn display-ea-table []
  (if-not (:frame ea-table-gui)
    (let [frame (jframe "expert advisors")]
      (doto frame 
        (.add (gui-new-table frame))
        (.pack)
        (.setVisible true))
      (add-ea-table-watch))
    (do (update-ea-table true) 
        (.setVisible (:frame ea-table-gui) true))))
 
(alter-var-root
 #'forex.interface.gui/gui-key-map
 merge {\v #'display-ea-table})

