(ns spec-validate.core-test
  (:refer-clojure :exclude [string? number?])
  (:require
    [clojure.test :refer :all]
    [clojure.spec.alpha :as spec]

    [spec-validate.core :refer :all]))

(def string?
  ^{:spec-validate/requirement :must-be-a-string}
  (fn [value] (clojure.core/string? value)))

(def length-greater-than-5?
  ^{:spec-validate/requirement :must-have-length-greater-than-5}
  (fn [value] (> (count value) 5)))

(def number?
  ^{:spec-validate/requirement :must-be-a-number}
  (fn [value] (clojure.core/number? value)))

(defn greater-than-5? [value]
  (> value 5))

(spec/def ::some-string string?)
(spec/def ::some-number number?)
(spec/def ::some-large-value greater-than-5?)

(spec/def ::some-complex-thing (spec/and string? length-greater-than-5?))

(spec/def ::other-string string?)
(spec/def ::other-number number?)

(spec/def ::some-object
  (spec/keys
    :req-un [::some-string ::some-number ::some-large-value]))

(spec/def ::some-complex-object
  (spec/keys
    :req-un [::some-complex-thing]))

(spec/def ::other-object
  (spec/keys
    :req-un [::other-string ::other-number]))

(spec/def ::higher-order-object
  (spec/keys
    :req-un [::some-object ::other-object]))

(deftest about-validator-for
  (testing "when there are no problems"
    (let [target {:some-string      "correct"
                  :some-number      50
                  :some-large-value 10}
          valid? (validator-for ::some-object)]
      (is (true? (valid? target)))))

  (testing "when there are problems"
    (let [target {:wrong-string "nope"}
          valid? (validator-for ::some-object)]
      (is (false? (valid? target))))))

(deftest about-problem-calculator-for
  (testing "when there are no problems"
    (let [target {:some-string      "correct"
                  :some-number      50
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object)]
      (is (= (calculate-problems target) []))))

  (testing "when one top level field is invalid"
    (let [target {:some-string      "correct"
                  :some-number      "oops"
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object)]
      (is (= (calculate-problems target)
            [{:subject      :some-object
              :field        [:some-number]
              :type         :invalid
              :requirements [:must-be-a-number]}]))))

  (testing "when a top level field is specified with spec/and and is invalid"
    (let [target {:some-complex-thing "abc"}
          calculate-problems
          (problem-calculator-for ::some-complex-object)]
      (is (= (calculate-problems target)
            [{:subject      :some-complex-object
              :field        [:some-complex-thing]
              :type         :invalid
              :requirements [:must-have-length-greater-than-5]}]))))

  (testing "when one top level field is missing"
    (let [target {:some-string      "correct"
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object)]
      (is (= (calculate-problems target)
            [{:subject      :some-object
              :field        [:some-number]
              :type         :missing
              :requirements [:must-be-present]}]))))

  (testing "when many top level fields are invalid"
    (let [target {:some-string      10
                  :some-number      "oops"
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object)]
      (is (= (calculate-problems target)
            [{:subject      :some-object
              :field        [:some-string]
              :type         :invalid
              :requirements [:must-be-a-string]}
             {:subject      :some-object
              :field        [:some-number]
              :type         :invalid
              :requirements [:must-be-a-number]}]))))

  (testing "when many top level fields are missing"
    (let [target {:some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object)]
      (is (= (calculate-problems target)
            [{:subject      :some-object
              :field        [:some-string]
              :type         :missing
              :requirements [:must-be-present]}
             {:subject      :some-object
              :field        [:some-number]
              :type         :missing
              :requirements [:must-be-present]}]))))

  (testing "when one nested field is invalid"
    (let [target {:some-object  {:some-string      10
                                 :some-number      10
                                 :some-large-value 10}
                  :other-object {:other-string "correct"
                                 :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject      :higher-order-object
              :field        [:some-object :some-string]
              :type         :invalid
              :requirements [:must-be-a-string]}]))))

  (testing "when one nested field is missing"
    (let [target {:some-object  {:some-number      10
                                 :some-large-value 10}
                  :other-object {:other-string "correct"
                                 :other-number 20}}
          calculate-problems
          (problem-calculator-for ::higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject      :higher-order-object
              :field        [:some-object :some-string]
              :type         :missing
              :requirements [:must-be-present]}]))))

  (testing "when many nested fields are invalid"
    (let [target {:some-object  {:some-string      "10"
                                 :some-number      "10"
                                 :some-large-value 10}
                  :other-object {:other-string 10
                                 :other-number 10}}
          calculate-problems
          (problem-calculator-for ::higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject      :higher-order-object
              :field        [:some-object :some-number]
              :type         :invalid
              :requirements [:must-be-a-number]}
             {:subject      :higher-order-object
              :field        [:other-object :other-string]
              :type         :invalid
              :requirements [:must-be-a-string]}]))))

  (testing "when many nested fields are missing"
    (let [target {:some-object  {:some-number      10
                                 :some-large-value 10}
                  :other-object {:other-string "correct"}}
          calculate-problems
          (problem-calculator-for ::higher-order-object)]
      (is (= (calculate-problems target)
            [{:subject      :higher-order-object
              :field        [:some-object :some-string]
              :type         :missing
              :requirements [:must-be-present]}
             {:subject      :higher-order-object
              :field        [:other-object :other-number]
              :type         :missing
              :requirements [:must-be-present]}]))))

  (testing "allows validation subject to be overridden"
    (let [target {:some-string      "correct"
                  :some-number      "oops"
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object
            :validation-subject :the-object)]
      (is (= (calculate-problems target)
            [{:subject      :the-object
              :field        [:some-number]
              :type         :invalid
              :requirements [:must-be-a-number]}]))))

  (testing "allows a problem transformer to be provided"
    (let [target {:some-string      "correct"
                  :some-number      "oops"
                  :some-large-value 10}
          calculate-problems
          (problem-calculator-for ::some-object
            :problem-transformer
            (fn [problem]
              (merge
                (select-keys problem [:subject :field :requirements])
                {:type    :validation-failure
                 :problem (:type problem)})))]
      (is (= (calculate-problems target)
            [{:type         :validation-failure
              :subject      :some-object
              :field        [:some-number]
              :problem      :invalid
              :requirements [:must-be-a-number]}])))))
