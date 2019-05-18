(ns spec-validate.core
  (:require
    [clojure.spec.alpha :as spec]))

(defn predicate-details [problem-details]
  (vec (get (vec problem-details) 2)))
(defn pred-fn-symbol [problem-details]
  (get (predicate-details problem-details) 0))
(defn pred-fn-field [problem-details]
  (get (predicate-details problem-details) 2))

(defn validator-for [spec]
  (fn [validation-target]
    (spec/valid? spec validation-target)))

(defn problem-calculator-for [spec validation-subject]
  (fn [validate-target]
    (let [context (spec/explain-data spec validate-target)]
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
