(ns spec-validate.core-test
  (:require
    [clojure.test :refer :all]
    [clojure.spec.alpha :as spec]

    [spec-validate.core :refer :all]))

(spec/def ::some-string string?)
(spec/def ::some-number number?)

(spec/def ::other-string string?)
(spec/def ::other-number number?)

(spec/def ::some-object
  (spec/keys
    :req-un [::some-string ::some-number]))

(spec/def ::other-object
  (spec/keys
    :req-un [::other-string ::other-number]))

(spec/def ::higher-order-object
  (spec/keys
    :req-un [::some-object ::other-object]))

(deftest about-problem-calculator-for
  (testing "when there are no problems"
    (let [subject {:some-string "correct"
                   :some-number 50}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems subject) []))))

  (testing "when one top level field is invalid"
    (let [subject {:some-string "correct"
                   :some-number "oops"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems subject)
            [{:subject :some-object
              :field   [:some-number]
              :type    :invalid}]))))

  (testing "when one top level field is missing"
    (let [subject {:some-string "correct"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems subject)
            [{:subject :some-object
              :field   [:some-number]
              :type    :missing}]))))

  (testing "when many top level fields are invalid"
    (let [subject {:some-string 10
                   :some-number "oops"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems subject)
            [{:subject :some-object
              :field   [:some-string]
              :type    :invalid}
             {:subject :some-object
              :field   [:some-number]
              :type    :invalid}]))))

  (testing "when many top level fields are missing"
    (let [subject {}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems subject)
            [{:subject :some-object
              :field   [:some-string]
              :type    :missing}
             {:subject :some-object
              :field   [:some-number]
              :type    :missing}]))))

  (testing "when one nested field is invalid"
    (let [subject {:some-object  {:some-string 10
                                  :some-number 10}
                   :other-object {:other-string "correct"
                                  :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems subject)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :invalid}]))))

  (testing "when one nested field is missing"
    (let [subject {:some-object  {:some-number 10}
                   :other-object {:other-string "correct"
                                  :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems subject)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :missing}]))))

  (testing "when many nested fields are missing"
    (let [subject {:some-object  {:some-number 10}
                   :other-object {:other-string "correct"}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems subject)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :missing}
             {:subject :higher-order-object
              :field   [:other-object :other-number]
              :type    :missing}])))))
