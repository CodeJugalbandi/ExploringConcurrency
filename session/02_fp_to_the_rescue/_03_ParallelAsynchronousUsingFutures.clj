(ns weather-and-places)

(def http-host "http://localhost:8000")
(def nearby-path "/places/nearby")
(def weather-path "/weather")

(defn nearby-url [lat lon radius unit]
  (format "%s%s?lat=%s&lon=%s&radius=%s&unit=%s"
          http-host nearby-path lat lon radius unit))

(defn weather-url [lat lon]
  (format "%s%s?lat=%s&lon=%s"
          http-host weather-path lat lon))

(defn print-info [weather places]
  (println
    (format "{ \"weather\": %s, \"placesNearby\": %s }"
            weather places)))

(def test-lat "19.01")
(def test-lon "72.8")
(def radius 25)
(def units "km")

(time
      (let [weather (future (slurp (weather-url test-lat test-lon)))
            places (future (slurp (nearby-url test-lat test-lon radius units)))]

        ;; A generic call that blocks until all futures have completed
        ;; Note the `doall` - if not wrapped in a doall, the `map` expression, being
        ;; lazy, will do no `deref` on any of the futures passed to it.
        (doall
          (map deref [weather places]))

        ;; The deref call with `@` below blocks in any case if the values are not realized
        ;; Because we deref all the futures in this example in the following call,
        ;; technically the above (doall ...) is moot.
        (print-info @weather @places)))

(shutdown-agents)
