(ns img-crawler.core-test
  (:require [clojure.test :refer :all]
            [img-crawler.core :refer :all]))

(deftest dom-processing-test
  (testing "imgs-src empty dom"
    (is (= '() (imgs-src []))))
  (testing "img-src the non-empty dom, no img tag"
    (is (= '() (imgs-src []))))
  (testing "img-src the non-empty dom has the img tag"
    (is (= '("foo") (imgs-src [:html {} [:img {:src "foo"}]]))))
  (testing "extracting src and href attributes"
    (is (= nil (extract-src-or-href :html {:foo 42 :bar 100})))
    (is (= nil (extract-src-or-href :img {:href 42 :bar 100})))
    (is (= 42 (extract-src-or-href :img {:src 42 :bar 100})))
    (is (= 100 (extract-src-or-href :a {:src 42 :href 100})))))

(deftest work-with-urls
  (testing "constructing an absolute path to an image"
    (is "http://example.com/image.png" (construct-absolute-path "http://example.com/" "image.png"))))
