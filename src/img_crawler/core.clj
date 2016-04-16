(ns img-crawler.core
  (:require [pl.danieljanus.tagsoup :refer [parse-string tag attributes children]])
  (:require [clj-http.client :as client])
  (:gen-class))

(defn img-tag? [tag]
  (= :img tag))

(defn src-attribute [attributes]
  (when (map? attributes)
    (:src attributes)))

(defn get-src-if-img [tag attributes]
  (when (img-tag? tag)
    (src-attribute attributes)))

;; In case I want to actually crawl the page, I need to extract <a>
;; tags as well... I think, I should rework this function. I want to
;; have something like this: {:hrefs [...] :img-srcs [...]}

(defn imgs-src
  "Takes a page's DOM, finds all <img> tags and returns
  a list of their src attributes values."
  [dom]
  (let [tag (tag dom)
        attrs (attributes dom)
        children (children dom)]
    (if (or (not children) (string? children))
      (conj [] (get-src-if-img tag attrs))
      (filter some? (concat (conj [] (get-src-if-img tag attrs)) (mapcat imgs-src (filter vector? children)))))))

(defn extract-src-or-href
  "Depending on the tag attribute value extracts src attribute (for
  <img>) or href (for <a>)."
  [tag attributes]
  (case tag
    :img (:src attributes)
    :a (:href attributes)
    nil))

(defn process-page
  "Takes a page's DOM and returns a hash-map with images URLs and list
  of <a> tags href attributes."
  [dom]
  (let [tag (tag dom)
        attrs (attributes dom)
        children (children dom)]
    (if (or (not children) (string? children))
      (conj [] (get-src-if-img tag attrs))
      (filter some? (concat (conj [] (get-src-if-img tag attrs)) (mapcat imgs-src (filter vector? children)))))))

(defn construct-absolute-path
  "Constructs absolute path to the resources (image in our case)."
  [prefix img-src]
  (str prefix "/" img-src))

;; This function should return a list of images URLs and a list of
;; hrefs.
(defn crawl-page [url]
  (let [h {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}
        resp (client/get url {:headers h})
        dom (parse-string (:body resp))]
    (map (partial construct-absolute-path url) (imgs-src dom))))

(defn run-crawler [url]
  ;; Not sure about this function yet... Let's start with single page
  ;; processing...
  )

(defn -main
  "Let's the first argument will be the URL."
  [& args]
  (if (< 2 (count args))
    (do
      (println "Usage: lein run <URL> <TARGET DIR>")
      (System/exit 1))
    ;; else do the thing!
    (let [url (first args)
          target-dir (second args)]
      (run-crawler url))))
