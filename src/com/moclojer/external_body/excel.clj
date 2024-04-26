(ns com.moclojer.external-body.excel
  (:require
   [bb-excel.core :as excel]))

(defn ->map
  "converts excel (xlsx and xls) tab/sheet name to map"
  [path sheet-name]
  (map #(dissoc % :_r)
       (excel/get-sheet
        path sheet-name {:hdr true :fxn keyword})))
