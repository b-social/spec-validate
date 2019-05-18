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

(deftest about-validator-for
  (testing "when there are no problems"
    (let [target {:some-string "correct"
                  :some-number 50}
          valid? (validator-for ::some-object)]
      (is (true? (valid? target)))))

  (testing "when there are problems"
    (let [target {:wrong-string "nope"}
          valid? (validator-for ::some-object)]
      (is (false? (valid? target))))))

(deftest about-problem-calculator-for
  (testing "when there are no problems"
    (let [target {:some-string "correct"
                   :some-number 50}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems target) []))))

  (testing "when one top level field is invalid"
    (let [target {:some-string "correct"
                   :some-number "oops"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems target)
            [{:subject :some-object
              :field   [:some-number]
              :type    :invalid}]))))

  (testing "when one top level field is missing"
    (let [target {:some-string "correct"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems target)
            [{:subject :some-object
              :field   [:some-number]
              :type    :missing}]))))

  (testing "when many top level fields are invalid"
    (let [target {:some-string 10
                   :some-number "oops"}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems target)
            [{:subject :some-object
              :field   [:some-string]
              :type    :invalid}
             {:subject :some-object
              :field   [:some-number]
              :type    :invalid}]))))

  (testing "when many top level fields are missing"
    (let [target {}
          calculate-problems
          (problem-calculator-for ::some-object :some-object)]
      (is (= (calculate-problems target)
            [{:subject :some-object
              :field   [:some-string]
              :type    :missing}
             {:subject :some-object
              :field   [:some-number]
              :type    :missing}]))))

  (testing "when one nested field is invalid"
    (let [target {:some-object  {:some-string 10
                                  :some-number 10}
                   :other-object {:other-string "correct"
                                  :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :invalid}]))))

  (testing "when one nested field is missing"
    (let [target {:some-object  {:some-number 10}
                   :other-object {:other-string "correct"
                                  :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :missing}]))))

  (testing "when many nested fields are missing"
    (let [target {:some-object  {:some-number 10}
                   :other-object {:other-string "correct"}}
          calculate-problems
          (problem-calculator-for ::higher-order-object :higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject :higher-order-object
              :field   [:some-object :some-string]
              :type    :missing}
             {:subject :higher-order-object
              :field   [:other-object :other-number]
              :type    :missing}])))))
