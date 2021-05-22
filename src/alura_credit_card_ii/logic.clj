(ns alura-credit-card-ii.logic
  (:import [java.util UUID]))

(defn new-client
  "Creates a new client entity"
  ([name cpf email]
   {:client/id (UUID/randomUUID)
    :client/name name
    :client/cpf cpf
    :client/email email})
  ([uuid name cpf email]
   {:client/id uuid
    :client/name name
    :client/cpf cpf
    :client/email email}))

(defn new-credit-card
  "Creates a new credit card entity"
  ([number cvv expiration-date limit]
   {:credit-card/id (UUID/randomUUID)
    :credit-card/number number
    :credit-card/cvv cvv
    :credit-card/expiration-date expiration-date
    :credit-card/limit limit})
  ([uuid number cvv expiration-date limit]
   {:credit-card/id uuid
    :credit-card/number number
    :credit-card/cvv cvv
    :credit-card/expiration-date expiration-date
    :credit-card/limit limit}))

(defn new-purchase
  "Creates a new purchase entity"
  ([date value establishment category]
   {:purchase/id (UUID/randomUUID)
    :purchase/date date
    :purchase/value value
    :purchase/establishment establishment
    :purchase/category category}))