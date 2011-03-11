
;;(clojure.core/use 'nstools.ns)
(ns forex.interface.gui
  (:require swank.swank)  
  (:use
   forex.util.general
   forex.util.log
   forex.module.ea forex.util.general
   clojure.contrib.miglayout forex.util.emacs 
   forex.util.gui forex.interface.tray)
  (:import 
   (java.awt Dimension Font event.MouseListener
             event.MouseAdapter Toolkit event.ActionListener event.KeyListener)
   (javax.swing JComboBox JPopupMenu JMenuItem
                ImageIcon SwingConstants
                table.AbstractTableModel 
                JScrollPane JTable
                JFrame JPanel JTextField JLabel JButton JFileChooser
                SwingUtilities)))
;;TODO: all action listeners should be vars for maximum flexibility - because sometimes it all goes away!

;;Useful User Vars
(defonce gui nil) 
(defn message [msg & args]
  (when-let [it (:status gui)]
    (.setText it (apply format msg args))))

;;LOADING EAS
(defonce prev-load-file (atom nil))
(defn on-load-eas []
  (let [fileopen (if @prev-load-file (JFileChooser. @prev-load-file) (JFileChooser.))
        ret (.showDialog fileopen nil "Load")] 
    (if (= ret JFileChooser/APPROVE_OPTION)
      (let [file (.getSelectedFile fileopen)]
        (with-out-str+ out (if-let [it (load-eas file true)] (message "loaded %s eas" it) (inform (str out))))
        (reset! prev-load-file (.getParentFile file))))))

;;SAVING EAS
(defonce prev-save-file (atom nil))
(defn on-save-eas []
  (message "saving ...")
  (let [fileopen
        (if @prev-save-file (JFileChooser. @prev-save-file) (JFileChooser.))
        ret (.showDialog fileopen nil "Save")]
    (if (= ret JFileChooser/APPROVE_OPTION)
      (let [file (.getSelectedFile fileopen)]
        (reset! prev-save-file (.getParentFile file))
        (if-let [it (save-eas file)]
          (message "saved %s eas" it)
          (inform "unable to acquire ea write lock in order to save all eas")))))) 
(defn on-log []
  (message "open log file @ %s/.forex/" (System/getProperty "user.home"))) 
;;User Customization
(defhook gui-on-exit-hooks) 
(defhook gui-create-pre-hook)
(defhook gui-create-post-hook)
(defvar swank-port 4005) 
(defvar gui-image "pictures/clojure.gif")
(defvar gui-icon-image "pictures/clojure.gif")
(defvar gui-window-image "pictures/clojure.gif")

(defvar gui-key-map 
  {\l #'on-log  
   \s 
   #(do
      (swank.swank/start-repl swank-port)
      (message "started swank on port %s" swank-port))})
(defn on-restart-eas []
  (dorun (map restart @*eas*))
  (message "restarted %s eas" (count @*eas*))) 
(defvar
  gui-menu-items
  [{:name "new ea" :key \n}
   {:name "save eas" :action #'on-save-eas}
   {:name "load eas" :action #'on-load-eas}
   {:name "view eas" :key \v} 
   {:name "log" :key \l :action #'on-log}
   {:name "start swank" :key \s}
   {:name "restart eas" :key \r :action #'on-restart-eas} 
   {:name "load forex-init" 
    :action #(try       
               (message "loading ...")
               (require :reload 'forex-init)
               (message "loaded forex-init")
               (catch Exception e
                 (message "failed to load forex-init")
                 (inform "failed to load forex-init %s" e)))}
   {:name "preferences" :key \p}])
(defvar gui-frame-title "clj-forex")
(defvar gui-reminder-visible true) 
;;;;
;;;
(defn jframe [title]
  (doto
      (JFrame. title)
    (.setIconImage (create-image gui-window-image))))

(defn- get-fn [a] (if (var? a) (var-get a) a))
(defn on-key [e] 
  (when-let [it (get-fn (get gui-key-map (.getKeyChar e)))]
    (try (when (fn? it) (it))
         (catch Exception e
           (.printStackTrace e)
           (severe e))))) 
(defn add-menu-item [parent {:keys [name key action]}]
  (let [item (JMenuItem. (str name " (" key ")"))]
    (add-action-listener 
     item (or (and (not action) (bound-fn
                                 [e]
                                 (let [key-map-fn (get-fn (get gui-key-map key))]
                                   (when (fn? key-map-fn) (key-map-fn)))))
              (bound-fn [e] (thread (action))))) ;;no e
    (.add parent item)
    item)) 
(defn on-popup [e] 
  (try
    (let [menu (JPopupMenu.)]
      (dorun (map #(add-menu-item menu %) gui-menu-items))
      (.show menu
             (.getComponent e)
             (.getX e) (.getY e)))
    (catch Exception e (println e))))
(defn on-exit [icon] 
  (if (empty? gui-on-exit-hooks)
    (remove-system-icon icon)
    (run-hook-with-args gui-on-exit-hooks icon)))
(defn on-click [icon]
  (.setVisible (:frame gui) true))
(defn gui-make-icon []
  (system-icon
   {:image gui-icon-image
    :on-click  on-click 
    :on-exit on-exit}))

(defn new-gui []
  (alter-var-root #'gui (constantly nil))
  (run-hooks gui-create-pre-hook)
  (let [icon (gui-make-icon)
        image (if (string? gui-image) (create-icon gui-image))
        panel
        (doto (miglayout  
               (JPanel.)  :column "center" 
               (JLabel. (if image image ""))  "width 245!" "span,growx,pushx" :wrap 
               (JComboBox. (into-array String @*ea-registry*))
               {:id :selector} "span,growx,pushx"  :wrap
               (doto (JLabel. "<html>n = new ea<br>v = view eas<br>l = log<br>s = start swank<html>") (.setHorizontalAlignment SwingConstants/CENTER))
               
               {:id :reminder} :wrap
               (doto (JLabel. "")
                 (.setFont (Font. "SansSerif" Font/ITALIC 12)))
               {:id :status} "h 20!" "growx,align left,span"))  
        frame (doto (jframe gui-frame-title)
                (.setDefaultCloseOperation JFrame/HIDE_ON_CLOSE) 
                (.add panel)
                (.pack)) 
        {:keys [selector reminder view]} (components panel)] 
    (when-not gui-reminder-visible (.setVisible reminder false))
    ;; (add-action-listener selector (bound-fn* on-selector))
    (dorun (map (fn [obj]
                  (add-key-listener obj #'on-key)
                  (add-popup-listener obj #'on-popup))
                (conj (vals (components panel)) panel)))
    (alter-var-root  #'gui (constantly (merge (components panel) {:frame frame :panel panel :icon icon})))
    (run-hooks gui-create-post-hook)
    frame))


(defn add-ea-registry-watch []
  (add-watcher
   *ea-registry* "registry-add"
   (fn [key ref old new] 
     (when-let [it (:selector gui)]
       (.removeAllItems it)
       (dorun (map #(.addItem it %) new))))))

(defn invoke-new-gui
  ([] (invoke-new-gui ""))
  ([msg] 
     (invoke-later
      (when-let [it (:icon gui)]
        (remove-system-icon it))
      (when-let [it (:frame gui)]
        (.setVisible it false))
      (new-gui)
      (.setVisible (:frame gui) true) 
      (add-system-icon (:icon gui)) 
      (when msg (message msg))
      (add-ea-registry-watch))))

