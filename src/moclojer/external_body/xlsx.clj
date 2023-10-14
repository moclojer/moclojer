(ns moclojer.external-body.xlsx
  (:require [dk.ative.docjure.spreadsheet :as sheet]))

(defn header-keywordize
  "Converts header to keywords."
  [[header-row & rows]]
  (let [header-keys (map keyword header-row)]
    (map (fn [row] (zipmap header-keys row)) rows)))

(defn ->map
  "converts xlsx to map, sheet-name is optional"
  [path sheet-name]
  (->> (sheet/load-workbook path)
       (sheet/select-sheet sheet-name)
       sheet/row-seq
         ;; (remove nil?)
       (map #(map sheet/read-cell %))
       header-keywordize))

