(ns spec-validate.predicates
  (:refer-clojure :exclude [uuid? zero?])
  (:require
    [valip.predicates :as valip-predicates]

    [clojurewerkz.money.amounts :as money-amounts]
    [clojurewerkz.money.currencies :as money-currencies]

    [org.bovinegenius.exploding-fish :as urls]

    [clj-time.format :as datetimes]
    [clojure.string :as string])
  (:import
    [com.google.i18n.phonenumbers PhoneNumberUtil NumberParseException]
    [org.apache.commons.validator.routines DoubleValidator]))

(defmacro ^:private exception->false [form]
  `(try ~form (catch Exception _# false)))

(defn- nil->false [value]
  (if (nil? value) false value))

;; identifiers
(def ^:private uuid-v4-regex
  (re-pattern
    (str "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-"
      "[89abAB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$")))

(def uuid-v4?
  "Returns true if the provided value is a string representing a v4 UUID,
  else returns false."
  ^{:spec-validate/requirement :must-be-a-v4-uuid}
  (fn [value]
    (exception->false (boolean (re-matches uuid-v4-regex value)))))

;; URLs
(def absolute-url?
  "Returns true if the provided value is a string representing an absolute URL,
  else returns false."
  ^{:spec-validate/requirement :must-be-an-absolute-url}
  (fn [value]
    (exception->false (urls/absolute? value))))

;; strings
(def ^:private digits-regex #"^\d+$")

(def content?
  "Returns true if the provided value is a string containing non-whitespace
  characters, else returns false."
  ^{:spec-validate/requirement :must-have-non-whitespace-content}
  (fn [value]
    (exception->false (not (string/blank? value)))))

(def digits?
  "Returns true if the provided value is a string containing only the digits
  0-9, else returns false."
  ^{:spec-validate/requirement :must-be-a-string-of-digits}
  (fn [value]
    (exception->false (boolean (re-matches digits-regex value)))))

;; numbers
(defn- parse-number [value]
  (if (string? value)
    (.validate (DoubleValidator.) value)
    value))

(def positive?
  "Returns true if the provided value is a positive number or a string
  representing a positive number, else returns false."
  ^{:spec-validate/requirement :must-be-a-positive-number}
  (fn [value]
    (exception->false ((valip.predicates/gt 0) value))))

(def negative?
  "Returns true if the provided value is a positive number or a string
  representing a positive number, else returns false."
  ^{:spec-validate/requirement :must-be-a-negative-number}
  (fn [value]
    (exception->false ((valip.predicates/lt 0) value))))

(def zero?
  "Returns true if the provided value is zero or a string representing zero,
  else returns false."
  ^{:spec-validate/requirement :must-be-zero}
  (fn [value]
    (exception->false (clojure.core/zero? (parse-number value)))))

;; collections
(def not-empty?
  "Returns true if the provided value has a count of at least 1,
  else returns false."
  ^{:spec-validate/requirement :must-not-be-empty}
  (fn [value]
    (exception->false (>= (count value) 1))))

(defn length-equal-to? [length]
  "Returns a function that returns true if the provided value has the
  specified length, else returns false."
  ^{:spec-validate/requirement
    (keyword (str "must-have-length-equal-to-" length))}
  (fn [value]
    (exception->false (= (count value) length))))

(def length-equal-to-1? (length-equal-to? 1))
(def length-equal-to-2? (length-equal-to? 2))
(def length-equal-to-3? (length-equal-to? 3))
(def length-equal-to-4? (length-equal-to? 4))
(def length-equal-to-5? (length-equal-to? 5))
(def length-equal-to-6? (length-equal-to? 6))
(def length-equal-to-7? (length-equal-to? 7))
(def length-equal-to-8? (length-equal-to? 8))
(def length-equal-to-9? (length-equal-to? 9))
(def length-equal-to-10? (length-equal-to? 10))

(defn length-less-than? [length]
  "Returns a function that returns true if the provided value has a length
  less than the specified length, else returns false."
  ^{:spec-validate/requirement
    (keyword (str "must-have-length-less-than-" length))}
  (fn [value]
    (exception->false (and (some? value) (< (count value) length)))))

(def length-less-than-1? (length-less-than? 1))
(def length-less-than-2? (length-less-than? 2))
(def length-less-than-3? (length-less-than? 3))
(def length-less-than-4? (length-less-than? 4))
(def length-less-than-5? (length-less-than? 5))
(def length-less-than-6? (length-less-than? 6))
(def length-less-than-7? (length-less-than? 7))
(def length-less-than-8? (length-less-than? 8))
(def length-less-than-9? (length-less-than? 9))
(def length-less-than-10? (length-less-than? 10))

(defn length-greater-than? [length]
  "Returns a function that returns true if the provided value has a length
  greater than the specified length, else returns false."
  ^{:spec-validate/requirement
    (keyword (str "must-have-length-greater-than-" length))}
  (fn [value]
    (exception->false (and (some? value) (> (count value) length)))))

(def length-greater-than-1? (length-greater-than? 1))
(def length-greater-than-2? (length-greater-than? 2))
(def length-greater-than-3? (length-greater-than? 3))
(def length-greater-than-4? (length-greater-than? 4))
(def length-greater-than-5? (length-greater-than? 5))
(def length-greater-than-6? (length-greater-than? 6))
(def length-greater-than-7? (length-greater-than? 7))
(def length-greater-than-8? (length-greater-than? 8))
(def length-greater-than-9? (length-greater-than? 9))
(def length-greater-than-10? (length-greater-than? 10))

;; dates
(def iso8601-datetime?
  "Returns true if the provided value is a string representing an ISO8601
  datetime, else returns false."
  ^{:spec-validate/requirement :must-be-an-iso8601-datetime}
  (fn [value]
    (nil->false (and (datetimes/parse value) true))))

;; currency
(def currency-amount?
  "Returns true if the provided value is a string representing a currency
  amount, else returns false."
  ^{:spec-validate/requirement :must-be-a-currency-amount}
  (fn [value]
    (exception->false (and (money-amounts/parse (str "GBP " value)) true))))

(def currency-code?
  "Returns true if the provided value is a string representing a currency
  code, else returns false."
  ^{:spec-validate/requirement :must-be-a-currency-code}
  (fn [value]
    (exception->false (and (money-currencies/for-code value) true))))

;; post codes
(def ^:private postcode-regex
  (re-pattern
    (str "^([A-Za-z][A-Ha-hK-Yk-y]?[0-9][A-Za-z0-9]? "
      "[0-9][A-Za-z]{2}|[Gg][Ii][Rr] 0[Aa]{2})$")))

(def postcode?
  "Returns true if the provided value is a string representing a UK postcode,
  else returns false."
  ^{:spec-validate/requirement :must-be-a-uk-postcode}
  (fn [value]
    (exception->false (boolean (re-matches postcode-regex value)))))

;; phone numbers
(def ^:dynamic *default-phone-number-region-code* "GB")

(def ^:private ^PhoneNumberUtil phone-number-util
  (PhoneNumberUtil/getInstance))

(defn- string->PhoneNumber [value]
  (try
    (.parse phone-number-util value *default-phone-number-region-code*)
    (catch NumberParseException _
      nil)))

(def phone-number?
  "Returns true if the provided value is a string representing a phone number,
  else returns false. By default, treats phone numbers as being from
  Great Britain, however the default region can be overridden with
  `*default-phone-number-region-code*`."
  ^{:spec-validate/requirement :must-be-a-phone-number}
  (fn [value]
    (exception->false
      (.isValidNumber phone-number-util (string->PhoneNumber value)))))

;; email address
(def email-address?
  "Returns true if the email address is valid, based on RFC 2822. Email
  addresses containing quotation marks or square brackets are considered
  invalid, as this syntax is not commonly supported in practise. The domain of
  the email address is not checked for validity."
  ^{:spec-validate/requirement :must-be-an-email-address}
  (fn [value]
    (exception->false (valip-predicates/email-address? value))))
