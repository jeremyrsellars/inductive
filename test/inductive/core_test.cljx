(ns inductive.core_test
  #+clj (:require [clojure.test :as t
                   :refer (is deftest with-test run-tests testing)])
  #+cljs (:require-macros [cemerick.cljs.test
                           :refer (is deftest with-test run-tests testing test-var)])
  #+cljs (:require [cemerick.cljs.test :as t]))

(deftest somewhat-less-wat
  (is (= "{}[]" "(+ {} [])")))

(deftest somewhat-less-wat2
  (is (= "{}[]" "(+ {} [])")))
