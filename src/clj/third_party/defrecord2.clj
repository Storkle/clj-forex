
;;BY David McNeil
;;https://github.com/david-mcneil/defrecord2
;;modified defrecord2 macro so i could include protocols
;;got rid of print-method and pprint method 

(ns clj.third-party.defrecord2
  (:require [clojure.contrib.str-utils2 :as str2])
  (:use [clojure.contrib.core :only (seqable?)]
        [clojure.set :only (difference)]
        [clojure.string :only (join)]
        [clojure.contrib.pprint :only (*simple-dispatch* use-method pprint-map)])
  (:import [clojure.lang IPersistentList IPersistentVector IPersistentMap ISeq]))

;;;; enhanced records with constructor support (take in a hashmap) and print-dup support

;; internal helpers for name conversion

(defn- take-even [x]
  (take-nth 2 x))

(defn- take-odd [x]
  (take-nth 2 (drop 1 x)))

(defn- is-upper? [s]
  (= (.toUpperCase s) s))

(defn- assemble-words [parts]
  (loop [remaining-parts parts result []]
    (if (seq remaining-parts)
      (let [part (first remaining-parts)]
        (recur (rest remaining-parts)
               (if (is-upper? part)
                 (conj result (.toLowerCase part))
                 (conj (if (seq result)
                         (pop result)
                         []) (str (last result) part)))))
      result)))

