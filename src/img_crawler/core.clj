(ns img-crawler.core
  (:gen-class)
  (:require [pl.danieljanus.tagsoup :refer [parse-string tag attributes children]])
  (:require [clj-http.client :as client])
  (:require [clojure.string :refer [starts-with? ends-with? split]])
  (:import (java.io IOException)))

(defn get-url-contents [url]
  (let [h {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}]
    (if-let [response (try
                        (client/get url {:headers h})
                        (catch IOException _ nil))]
      (get response :body))))

(defn process-node
  ([node]
   (process-node node [] []))
  ([node images pages]
   (let [tag (tag node)
         attrs (attributes node)
         children (filter vector? (children node))]
     (if (empty? children)
       (case tag
         :img [(conj images (:src attrs)) pages]
         :a [images (conj pages (:href attrs))]
         [images pages])
       (let [[new-images new-pages] (process-node [tag attrs nil] images pages)]
         (loop [images new-images
                pages new-pages
                children children]
           (if-let [child (first children)]
             (let [[new-images new-pages] (process-node child images pages)]
               (recur new-images new-pages (rest children)))
             [images pages])))))))

(defn filter-pages [url pages]
  (let [current-page (last (split url #"/"))
        pred (every-pred #(not= (last (split % #"/")) current-page) #(not (starts-with? % "/#")) #(not (re-find #"#" %)))]
    (filter pred (filter (every-pred #(not= "/" %) #(not (nil? %))) pages))))

(defn process-page [url]
  (println (str "Processing page " url))
  (if-let [data (get-url-contents url)]
    (let [node (parse-string data)
          [images pages] (process-node node)]
      (println (filter-pages url pages))
      (if-not pages
        images
        (concat images (mapcat process-page (map #(str url %) (filter-pages url pages))))))))

(defn start [url target-dir]
  ;; 1. Ensure target-dir exists.
  ;; 2. Get a list of images.
  ;; 3. Download every image from the list to the target-dir.
  )

(defn -main
  [& args]
  (if (not= 2 (count args))
    (do
      (println "Usage: java -jar img-crawler.jar <url> <target_dir>")
      (System/exit 1))
    (let [url (first args)
          target-dir (second args)]
      (start url target-dir))))
