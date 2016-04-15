(ns img-crawler.core-test
  (:require [clojure.test :refer :all]
            [img-crawler.core :refer :all]))

(deftest dom-processing-test
  (testing "imgs-src empty dom"
    (is (= '() (imgs-src []))))
  (testing "img-src the non-empty dom, no img tag"
    (is (= '() (imgs-src []))))
  (testing "img-src the non-empty dom has the img tag"
    (is (= '("foo") (imgs-src [:html {} [:img {:src "foo"}]])))))