(defn- camel-to-dashed
  "Convert a name like 'BigBlueCar' to 'big-blue-car'."
  [s]
  (let [parts (remove #(= "" %) (str2/partition s #"[A-Z]"))
        words (assemble-words parts)]
    (join "-" words)))

;; internal helpers for changing records via maps 

(defn set-record-field
  "Set a single field on a record."
  [source [key value]]
  (assoc source key value))

(defn set-record-fields
  "Set many fields on a record, from a map."
  [initial value-map]
  (reduce set-record-field initial value-map))

;; internal helper for generating constructor function

(defn expected-keys? [map expected-key-set]
  (not (seq (difference (set (keys map)) expected-key-set))))

(defmacro make-record-constructor
  "Define the constructor functions used to instantiate a record."
  [ctor-name type-name field-list default-record]
  `(defn ~ctor-name
     ([value-map#]
        (~ctor-name ~default-record value-map#))
     ([initial# value-map#]
        {:pre [(or (nil? initial#)
                   (isa? (class initial#) ~type-name))
               (map? value-map#)
               (expected-keys? value-map# ~(set (map keyword field-list)))]}
        (set-record-fields (if (nil? initial#) ~default-record initial#) value-map#)))) 

;; internal helpers for printing

(defn remove-nil-native-fields [native-keys record]
  (let [extra-keys (difference (set (keys record))
                               native-keys)]
    (apply array-map (reduce into (for [[k v] record]
                                    (if (or (contains? extra-keys k)
                                            (not (nil? v)))
                                      [k v]))))))

(defn- ns-resolve-symbol [s]
  (if-let [s (resolve s)]
    (.substring (str s) 2)
    (str s)))

(defmacro print-record
  "Low-level function to print a record to a stream using the specified constructor name in the print output and using the provided write-contents function to write out the contents of the record (represented as a map)."
  [ctor ctor-name native-keys record stream write-contents]
  `(do 
     (.write ~stream (str "#=(" ~(ns-resolve-symbol ctor-name) " "))
     (~write-contents (remove-nil-native-fields ~native-keys ~record))
     (.write ~stream  ")")))

(defn print-record-contents
  "Simply write the contents of a record to a stream as a string. Used for basic printing."
  [stream contents]
  (.write stream (str contents)))

(defmacro setup-print-record-method [ctor ctor-name native-keys type-name method-name]
  `(defmethod ~method-name ~type-name [record# writer#]
     (print-record ~ctor ~ctor-name ~native-keys record# writer# (partial print-record-contents writer#))))

(defmacro setup-print-record
  "Define the print methods to print a record nicely (so that records will print in a form that can be evaluated as itself)."
  [ctor ctor-name native-keys type-name]

  `(do ;(setup-print-record-method ~ctor ~ctor-name ~native-keys ~type-name print-method)
     (setup-print-record-method ~ctor ~ctor-name ~native-keys ~type-name print-dup)))

(defmacro generate-record-pprint
  "Return a function that can be used in the pprint dispatch mechanism to handle a specific constructor name."
  [ctor ctor-name native-keys]
  `(fn [record#]
     (print-record ~ctor ~ctor-name ~native-keys record# *out* pprint-map)))

;; internal helpers - walking data structures

;; w - walker function
;; f - mutator function
;; n - node in data tree being walked

;; helper - generating walking methods like this:
(comment (defmethod prewalk2 Foo [f foo]
           (if-let [foo2 (f foo)]
             (new-foo foo2 {:a (prewalk2 f (:a foo2))
                            :b (prewalk2 f (:b foo2))})))

         (defmethod postwalk2 Foo [f foo]
           (f (new-foo foo {:a (postwalk2 f (:a foo))
                            :b (postwalk2 f (:b foo))}))))

(defmulti walk2 (fn [w f n] (class n)))

(defmethod walk2 :default [w f n]
  n)

;; TODO: handle sets

(defmethod walk2 IPersistentVector [w f n]
  (apply vector (map (partial w f) n)))

(defmethod walk2 IPersistentMap [w f n]
  ;; TODO: handle sorted maps
  (apply array-map (mapcat (partial walk2 w f) n)))

(defmethod walk2 IPersistentList [w f n]
  (apply list (map (partial w f) n)))

(prefer-method walk2 IPersistentList ISeq)

(defmethod walk2 ISeq [w f n]
  (map (partial w f) n))

(defmacro walking-helper-field
  ([w f n field]
     `[~(keyword field) (~w ~f (~(keyword field) ~n))])
  ([w f n field & more]
     `(concat (walking-helper-field ~w ~f ~n ~field) (walking-helper-field ~w ~f ~n ~@more))))

(defmacro walking-helper-fields
  [w f n fields]
  `(apply array-map (walking-helper-field ~w ~f ~n ~@fields)))

(defmacro make-prewalk2-method
  "Define the methods used to walk data structures."
  [ctor-name type-name field-list]
  `(defmethod prewalk2 ~type-name [f# n#]
     (if-let [n2# (f# n#)]
       (~ctor-name n2# (walking-helper-fields prewalk2 f# n2# ~field-list)))))

(defmacro make-postwalk2-method
  "Define the methods used to walk data structures."
  [ctor-name type-name field-list]
  `(defmethod postwalk2 ~type-name [f# n#]
     (f# (~ctor-name n# (walking-helper-fields postwalk2 f# n# ~field-list)))))

;; public entry points

(defmulti prewalk2 (fn [f n] (class n)))

(defmethod prewalk2 :default [f n]
  (walk2 prewalk2 f (f n)))

(defmulti postwalk2 (fn [f n] (class n)))

(defmethod postwalk2 :default [f n]
  (f (walk2 postwalk2 f n)))

(defmacro defrecord2
  "Defines a record and sets up constructor functions, printing, and pprinting for the new record type."
  [type field-list & protocols]
  (let [type-name (if (seqable? type) (first type) type)
        ctor-name (if (seqable? type)
                    (second type)
                    (symbol (str "new-" (camel-to-dashed (str type)))))]
    `(do 
       ;; define the record
       (defrecord ~type-name ~field-list ~@protocols)
       ;; define the constructor functions
       (make-record-constructor ~ctor-name
                                ~type-name
                                ~field-list
                                (~(symbol (str type-name ".")) ~@(repeat (count field-list) nil)))
       ;; setup tree walking methods
       (make-prewalk2-method ~ctor-name ~type-name ~field-list)
       (make-postwalk2-method ~ctor-name ~type-name ~field-list)

       ;; setup printing
       (let [empty-record# (~ctor-name {})
             native-keys# (set (keys empty-record#))]
         (setup-print-record ~ctor-name ~ctor-name native-keys# ~type-name)
         ;; setup pprinting
         (comment
           (use-method *simple-dispatch*
                       ~type-name

                       (generate-record-pprint ~ctor-name
                                               ~ctor-name
                                               native-keys#)))))))


