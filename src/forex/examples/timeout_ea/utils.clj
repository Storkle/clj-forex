
(clojure.core/use 'nstools.ns)
(ns+ forex.examples.timeout-ea.utils
     (:clone forex.default)
     (:use forex.util.general)) 

(defn price
  ([val] (price val (env :symbol)))
  ([val symbol] 
     (* (mode-tickvalue symbol) (point val)))) 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def method-regex
  (re-pattern (.replaceAll
               (str "(?i)\\s*(Propulsion|Pip Reactor|Impulse|Spring)\\s+Method"
                    ".+running on (\\w+/\\w+)"
                    ".+generated a (Buy|Sell) Signal @ (\\d{0,15}\\.\\d{0,15})"
                    ".+Stop @ (\\d{0,15}\\.\\d{0,15})"
                    ".+(?:1st|First) Limit @ (\\d{0,15}\\.\\d{0,15})"
                    ".+(?:2nd|Second) Limit @ (\\d{0,15}\\.\\d{0,15})") 
               "\\s+" "\\\\s+")))

(defmacro catch-un [& body]
  `(try (do ~@body) (catch Exception e#
		      (.printStackTrace e#) (warn "caught unexpected error: %s" e#))))

(defn match-method [risk-percent s]
  (when s 
    (debugging "Matching Profit Multiplier Trade: "
               (catch-un 
                (when-let [it (first (re-seq method-regex (.replaceAll s "[\\r\\n]+" " ")))]
                  (let [[method-type symbol type price stop tp1 tp2] (rest it)
                        method (.toLowerCase method-type)
			symbol (.replaceAll symbol "/" "")]
                    {:method method
                     :hour (if (= method "pip reactor") 4 12) 
                     :symbol symbol
                     :type (condp = (.toLowerCase type)
                               "buy" :buy-stop
                               "sell" :sell-stop)
                     :price (Double/parseDouble price)
                     :sl (Double/parseDouble stop)
		     :lots (risk risk-percent
				 (Double/parseDouble stop)
				 (Double/parseDouble price)
				 symbol)
		     :period +h4+
                     :tp1 (Double/parseDouble tp1)
                     :tp2 (Double/parseDouble tp2)}))))))


