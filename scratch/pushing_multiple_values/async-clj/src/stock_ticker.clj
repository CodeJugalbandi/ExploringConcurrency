(ns stock-ticker
  (:require [portfolio :as p]
            [clojure.edn :as edn]
            [clojure.core.async :as async
             :refer [go go-loop >! <! buffer dropping-buffer chan mult tap untap]]
            [cheshire.core :as json]
            [aleph.http :as http]
            [manifold.stream :as s]))

;; Given
(defonce wss-url-template "wss://national-stock-service.herokuapp.com/stocks/realtime/%s")
(defn- get-ws-url [& [ticker]] (format wss-url-template (or ticker "")))

;; Make ends meet
(defn- parse-message [message-str] (json/parse-string message-str keyword))
(defn create-message-stream [url & {:keys [on-connect before-disconnect]
                                    :or {on-connect identity before-disconnect identity}}]
  (let [ws-conn     (http/websocket-client url)
        ch          (chan (dropping-buffer 32) (map parse-message))
        stop?       (atom false)
        stop-fn     (fn []
                      (reset! stop? true)
                      (before-disconnect ws-conn)
                      (s/close! @ws-conn)
                      (async/close! ch))]

    (go-loop []
      (when-let [message @(s/take! @ws-conn)]
        (>! ch message)
        (if-not @stop?
          (recur))))

    (on-connect ws-conn)
    {:ws-conn ws-conn :ch ch :stop-fn stop-fn}))

(defn send-message [ws-conn message]
  (s/put! @ws-conn (json/generate-string message)))

(defn subscribe [ws-conn] (send-message ws-conn {:command :subscribe}))
(defn unsubscribe [ws-conn] (send-message ws-conn {:command :unsubscribe}))

(defn add-listener [m listener-fn]
  (let [stop?   (atom false)
        ch      (chan (async/dropping-buffer 32))
        _       (tap m ch)
        stop-fn (fn []
                  (untap m ch)
                  (reset! stop? true))]
    (go-loop []
      (when-let [message (<! ch)]
        (listener-fn message)
        (if-not @stop?
          (recur))))
    stop-fn))

(defn stock-info-printer [{:keys [ticker price]}]
  (println (pr-str [ticker price])))

(defn- stock-prices-updater [stocks-info-db]
  {:pre [(= clojure.lang.Atom (type stocks-info-db))]}
  (fn [{:keys [ticker price]}]
    (swap! stocks-info-db assoc ticker price)))

(defn net-worth-printer [portfolio]
  {:pre [(= clojure.lang.Atom (type portfolio))]}
  (let [stock-info-db       (atom {})
        prices-db-update-fn (stock-prices-updater stock-info-db)]
    (fn [{:keys [ticker] :as price-info}]
      (if (@portfolio ticker)
        (prices-db-update-fn price-info))
      (println (str "Net worth: " (p/net-worth @portfolio @stock-info-db))))))

(defn load-portfolio! [portfolio stocks-data]
  (doseq [[symbol qty buy-price] stocks-data]
    (p/add-stocks! portfolio symbol (p/new-stocks qty buy-price))))

(comment

  (do (def streamer (create-message-stream (get-ws-url)
                                           :on-connect subscribe
                                           :before-disconnect unsubscribe))
      (def ch (:ch streamer))
      (def stop-fn (:stop-fn streamer))
      (def m (async/mult ch)))

  (do
      (defonce portfolio (p/new-portfolio))
      (load-portfolio! portfolio (-> "portfolio.edn" slurp edn/read-string)))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (def print-stop-fn (add-listener m stock-info-printer))
  (def net-worth-print-stop-fn (add-listener m (net-worth-printer portfolio)))
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; Stop-knobs
  (stop-fn)
  (p/clear-portfolio! portfolio)
  (print-stop-fn)
  (net-worth-print-stop-fn))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;