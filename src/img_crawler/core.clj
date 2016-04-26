(ns img-crawler.core
  (:gen-class)
  (:require [pl.danieljanus.tagsoup :refer [parse-string tag attributes children]])
  (:require [clj-http.client :as client])
  (:require [clojure.string :refer [starts-with? ends-with? split]])
  (:require [clojure.set :refer [subset? difference]])
  (:require [clojure.java.io :refer [as-file as-url]]
            [clojure.java.io :as io])
  (:import (java.io IOException)
           (java.net URL)))

(defn get-url-contents [url]
  (let [h {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}]
    (if-let [response (try
                        (client/get url {:headers h})
                        (catch IOException _ nil))]
      (get response :body))))

(defn process-node
  ([node]
   (process-node node [] []))
  ([node found-images found-pages]
   (let [tag (tag node)
         attrs (attributes node)
         children (filter vector? (children node))]
     (if (empty? children)
       (case tag
         :img [(conj found-images (:src attrs)) found-pages]
         :a [found-images (conj found-pages (:href attrs))]
         [found-images found-pages])
       (let [[new-images new-pages] (process-node [tag attrs nil] found-images found-pages)]
         (loop [images new-images
                pages new-pages
                children children]
           (if-let [child (first children)]
             (let [[new-images new-pages] (process-node child images pages)]
               (recur new-images new-pages (rest children)))
             [images pages])))))))

(defn filter-pages [url pages]
  (let [current-page (last (split url #"/"))
        pred (every-pred #(not= (last (split % #"/")) current-page) #(not (starts-with? % "/#")) #(not (re-find #"#" %))
                         #(not (re-find #"(^www)|(^http)" %)) #(re-matches #"/?[a-zA-Z_%\d\+\-]+/?" %))]
    (filter pred (filter (every-pred #(not= "/" %) #(not (nil? %))) pages))))

(defn process-page [url]
  (println (str "Processing page " url))
  (if-let [data (get-url-contents url)]
    (let [node (parse-string data)
          [images pages] (process-node node)]
      ;(println (filter-pages url pages))
      (loop [processed-pages #{}
             found-pages (into #{} (conj (filter-pages url pages) url))
             found-images (into #{} images)]
        ;(println "----")
        ;(println found-pages)
        ;(println processed-pages)
        ;(println "----")
        (if (subset? found-pages processed-pages)
          (into [] (flatten (seq found-images)))
          (let [hostname (-> (URL. url) (.getHost))
                protocol (-> (URL. url) (.getProtocol))
                diff (difference found-pages processed-pages)
                current-page (first diff)
                [new-images new-pages] (process-node (try (-> (str protocol "://" hostname current-page) get-url-contents parse-string) (catch Exception _ nil) ))]
            (recur (conj processed-pages current-page) (into found-pages (filter-pages url new-pages)) (conj found-images new-images))))))))

(defn ensure-dir [path]
  (let [file (as-file path)]
    (when (.exists file)
      (when (.isFile file) false))
    (.mkdirs file)))

(defn basename [path]
  (-> (as-file path) (.getName)))

(defn dirname [path]
  (-> (as-file path) (.getParent)))

(defn start [url target-dir]
  ;; 1. Ensure target-dir exists.
  ;; 2. Get a list of images.
  ;; 3. Download every image from the list to the target-dir.
  (if (ensure-dir target-dir)
    (let [links (process-page url)
          local-paths (map #(str target-dir %) links)
          remote-paths (map #(str url %) links)]
      (println remote-paths)
      ;(map (fn [local-path remote-path] (let [content (slurp remote-path)] (ensure-dir (dirname local-path)) (spit local-path content))) local-paths remote-paths))))
      (map
        (fn [local-path remote-path]
          (ensure-dir (dirname local-path))
          (with-open [in (io/input-stream remote-path)
                      out (io/output-stream local-path)]
            (io/copy in out))) local-paths remote-paths))))

(defn -main
  [& args]
  (if (not= 2 (count args))
    (do
      (println "Usage: java -jar img-crawler.jar <url> <target_dir>")
      (System/exit 1))
    (let [url (first args)
          target-dir (second args)]
      (start url target-dir))))
