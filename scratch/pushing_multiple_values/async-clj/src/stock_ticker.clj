(ns stock-ticker
  (:require [clojure.core.async :as async
             :refer [go go-loop >! <! buffer chan mult tap untap]]
            [cheshire.core :as json]
            [aleph.http :as http]
            [manifold.stream :as s]))

(defonce wss-url-template "wss://national-stock-service.herokuapp.com/stocks/realtime/%s")
(defn- url-for [& [ticker]]
   (format wss-url-template (or ticker "")))

(defn connect [url] (http/websocket-client url))
(defn disconnect [ws-conn] (s/close! @ws-conn))

(defn start-processor [ws-conn ch]
  (let [stop?   (atom false)
        stop-fn (fn [] (reset! stop? true))]
    (go-loop [message @(s/take! @ws-conn)]
      (>! ch (json/parse-string message keyword))
      (if-not @stop?
        (recur @(s/take! @ws-conn))))
    stop-fn))

(defn send-message-as-json-str [ws-conn message]
  (s/put! @ws-conn (json/generate-string message)))

(defn subscribe ([ws-conn] (send-message-as-json-str ws-conn {:command :subscribe})))
(defn unsubscribe ([ws-conn] (send-message-as-json-str ws-conn {:command :unsubscribe})))

(defn get-stock-movements-streamer
  ([] (get-stock-movements-streamer ""))
  ([ticker]
   (let [url            (url-for ticker)
         ws-conn        (connect url)
         ch             (async/chan (async/buffer 32))
         stream-halt-fn (start-processor ws-conn ch)
         stop-fn        (fn []
                          (stream-halt-fn)
                          (unsubscribe ws-conn)
                          (disconnect ws-conn)
                          (async/close! ch))]
     (start-processor ws-conn ch)
     (subscribe ws-conn)
     {:ch ch :stop-fn stop-fn})))

(defn start-stock-movement-consumer [m action-fn]
  (let [stop? (atom false)
        ch (chan 1)
        _ (tap m ch)
        stop-fn (fn []
                  (reset! stop? true)
                  (untap m ch)
                  (async/close! ch))]
    (go-loop [stock-info (<! ch)]
      (action-fn stock-info)
      (if-not @stop?
        (recur (<! ch))))
    stop-fn))

(defn stock-info-printer [{:keys [ticker price]}]
  (println (pr-str [ticker price])))

(defn stock-prices-updater [stocks-info-db]
  {:pre [(= clojure.lang.Atom (type stocks-info-db))]}
  (fn [{:keys [ticker price]}]
      (swap! stocks-info-db assoc ticker price)))

(defn new-stock [quantity & [buy-price]]
  {:quantity quantity :buy-price (or buy-price 0.0)})

(defn stock-lot-worth [stock-lot current-price]
  (->> stock-lot
      (map :quantity)
      (reduce +)
      (* current-price)))

(defn stocks-lot-cost [stocks-lot]
  (reduce
    (fn [acc {:keys [quantity buy-price]}] (+ acc (* quantity buy-price)))
    0
    stocks-lot))

(defn add-stocks! [portfolio symbol stocks]
  {:pre [(= clojure.lang.Atom (type portfolio))]}
  (let [current-lot (get @portfolio symbol [])]
    (swap! portfolio assoc symbol (conj current-lot stocks))))

(defn stocks-worth [symbol stock-lot stocks-info-db]
  (if-let [price (get stocks-info-db symbol)]
    (stock-lot-worth stock-lot price)
    (stocks-lot-cost stock-lot)))

(defn net-worth [portfolio stocks-info-db]
  (reduce (fn [acc [symbol stock-lot]]
            (+ acc (stocks-worth symbol stock-lot stocks-info-db)))
          0
          portfolio))

(defn net-worth-printer [portfolio]
  (let [stock-info-db (atom {})
        prices-db-update-fn (stock-prices-updater stock-info-db)]
    (fn [{:keys [ticker price] :as price-info}]
      (if (@portfolio ticker)
        (prices-db-update-fn price-info))
      (println (str "Net worth: " (net-worth @portfolio @stock-info-db))))))


(comment
  (def streamer (get-stock-movements-streamer))
  (def ch (:ch streamer))
  (def stop-fn (:stop-fn streamer))
  (def m (async/mult ch))

  (def goog-stocks (new-stock 10 100))
  (def goog-stocks-2 (new-stock 10 150))
  (def aapl-stocks (new-stock 20 200))
  (def yhoo-stocks (new-stock 30 300))
  (def msft-stocks (new-stock 40 400))

  (defonce portfolio (atom {}))

  (add-stocks! portfolio "GOOG" goog-stocks)
  (add-stocks! portfolio "GOOG" goog-stocks-2)
  (add-stocks! portfolio "AAPL" aapl-stocks)
  (add-stocks! portfolio "YHOO" yhoo-stocks)
  (add-stocks! portfolio "MSFT" msft-stocks)

  (def print-stop-fn (start-stock-movement-consumer m stock-info-printer))
  (def net-worth-print-stop-fn (start-stock-movement-consumer m (net-worth-printer portfolio)))

  (print-stop-fn)
  (net-worth-print-stop-fn))


