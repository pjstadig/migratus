;;;; Copyright © 2011 Paul Stadig
;;;;
;;;; Licensed under the Apache License, Version 2.0 (the "License"); you may not
;;;; use this file except in compliance with the License.  You may obtain a copy
;;;; of the License at
;;;;
;;;;   http://www.apache.org/licenses/LICENSE-2.0
;;;;
;;;; Unless required by applicable law or agreed to in writing, software
;;;; distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
;;;; WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
;;;; License for the specific language governing permissions and limitations
;;;; under the License.
(ns leiningen.migratus
  (:require [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as logi]
            [leiningen.core.eval :as eval])
  (:import (java.util.logging Logger Level)))

(defn migratus
  "MIGRATE ALL THE THINGS!

Run migrations against a store.  The :migratus key in project.clj is passed to
migratus as configuration.

Usage `lein migratus [command & ids]`.  Where 'command' is:

migrate  Bring up any migrations that are not completed.
up       Bring up the migrations specified by their ids.  Skips any migrations
         that are already up.
down     Bring down the migrations specified by their ids.  Skips any migrations
         that are already down.

If you run `lein migrate` without specifying a command, then the 'migrate'
command will be executed."
  [project & [command & ids]]
  (if (= "java.util.logging" (logi/name log/*logger-factory*))
    (.setLevel (Logger/getLogger "") Level/SEVERE))
  (let [updated-project (update-in project [:dependencies]
                                   conj ['migratus "0.4.1"])]
    (if-let [config (:migratus project)]
      (let [config (assoc config :store :cli :real-store (:store config))]
        (case command
          "up" (eval/eval-in-project updated-project
                                     `(apply migratus.core/up ~config ~(map #(Long/parseLong %) ids))
                                     '(require 'migratus.core))
          "down" (eval/eval-in-project updated-project
                                       `(apply migratus.core/down ~config ~(map #(Long/parseLong %) ids))
                                       '(require 'migratus.core))
          (if (and (or (= command "migrate") (nil? command)) (empty? ids))
            (eval/eval-in-project updated-project
                                  `(migratus.core/migrate ~config)
                                  '(require 'migratus.core))
            (println "Unexpected arguments to 'migrate'"))))
      (println "Missing :migratus config in project.clj"))))
