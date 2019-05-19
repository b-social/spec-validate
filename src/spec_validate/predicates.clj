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

(defn uuid-v4? [value]
  "Returns true if the provided value is a string representing a v4 UUID,
  else returns false."
  (exception->false (boolean (re-matches uuid-v4-regex value))))

;; URLs
(defn absolute-url? [value]
  "Returns true if the provided value is a string representing an absolute URL,
  else returns false."
  (exception->false (urls/absolute? value)))

;; strings
(def ^:private digits-regex #"^\d+$")

(defn content? [value]
  "Returns true if the provided value is a string containing non-whitespace
  characters, else returns false."
  (exception->false (not (string/blank? value))))

(defn digits? [value]
  "Returns true if the provided value is a string containing only the digits
  0-9, else returns false."
  (exception->false (boolean (re-matches digits-regex value))))

;; numbers
(defn- parse-number [value]
  (if (string? value)
    (.validate (DoubleValidator.) value)
    value))

(defn positive? [value]
  "Returns true if the provided value is a positive number or a string
  representing a positive number, else returns false."
  (exception->false ((valip.predicates/gt 0) value)))

(defn negative? [value]
  "Returns true if the provided value is a positive number or a string
  representing a positive number, else returns false."
  (exception->false ((valip.predicates/lt 0) value)))

(defn zero? [value]
  "Returns true if the provided value is zero or a string representing zero,
  else returns false."
  (exception->false (clojure.core/zero? (parse-number value))))

;; collections
(defn not-empty? [value]
  "Returns true if the provided value has a count of at least 1,
  else returns false."
  (exception->false (>= (count value) 1)))

(defn length-equal-to? [length]
  "Returns a function that returns true if the provided value has the
  specified length, else returns false."
  #(exception->false (= (count %) length)))

(defn length-less-than? [length]
  "Returns a function that returns true if the provided value has a length
  less than the specified length, else returns false."
  #(exception->false (and (some? %) (< (count %) length))))

(defn length-greater-than? [length]
  "Returns a function that returns true if the provided value has a length
  greater than the specified length, else returns false."
  #(exception->false (and (some? %) (> (count %) length))))

;; dates
(defn iso8601-datetime? [value]
  "Returns true if the provided value is a string representing an ISO8601
  datetime, else returns false."
  (nil->false (and (datetimes/parse value) true)))

;; currency
(defn currency-amount? [value]
  "Returns true if the provided value is a string representing a currency
  amount, else returns false."
  (exception->false (and (money-amounts/parse (str "GBP " value)) true)))

(defn currency-code? [value]
  "Returns true if the provided value is a string representing a currency
  code, else returns false."
  (exception->false (and (money-currencies/for-code value) true)))

;; post codes
(def ^:private postcode-regex
  (re-pattern
    (str "^([A-Za-z][A-Ha-hK-Yk-y]?[0-9][A-Za-z0-9]? "
      "[0-9][A-Za-z]{2}|[Gg][Ii][Rr] 0[Aa]{2})$")))

(defn postcode? [value]
  "Returns true if the provided value is a string representing a UK postcode,
  else returns false."
  (exception->false (boolean (re-matches postcode-regex value))))

;; phone numbers
(def ^:dynamic *default-phone-number-region-code* "GB")

(def ^:private ^PhoneNumberUtil phone-number-util
  (PhoneNumberUtil/getInstance))

(defn- string->PhoneNumber [value]
  (try
    (.parse phone-number-util value *default-phone-number-region-code*)
    (catch NumberParseException _
      nil)))

(defn phone-number? [value]
  "Returns true if the provided value is a string representing a phone number,
  else returns false. By default, treats phone numbers as being from
  Great Britain, however the default region can be overridden with
  `*default-phone-number-region-code*`."
  (exception->false
    (.isValidNumber phone-number-util (string->PhoneNumber value))))

;; email address
(defn email-address? [value]
  "Returns true if the email address is valid, based on RFC 2822. Email
  addresses containing quotation marks or square brackets are considered
  invalid, as this syntax is not commonly supported in practise. The domain of
  the email address is not checked for validity."
  (exception->false (valip-predicates/email-address? value)))