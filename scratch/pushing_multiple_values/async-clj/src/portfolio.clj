(ns portfolio
  (:import [clojure.lang Atom]))

(defn- stock-lot-worth [stock-lot current-price]
  (->> stock-lot
       (map :quantity)
       (reduce +)
       (* current-price)))

(defn- stocks-lot-cost [stocks-lot]
  (reduce
    (fn [acc {:keys [quantity buy-price]}] (+ acc (* quantity buy-price)))
    0
    stocks-lot))

(defn- stocks-worth [symbol stocks-lot price-db]
  (if-let [price (get price-db symbol)]
    (stock-lot-worth stocks-lot price)
    (stocks-lot-cost stocks-lot)))

(defn new-stocks [quantity & [buy-price]]
  (let [uuid (java.util.UUID/randomUUID)
        now (java.time.LocalDateTime/now (java.time.ZoneId/of "UTC"))]
    {:quantity quantity :buy-price (or buy-price 0.0) :id uuid :timestamp now}))

(defn ^Atom new-portfolio [] (atom {}))
(defn clear-portfolio! [^Atom portfolio] (reset! portfolio {}))
(defn remove-stocks! [^Atom portfolio symbol] (swap! portfolio dissoc symbol))
(defn add-stocks! [^Atom portfolio symbol stocks]
  (let [current-lot (get @portfolio symbol [])]
    (swap! portfolio assoc symbol (conj current-lot stocks))))

(defn net-worth [^Atom portfolio price-db]
  (reduce (fn [acc [symbol stock-lot]]
            (+ acc (stocks-worth symbol stock-lot price-db)))
          0
          portfolio))
