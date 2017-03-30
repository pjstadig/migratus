(ns migratus.utils
  (:require [clojure.java.classpath :as cp]
            [clojure.java.io :as io])
  (:import java.io.File
           [java.util.jar JarEntry JarFile]
           java.util.regex.Pattern))

(def default-init-script-name "init.sql")

(defn get-init-script
  "Gets the :init-script from config, or default if missing."
  [config]
  (get config :init-script default-init-script-name))

(defn get-exclude-scripts
  "Returns a set of script names to exclude when finding migrations"
  [config]
  (into #{(get-init-script config)}
        (get config :exclude-scripts)))

(defn ensure-trailing-slash
  "Put a trailing slash on the dirname if not present"
  [dir]
  (if (not= (last dir) \/)
    (str dir "/")
    dir))

(defn find-migration-dir
  "Finds the given directory on the classpath"
  [dir]
  (->> (cp/classpath-directories)
       (map #(io/file % dir))
       (filter #(.exists ^File %))
       first))

(defn find-migration-jar
  "Finds the first jar on the classpath containing a directory with the given
  name."
  [dir]
  (first (for [jar (cp/classpath-jarfiles)
               :when (some #(.matches (.getName ^JarEntry %)
                                      (str "^" (Pattern/quote dir) ".+"))
                           (enumeration-seq (.entries ^JarFile jar)))]
           jar)))

(defn deep-merge
  [& maps]
  (apply merge-with deep-merge maps))