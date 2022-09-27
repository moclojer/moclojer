(ns moclojer.adapters-test
  (:require [clojure.test :refer [are deftest testing]]
            [moclojer.adapters :as adapters]))

(deftest inputs-config-test
  (testing "inputs->config can read data from all data sources"
    (are [result inputs] (= result inputs)

      {:current-version "dev"
       :config "config.yaml"
       :mocks "mocks.yaml"
       :version true
       :help true}
      (adapters/inputs->config {:args [{:config "config.yaml"
                                        :mocks "mocks.yaml"
                                        :version true
                                        :help true}]
                                :opts {:config "opts-config.yaml"
                                       :mocks "opts-mocks.yaml"}}
                               {:config "default-config.yaml"
                                :mocks "default-mocks.yaml"}
                               "dev")

      {:current-version "dev"
       :config "opts-config.yaml"
       :mocks "opts-mocks.yaml"
       :version true
       :help true}
      (adapters/inputs->config {:args []
                                :opts {:config "opts-config.yaml"
                                       :mocks "opts-mocks.yaml"
                                       :version true
                                       :help true}}
                               {}
                               "dev")

      {:current-version "dev"
       :config "env-config.yaml"
       :mocks "env-mocks.yaml"
       :version true
       :help true}
      (adapters/inputs->config {:args [{:version true
                                        :help true}]
                                :opts {:config "opts-config.yaml"
                                       :mocks "opts-mocks.yaml"}}
                               {:config "env-config.yaml"
                                :mocks "env-mocks.yaml"}
                               "dev")

      {:current-version "dev"
       :config "env-config.yaml"
       :mocks "env-mocks.yaml"
       :version true
       :help true}
      (adapters/inputs->config {:args [{:version true
                                        :help true}]}
                               {:config "env-config.yaml"
                                :mocks "env-mocks.yaml"}
                               "dev"))))
