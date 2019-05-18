(ns spec-validate.core
  (:require
    [clojure.spec.alpha :as spec]))

(defprotocol Validator
  (valid? [_ m])
  (problems-for [_ m]))

(defn predicate-details [problem-details]
  (vec (get (vec problem-details) 2)))
(defn pred-fn-symbol [problem-details]
  (get (predicate-details problem-details) 0))
(defn pred-fn-field [problem-details]
  (get (predicate-details problem-details) 2))

(defn problem-calculator-for [spec validation-subject]
  (fn [validate-data]
    (let [context (spec/explain-data spec validate-data)]
      (reduce
        (fn [accumulator problem]
          (let [pred (:pred problem)
                is-missing (and
                             (seq? pred)
                             (= 'clojure.core/contains? (pred-fn-symbol pred)))
                [path-to-field type]
                (if is-missing
                  [(conj (:in problem) (pred-fn-field pred)) :missing]
                  [(:in problem) :invalid])]
            (conj accumulator
              {:type type
               :subject validation-subject
               :field   path-to-field})))
        []
        (::spec/problems context)))))
