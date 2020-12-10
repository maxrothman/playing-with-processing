(ns ice-melting-sim.double-pendulum
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn zip [& colls]
  (apply map (fn [& itms] (vec itms)) colls))

(defmacro with-stroke-weight [weight & forms]
  `(let [old# (.strokeWeight (q/current-graphics))]
    (q/stroke-weight ~weight)
    ~@forms
    (q/stroke-weight old#)))

(def initial-state [{:angle (* (/ 20 180) Math/PI)
                     :velocity 0}
                    {:angle (* (/ 40 180) Math/PI)
                     :velocity 0}
                    {:angle (* (/ 60 180) Math/PI)
                     :velocity 0}])

(defn setup []
  (q/frame-rate 30)
  ;; Math/{sin,cos} works in radians, not degrees
  {:paused false :state initial-state})

(defn update-state-single [{:keys [velocity angle]}]
  (let [gravity -0.005
        new-v (+ velocity (* gravity (Math/sin angle)))]
    {:velocity new-v
     :angle (+ new-v angle)}))

(defn update1
  "R1 = g sin(ɸ1) + g cos(ɸ2) sin(ɸ1)"
  [{attached-angle :angle} {:keys [velocity angle]}]
  (let [gravity -0.01
        new-v (+ velocity
                 (* gravity (Math/sin angle))
                 (* gravity (Math/cos attached-angle) (Math/sin attached-angle)))]
    {:velocity new-v
     :angle (+ new-v angle)}))

(defn spy [x]
  (prn x)
  x)

(defn update-state [{:keys [state] :as all-state}]
  ;; By passing the last item's own angle in for the attached angle, we ensure that ɸ2 - ɸ1 in the
  ;; second term is always 0, zeroing out the entire second term, reducing the equation one identical
  ;; to that of a single pendulum.
  (->> state
       reverse
       (reductions update1 {:angle (-> state last :angle)})
       rest  ; reductions's output includes the initial accumulator's value, which we don't care about
       reverse
       (assoc all-state :state)))

(defn pendulum-pos [[origin-x origin-y] {:keys [angle]}]
  (let [arm-length 120
        x (+ origin-x (* arm-length (Math/sin angle)))
        y (+ origin-y (* arm-length (Math/cos angle)))]
    [x y]))

(defn draw-pendulum [[origin-x origin-y] [x y]]
  (with-stroke-weight 3
    (q/line [origin-x origin-y] [x y]))
  (q/with-fill [36 193 224]
    (q/ellipse x y 30 30)))

(defn draw-state [{:keys [state]}]
  (q/background 250)
  (let [x-origin (/ (q/width) 2)
        y-origin (/ (q/height) 2)
        pendulum-xys (reductions pendulum-pos [x-origin y-origin] state)]
    ;; Reverse drawing order so pendulum weights appear on top of bars
    (doseq [[start end] (reverse (partition 2 1 pendulum-xys))]
      (draw-pendulum start end))
    (q/with-fill 0
      (q/ellipse x-origin y-origin 10 10))))

(defn start []
  (q/sketch
    :title "Double pendulum"
    :size [700 700]
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
    :middleware [m/fun-mode m/pause-on-error]))
