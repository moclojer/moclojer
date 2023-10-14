(ns moclojer.external-body.xlsx
  (:require [dk.ative.docjure.spreadsheet :as sheet]))

(defn header-keywordize
  "converts header to keywords"
  [data]
  (let [header (-> data first vec)]
    (map (fn [entry]
           (let [values (rest entry)]
             (zipmap (map keyword header)
                     (cons (first entry) values))))
         (rest data))))

(defn ->map
  "converts xlsx to map, sheet-name is optional"
  [path sheet-name]
  (->> (sheet/load-workbook path)
       (sheet/select-sheet sheet-name)
       sheet/row-seq
         ;; (remove nil?)
       (map #(map sheet/read-cell %))
       header-keywordize))

