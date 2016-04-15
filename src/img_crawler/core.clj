(ns img-crawler.core
  (:require [pl.danieljanus.tagsoup :refer [parse tag attributes children]])
  (:gen-class))

(defn img-tag? [tag]
  (= :img tag))

(defn src-attribute [attributes]
  (when (map? attributes)
    (:src attributes)))

(defn get-src-if-img [tag attributes]
  (when (img-tag? tag)
    (src-attribute attributes)))

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

(defn crawl-page [url]
  (let [content (parse url)]
    ;; content is a vector of vectors... I need to look a bit
    ;; closer...  So... we have a vector of vectors represents DOM...
    ;; And attributes are hash-maps... Our goal is to extract all
    ;; <img> tags and their attriutes... Smells like *recursion* :)
    ))

(defn run-crawler [url]
  ;; Not sure about this function yet... Let's start with single page
  ;; processing...
  )

(defn -main
  "Let's the first argument will be the URL."
  [& args]
  (if (empty? args)
    (do
      (println "Please, specify the URL")
      (System/exit 1))
    ;; else do the thing!
    (let [url (first args)]
      (run-crawler url))))
