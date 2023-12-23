(ns com.moclojer.watcher-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.watcher :as watcher]))

(deftest watcher-test-add-path
  (testing "add path parent to specs"
    (let [specs [{:file "/Users/matheus.machado/.config/moclojer.yaml"
                  :event-types [:create :modify :delete]
                  :bootstrap (fn [path] (prn "Starting to watch " path))
                  :callback (fn [event filename] (prn event filename))}]
          specs-with-path (watcher/add-path specs)]

      (is (= (:path (first specs-with-path))
             "/Users/matheus.machado/.config")))
    (testing "add path into multiple specs"
      (let [specs [{:file "/Users/matheus.machado/.config/moclojer.yaml"
                    :event-types [:create :modify :delete]
                    :bootstrap (fn [])
                    :callback (fn [])}
                   {:file "/Users/avelino/.config/moclojer.yaml"
                    :event-types [:create :modify :delete]
                    :bootstrap (fn [])
                    :callback (fn [])}]
            specs-with-path (watcher/add-path specs)]

        (is (= (->> specs-with-path
                    (map :path)
                    sort)
               ["/Users/avelino/.config"
                "/Users/matheus.machado/.config"]))))))

(deftest watcher-filter-not-nil
  (testing "filter nil file path"
    (is (= [{:file "path"}]
           (watcher/filter-nil-spec [{:file nil}
                                     {:file "path"}])))))

(deftest watcher-start-edit-file-test
  (testing "steup functions to create and edit file, "
    (let [should-edit-when-file-change (atom nil)
          edit-file (fn [path value]
                      (spit path value))
          create-file (fn [path]
                        (spit path "test"))
          delete-file (fn [path]
                        (io/delete-file path))]

      (testing "creating a file with content test"
        (create-file "test/com/moclojer/resources/tmp.clj")

        (testing "starting the watchers and editing the file"

          (let [specs [{:file "test/com/moclojer/resources/tmp.clj"
                        :event-types [:create :modify :delete]
                        :callback (fn [_ filename]
                                    (reset! should-edit-when-file-change {:filename filename}))}]
                stop-watcher (watcher/start-watch specs)]

            (edit-file "test/com/moclojer/resources/tmp.clj" "test1")
           ;;waiting to callback be trigger
            (Thread/sleep 5000)

            (is (= (:filename @should-edit-when-file-change)
                   "test/com/moclojer/resources/tmp.clj"))

            (is (= (slurp "test/com/moclojer/resources/tmp.clj")
                   "test1"))

            (testing "stop the watchers and cleaning the atom"
              (reset! should-edit-when-file-change nil)
              (stop-watcher)
              (edit-file "test/com/moclojer/resources/tmp.clj" "test2")
              ;;waiting to see that callback will be not trigged
              (Thread/sleep 5000)
              (is (= (:filename @should-edit-when-file-change)
                     nil))
              (delete-file "test/com/moclojer/resources/tmp.clj"))))))))
