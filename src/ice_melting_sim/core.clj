(ns ice-melting-sim.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:gen-class))

(def grid-width 100)
(def grid-height 100)

(defn setup []
  ; Initially set the background to the "frozen" color
  (q/background 125, 195, 245)
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 120)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :rgb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:grid (into {} (for [x (range grid-height) y (range grid-width)] [[x y] :frozen]))
   :paused false})

(defn draw-state [state]
  ;; We don't need to clear the sketch on each draw because cells will never re-freeze
  
  (let [sq-height (int (/ (q/width) grid-width))
        sq-width (int (/ (q/height) grid-height))]
    (q/no-stroke)
    (q/fill 203, 229, 247)
    (doseq [[[x y] state] (filter #(= (second %) :melted) (:grid state))]
      (q/rect (* x sq-width) (* y sq-height) sq-width sq-height))))

(def log (atom []))
(defn trace [msg x]
  (swap! log conj msg)
  x)

(defn update-cell [grid coord state]
  (let [frozen-neighbors (->> (mapv #(mapv + %1 %2)
                             (repeat coord)
                             [[0 -1] [0 1] [1 0] [-1 0]])
                       (map #(grid %))
                       (filter #(= :frozen %))
                       count)
        melt? (case frozen-neighbors
                0 (<= (rand) (/ 40 2000))
                1 (<= (rand) (/ 30 2000))
                2 (<= (rand) (/ 20 2000))
                3 (<= (rand) (/ 10 2000))
                4 (<= (rand) (/ 1 100000)))]
    (cond
      (= state :melted) (trace {:coord coord :state state :frozen-neighbors frozen-neighbors :path 1} :melted)
      melt? (trace {:coord coord :state state :frozen-neighbors frozen-neighbors :path 2} :melted)
      :else (trace {:coord coord :state state :frozen-neighbors frozen-neighbors :path 3} :frozen))))

(defn update-state [state]
  (assoc state :grid (into {} (map (fn [[k v]] [k (update-cell (:grid state) k v)])) (:grid state))))

(q/defsketch ice-melting-sim
  :title "Ice melting"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :mouse-clicked (fn [state evt]
                   (if (= :left (:button evt))
                     (if (:paused state)
                       (do (q/start-loop)
                           (assoc state :paused false))
                       (do (q/no-loop)
                           (assoc state :paused true)))
                     state))
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])

(defn -main [& args])
