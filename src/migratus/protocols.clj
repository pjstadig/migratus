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
(ns migratus.protocols
  (:refer-clojure :exclude [name]))

(defprotocol Migration
  (id [this] "Id of this migration.")
  (name [this] "Name of this migration")
  (up [this config] "Bring this migration up.")
  (down [this config] "Bring this migration down."))

(defprotocol Store
  (config [this])
  (init [this]
    "Initialize the data store.")
  (completed-ids [this]
    "Seq of ids of completed migrations.")
  (migrations [this]
    "Seq of migrations (completed or not).")
  (create [this name]
    "Create a new migration")
  (migrate-up [this migration]
    "Run and record an up migration")
  (migrate-down [this migration]
    "Run and record a down migration")
  (destroy [this name]
    "Destroy migration")
  (connect [this]
    "Opens resources necessary to run migrations against the store.")
  (disconnect [this]
    "Frees resources necessary to run migrations against the store."))

(defmulti make-store :store)
