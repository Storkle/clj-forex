(ns utils.general)
(def *fake* (gensym))
(defmacro defrecord+ 
  [record-name fields-and-values constructor-name & record-body] 
  (let [fields-and-values (map #(if (vector? %) % [% nil])
			       fields-and-values) 
        fields            (vec (map first fields-and-values)) 
        default-map       (into {} fields-and-values)
	fn-name (symbol (or (and constructor-name (str constructor-name))
			    (str "new-" (name record-name))))] 
    `(do 
       (defrecord ~record-name 
	   ~fields 
         ~@record-body) 
       (defn ~fn-name
	 ([] (~fn-name *fake* nil))
         ([& {:keys ~fields :or ~default-map}] 
	    (new ~record-name ~@fields))))))
