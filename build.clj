(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]))

(def lib 'org.fudo/notifier)
(def default-version "DEV")
(defn- class-dir [{:keys [target]}] (format "%s/classes" target))
(def basis (b/create-basis {:project "deps.edn"}))
(defn- jar-file [{:keys [target version]
                  :or   {version default-version}}]
  (format "%s/%s-%s.jar" target (name lib) version))

(defn- uberjar-file [{:keys [target version]
                      :or   {version default-version}}]
  (format "%s/%s-uber-%s.jar" target (name lib) version))

(def default-params
  {
   :verbose false
   :version "DEV"
   })

(defn clean [{:keys [target] :as params}]
  (b/delete {:path target})
  params)

(defn compile-java [{:keys [verbose java-src] :as params}]
  (when verbose (println (format "compiling java files in %s..." java-src)))
  (b/javac {:src-dirs   [java-src]
            :class-dir  (class-dir params)
            :basis      basis
            :javac-opts ["-source" "16" "-target" "16"]})
  params)

(defn compile-clj [{:keys [verbose clj-src] :as params}]
  (when verbose (println (format "compiling clj files in %s..." clj-src)))
  (b/compile-clj {:basis     basis
                  :src-dirs  [clj-src]
                  :class-dir (class-dir params)}))

(defn- read-metadata [filename]
  (-> filename
      (slurp)
      (edn/read-string)))

(defn- process-params [base-params]
  (-> base-params
      (merge (read-metadata (or (:metadata base-params)
                                "metadata.edn")))
      (update :target str)
      (update :version str)
      (update :java-src str)
      (update :clj-src str)))

(defn jar [base-params]
  (let [params (process-params base-params)
        {:keys [java-src clj-src version verbose]} params
        classes (class-dir params)]
    (when verbose
      (print "parameters: ")
      (pprint params))
    (compile-java params)
    (compile-clj params)
    (when verbose (println (format "writing POM file to %s..." classes)))
    (b/write-pom {
                  :class-dir classes
                  :lib       lib
                  :version   (str version)
                  :basis     basis
                  :src-dirs  [java-src clj-src]
                  })
    (when verbose (println (format "copying source files from %s to %s..."
                                   [java-src clj-src] classes)))
    (b/copy-dir {:src-dirs [java-src clj-src]
                 :target-dir classes})
    (let [jar (jar-file params)]
      (when verbose (println (format "writing JAR file to %s..." jar)))
      (b/jar {:class-dir classes
              :jar-file  jar}))
    (when verbose (println "done!"))
    params))

(defn uberjar [base-params]
  (let [params (process-params base-params)
        {:keys [java-src clj-src version verbose]} params
        classes (class-dir params)]
    (when verbose
      (print "parameters: ")
      (pprint params))
    (compile-java params)
    (compile-clj params)
    (when verbose (println (format "writing POM file to %s..." classes)))
    (b/write-pom {
                  :class-dir classes
                  :lib       lib
                  :version   (str version)
                  :basis     basis
                  :src-dirs  [java-src clj-src]
                  })
    (when verbose (println (format "copying source files from %s to %s..."
                                   [java-src clj-src] classes)))
    (b/copy-dir {:src-dirs [java-src clj-src]
                 :target-dir classes})
    (let [uberjar (uberjar-file params)]
      (when verbose (println (format "writing uberjar file to %s..." uberjar)))
      (b/uber {:class-dir  classes
               :uber-file  uberjar
               :basis      basis}))
    (when verbose (println "done!"))
    params))

(defn install [base-params]
  (let [params (process-params base-params)
        {:keys [version verbose]} params
        target-file (jar-file params)]
    (jar params)
    (when verbose (println (format "installing %s..." target-file)))
    (b/install {
                :basis      basis
                :lib        lib
                :version    (str version)
                :jar-file   target-file
                :class-dir  (class-dir params)
                })
    params))