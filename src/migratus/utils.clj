(ns migratus.utils
  (:import java.io.File
           java.util.jar.JarFile
           [java.net URL URLDecoder])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(def default-migration-parent "resources/")
(def default-migration-dir "migrations")
(def default-init-script-name "init.sql")

(defn get-parent-migration-dir
  "Gets the :parent-migration-dir from config, or default if missing."
  [config]
  (get config :parent-migration-dir default-migration-parent))

(defn get-migration-dir
  "Gets the :migration-dir from config, or default if missing."
  [config]
  (get config :migration-dir default-migration-dir))

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

(defn jar-file [^URL url]
  (some-> url
          (.getFile)
          (URLDecoder/decode "UTF-8")
          (.split "!/")
          ^String (first)
          (.replaceFirst "file:" "")
          (JarFile.)))

(defn find-migration-dir
  "Finds the given directory on the classpath. For backward
  compatibility, tries the System ClassLoader first, but falls back to
  using the Context ClassLoader like Clojure's compiler.
  If classloaders return nothing try to find it on a filesystem."
  ([^String dir]
   (or (find-migration-dir (ClassLoader/getSystemClassLoader) default-migration-parent dir)
       (-> (Thread/currentThread)
           (.getContextClassLoader)
           (find-migration-dir default-migration-parent dir))))
  ([^ClassLoader class-loader ^String parent-dir ^String dir]
   (if-let [^URL url (.getResource class-loader dir)]
     (if (= "jar" (.getProtocol url))
       (jar-file url)
       (File. (URLDecoder/decode (.getFile url) "UTF-8")))
     (let [migration-dir (io/file parent-dir dir)]
       (if (.exists migration-dir)
         migration-dir
         (let [no-implicit-parent-dir (io/file dir)]
           (when (.exists no-implicit-parent-dir)
             no-implicit-parent-dir)))))))

(defn deep-merge
  "Merge keys at all nested levels of the maps."
  [& maps]
  (apply merge-with deep-merge maps))

(defn recursive-delete
  "Delete a file, including all children if it's a directory"
  [^File f]
  (when (.exists f)
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (recursive-delete child))
      (.delete f))))

(defn normalize-path
  "Replace backslashes with forwardslashes"
  [^String s]
  (str/replace s "\\" "/"))
