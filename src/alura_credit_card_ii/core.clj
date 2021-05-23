(ns alura-credit-card-ii.core
  (:require [alura-credit-card-ii.db :as db]
            [alura-credit-card-ii.logic :as logic]
            [datomic.api :as d]
            [java-time :as jt])
  (:gen-class))

; Open connection to datomic database
(def conn (db/open-connection! db/database-uri))

; Create schemas
(db/create-schemas! conn db/all-schemas)

; Defining some client entities
(def luan (logic/new-client "Luan" "708.938.913-80" "luan@jourrapide.com"))
(def bruno (logic/new-client "Bruno" "509.567.180-86" "bruno@jourrapide.com"))
(def rafaela (logic/new-client "Rafaela" "275.047.576-75" "rafaela@jourrapide.com"))

; Create clients on datomic database
(db/create-clients! conn [luan, bruno, rafaela])
; Verifying if all clients are correctly added to database
(db/all-clients (d/db conn))

; Defining some credit cards
(def credit-card-for-luan (logic/new-credit-card "379302450924934" 155 #inst "2030-06-10" 20000M))
(def credit-card-for-bruno (logic/new-credit-card "348097759659250" 199 #inst "2030-06-10" 20000M))
(def credit-card-for-rafaela (logic/new-credit-card "375784861888366" 646 #inst "2030-06-10" 20000M))
; Adding credit card entities to datomic database
(db/create-credit-cards! conn [credit-card-for-luan credit-card-for-bruno credit-card-for-rafaela])


; Assign credit card to clients
(db/assign-credit-card-to-client! conn credit-card-for-luan luan)
(db/assign-credit-card-to-client! conn credit-card-for-bruno bruno)
(db/assign-credit-card-to-client! conn credit-card-for-rafaela rafaela)
; list all clients again to verify if the credit card entity reference was added
(db/all-clients (d/db conn))

; Defining some purchases
(def keyboard (logic/new-purchase #inst "2021-05-22T19:47:29" 1500M "Mercado Livre" "Eletronic"))
(def tv (logic/new-purchase #inst "2020-05-22T19:47:29" 2300M "Amazon" "Eletronic"))
(def mouse (logic/new-purchase #inst "2021-01-22T19:47:29" 100M "Amazon" "Eletronic"))
(def sandwich (logic/new-purchase #inst "2021-01-22T19:47:29" 15M "Ifood" "Food"))
(def book (logic/new-purchase #inst "2021-05-22T19:47:29" 130M "Amazon" "Book"))
(def phone (logic/new-purchase #inst "2021-02-22T19:47:29" 1500M "Amazon" "Eletronic"))
(def macbook (logic/new-purchase #inst "2021-02-22T19:47:29" 8000M "Amazon" "Eletronic"))
(def shampoo (logic/new-purchase #inst "2021-02-22T19:47:29" 30M "Amazon" "Cosmetic"))
(def console (logic/new-purchase #inst "2021-02-22T19:47:29" 3500M "Amazon" "Eletronic"))
; Adding purchases to databse
(db/create-purchases! conn [keyboard tv mouse sandwich book phone macbook shampoo console])

; Assign some purchases to the credit-cards
(db/assign-purchase-to-credit-card! conn keyboard credit-card-for-luan)
(db/assign-purchase-to-credit-card! conn mouse credit-card-for-luan)
(db/assign-purchase-to-credit-card! conn sandwich credit-card-for-bruno)
(db/assign-purchase-to-credit-card! conn phone credit-card-for-bruno)
(db/assign-purchase-to-credit-card! conn shampoo credit-card-for-luan)
(db/assign-purchase-to-credit-card! conn macbook credit-card-for-bruno)
; List all credit cards to verifi if purchases references was added correctely
(db/all-credit-cards (d/db conn))

; -> Listing all purchases
(db/all-purchases (d/db conn))

; -> Listing purchases by CPF
(db/query-all-purchases-by-cpf (d/db conn) "708.938.913-80")
(db/query-all-purchases-by-cpf (d/db conn) "509.567.180-86")
(db/query-all-purchases-by-cpf (d/db conn) "275.047.576-75")

; -> Sumarise expenses by category
(db/purchase-sumary-by-category (d/db conn) "708.938.913-80")
(db/purchase-sumary-by-category (d/db conn) "509.567.180-86")
(db/purchase-sumary-by-category (d/db conn) "275.047.576-75")

; -> Client with the max count of purchases
(db/client-with-max-number-of-purchases (d/db conn))

; -> The client with the max purchase value
(db/client-with-highest-value-purchase (d/db conn))

; -> Clients with 0 count of purchases
(db/clients-without-purchases (d/db conn))

(db/delete-database! db/database-uri)