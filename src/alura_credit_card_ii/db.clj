(ns alura-credit-card-ii.db
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [java-time :as jt])
  (:import java.util.Calendar))

(def database-uri "datomic:dev://localhost:4334/credit")

(defn open-connection!
  [database-uri]
  (d/create-database database-uri)
  (d/connect database-uri))

(defn delete-database!
  [database-uri]
  (d/delete-database database-uri))


(def client-schema [{:db/ident       :client/id
                     :db/valueType   :db.type/uuid
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/identity
                     :db/doc "Client entity ID."}
                    {:db/ident       :client/name
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "Name of the client."}
                    {:db/ident       :client/cpf
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "CPF of the client."}
                    {:db/ident       :client/email
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "Email of the client."}
                    {:db/ident       :client/credit-card
                     :db/valueType   :db.type/ref
                     :db/cardinality :db.cardinality/one
                     :db/doc "Credit card reference to entity."}])

(def credit-card-schema [{:db/ident       :credit-card/id
                          :db/valueType   :db.type/uuid
                          :db/cardinality :db.cardinality/one
                          :db/doc "Credit Card entity ID."
                          :db/unique      :db.unique/identity}
                         {:db/ident       :credit-card/number
                          :db/valueType   :db.type/string
                          :db/cardinality :db.cardinality/one
                          :db/doc "Credit Card number."}
                         {:db/ident       :credit-card/cvv
                          :db/valueType   :db.type/long
                          :db/cardinality :db.cardinality/one
                          :db/doc "Credit Card CVV code."}
                         {:db/ident       :credit-card/expiration-date
                          :db/valueType   :db.type/instant
                          :db/cardinality :db.cardinality/one
                          :db/doc "Credit Card expiration date."}
                         {:db/ident       :credit-card/limit
                          :db/valueType   :db.type/bigdec
                          :db/cardinality :db.cardinality/one
                          :db/doc "Credit Card limit."}
                         {:db/ident       :credit-card/purchases
                          :db/valueType   :db.type/ref
                          :db/cardinality :db.cardinality/many
                          :db/doc "Credit card purchases."}])

(def purchase-schema [{:db/ident       :purchase/id
                       :db/valueType   :db.type/uuid
                       :db/cardinality :db.cardinality/one
                       :db/doc "Purchase entity ID."
                       :db/unique      :db.unique/identity}
                      {:db/ident       :purchase/date
                       :db/valueType   :db.type/instant
                       :db/cardinality :db.cardinality/one
                       :db/doc "Purchase Date."}
                      {:db/ident       :purchase/value
                       :db/valueType   :db.type/bigdec
                       :db/cardinality :db.cardinality/one
                       :db/doc "Purchase value."}
                      {:db/ident       :purchase/establishment
                       :db/valueType   :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "Establishment name."}
                      {:db/ident       :purchase/category
                       :db/valueType   :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "Purchase category."}])

(def all-schemas (concat client-schema credit-card-schema purchase-schema))

(defn create-schemas!
  "Receives a open connection, and a vector of schemas definitions"
  [conn schemas]
  (d/transact conn schemas))

(defn create-clients!
  "Receives a vector of clients and add them to the datomic database"
  [conn clients]
  (d/transact conn clients))

(defn create-credit-cards!
  [conn credit-cards]
  (d/transact conn credit-cards))

(defn create-purchases!
  [conn purchases]
  (d/transact conn purchases))

(defn all-clients
  "Receives a database snapshot and returns a list with all clients"
  [db]
  (d/q '[:find (pull ?client [*])
         :where [?client :client/id]] db))

(defn all-credit-cards
  [db]
  (d/q '[:find (pull ?credit-card [*])
         :where [?credit-card :credit-card/id]] db))

(defn all-purchases
  [db]
  (d/q '[:find (pull ?purchase [*])
         :where [?purchase :purchase/id]] db))

(defn assign-credit-card-to-client!
  "Assign a credit card entity to a specific client entity"
  [conn credit-card client]
  (d/transact conn [[:db/add [:client/id (:client/id client)]
                     :client/credit-card
                     [:credit-card/id (:credit-card/id credit-card)]]]))

(defn assign-purchase-to-credit-card!
  "Assign a purchase entity to a credit-card entity"
  [conn purchase credit-card]
  (d/transact conn [[:db/add [:credit-card/id (:credit-card/id credit-card)]
                     :credit-card/purchases
                     [:purchase/id (:purchase/id purchase)]]]))

(defn query-all-purchases-by-cpf
  "Receives a snapshot db and a CPF and returns a list of purchases for the
   specific client."
  [db cpf]
  (->> (d/q '[:find (pull ?client [{:client/credit-card [{:credit-card/purchases [*]}]}])
              :in $ ?cpf
              :where [?client :client/cpf ?cpf]] db cpf)
       ffirst
       :client/credit-card
       :credit-card/purchases))

(defn group-purchases-by-category
  [purchases]
  (group-by :purchase/category purchases))

(defn total-spent
  [purchases]
  (->> purchases
       (map :purchase/value)
       (reduce +)))

(defn category-sumary
  [[category purchases]]
  {:category/name category
   :purchases/total-spent (total-spent purchases)})

(defn purchase-sumary-by-category
  [db cpf]
  (let [purchases (query-all-purchases-by-cpf db cpf)]
    (->> purchases
         group-purchases-by-category
         (map category-sumary))))

(defn client-with-max-number-of-purchases
  [db]
  (first (d/q '[:find ?name (count ?purchases)
                :keys client/name client/number-of-purchases
         :where [?credit-card :credit-card/purchases ?purchases]
                [?client :client/credit-card ?credit-card]
                [?client :client/name ?name]] db)))

(defn client-with-highest-value-purchase
  [db]
  (let [results (d/q '[:find (pull ?purchase [:purchase/id {:credit-card/_purchases [:credit-card/id {:client/_credit-card [:client/name]}]}]) (max ?value)
                       :keys client value
                       :where [?purchase :purchase/value ?value]] db)]
    (some->> results
             (sort-by :value)
             last)))

(defn clients-without-purchases
  [db]
  (d/q '[:find (pull ?credit-card [:credit-card/id {:client/_credit-card [:client/name]}])
         :where [?credit-card :credit-card/number]
         (not [?credit-card :credit-card/purchases])] db))

;; (defn get-month-from-instant
;;   [instant]
;;   (let [calendar (Calendar/getInstance)]
;;     (doto calendar
;;       (.setTime instant)
;;       (.get Calendar/MONTH))))

;; (defn purchases-in-the-month
;;   "Get all purchases made in the same month passed by date"
;;   [purchases, date]
;;   (let [purchase-month (.getMonth date)
;;         purchase-year (.getYear date)
;;         purchases-in-the-same-month (filter #(and (= (jt/month (:date %)) purchase-month) (= (jt/year (:date %)) purchase-year)) purchases)]
;;     purchases-in-the-same-month))

;; (defn month-billing
;;   [db cpf date]
;;   (let [all-purchases (query-all-purchases-by-cpf db cpf)
;;         purchases]))

;; (get-month-from-instant #inst "2021")