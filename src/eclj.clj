(ns eclj
  (:require [clojure.string :as s]
            [slingshot.slingshot :refer [throw+]]))

(defn has-more? [r]
  (.mark r 1)
  (let [more? (pos? (.read r))]
    (.reset r)
    more?))

(defn read-until [r marker]
  ;; Returns the string before the next match. The reader will
  ;; pointing at either the character after the marker or at EOF.
  
  (loop [before ""]
    (if-not (has-more? r)
      before
      
      (let [ch (char (.read r))]
        (if (= ch (first marker))
          (let [marker-tail (rest marker)
                _ (.mark r (count marker-tail))
                next-chs (->> (repeatedly (count marker-tail) #(.read r))
                              (map char))]
            (if (= next-chs marker-tail)
              ;; match!
              before

              ;; no match. reset the look-ahead and push the ch
              (do
                (.reset r)
                (recur (str before ch)))))

          (recur (str before ch)))))))

(defn eval-eclj-form [form params]
  (let [my-ns (create-ns (symbol (gensym "eclj")))]
    (doseq [[k v] params]
      (intern my-ns (symbol (name k)) v))
    (binding [*ns* my-ns]
      (refer-clojure)
      (eval form))))

(defn eval-eclj [template params]
  (let [expr-marker "#="]
    (with-open [r (java.io.LineNumberReader.
                   (java.io.StringReader. template))]
      (loop [res ""]
        (let [before (read-until r expr-marker)]
          (if-not (has-more? r)
            (str res before)

            (let [line (inc (:lineNumber (bean r)))]
              (recur
               (try
                 (str res before (eval-eclj-form (read (java.io.PushbackReader. r)) params))
                 (catch Exception e
                   (throw+ {:error e :line line})))))))))))

(defn make-safe [s]
  ;; we ensure that any #= simply executes a form that returns "#="
  (s/replace s #"#=" "#=\"#=\""))

(comment
  (eval-eclj (s/join "\n"
                     [""
                      "#=(str first-name \" \" last-name) says that, "
                      "at '#=(str (java.util.Date.))', "
                      "it's probably time for a coffee!"])
             {:first-name "Joe" :last-name "Bloggs"}))
