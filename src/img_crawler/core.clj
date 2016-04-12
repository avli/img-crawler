(ns img-crawler.core
  (:require [pl.danieljanus.tagsoup :only parse tag children])
  (:gen-class))

(defn one-or-zero-elements? [coll]
  (let [length (count coll)]
    (or (= 1 length) (= 0 length))))

(defn temp [flat-dom acc]
  (let [[x & rest] flat-dom]
    (if (or (nil? x) (nil? rest))
      ;; we're done, return what we were able to collect
      acc
      ;; this condition is terrible!! please, refactor it
      (if (= :img x)
        (let [[attrs & everything-else] rest]
          (if (and (map? attrs) (contains? attrs :src))
            (temp everything-else (conj acc (:src attrs)))
            (temp rest acc)))
        (temp rest acc)))))

(defn imgs
  "Takes the page content got from tagsoup/parse and returns all <img>
  tags."
  [content]
  ;; I think, I know the easy way to do it :)
  ;; Let's flatten the DOM first...
  (let [flat-dom content]
    ;; Now we should go through the vector and search for :img keywords.
    ;; The information we need will be in the map next to them!

    )
)

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
