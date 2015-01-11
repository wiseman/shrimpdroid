(ns com.lemondronor.shrimpdroid.main
  (:require
   [clojure.string :as string]
   [com.lemondronor.turboshrimp :as ar-drone]
   [neko.activity :as activity]
   [neko.context :as context]
   [neko.log :as log]
   [neko.threading :as threading]
   [neko.ui :as ui])
  (:import
   [android.app Activity]
   [android.content BroadcastReceiver Context IntentFilter]
   [android.graphics Typeface]
   [android.os Bundle]
   [android.view MotionEvent]))


(declare ^android.widget.LinearLayout mylayout)


(def actions
  {:takeoff [ar-drone/takeoff :continuous? false]
   :land [ar-drone/land :continuous? false]
   :forward [ar-drone/front]
   :backward [ar-drone/back]
   :left [ar-drone/left]
   :right [ar-drone/right]
   :clockwise [ar-drone/clockwise]
   :counter-clockwise [ar-drone/counter-clockwise]
   :up [ar-drone/up]
   :down [ar-drone/down]})


(def default-speed 0.5)


(defn run-action [controller action-tag ^MotionEvent evt]
  (let [drone @(:drone controller)
        action (.getAction evt)
        [action-fn &
         {:keys [continuous?] :or {continuous? true}}] (actions action-tag)
        action-args (cons drone
                          (cond
                            (and (= action MotionEvent/ACTION_DOWN) continuous?)
                            (list default-speed)
                            (and (= action MotionEvent/ACTION_UP) continuous?)
                            (list 0.0)
                            :else
                            '()))]
    (when (or (= action MotionEvent/ACTION_DOWN)
              (and (= action MotionEvent/ACTION_UP) continuous?))
      (log/i "Running" action-fn action-args)
      (apply action-fn action-args))))


(defn touch-handler [controller action-tag]
  (fn [view evt]
    (run-action controller action-tag evt)
    false))


(defn data-display [label-text id]
  [:linear-layout {:orientation :horizontal
                   :layout-width :fill}
   [:text-view {:text label-text
                :text-size [40 "sp"]
                :typeface Typeface/MONOSPACE
                :layout-margin-left [50 "dp"]
                :layout-margin-right [30 "dp"]}]
   [:text-view {:id id
                :text "0.0"
                :text-size [40 "sp"]
                :typeface Typeface/MONOSPACE}]])


(defn main-layout [controller]
  (let [action (partial touch-handler controller)]
    [:linear-layout {:id-holder true
                     :def `mylayout
                     :layout-width :fill
                     :orientation :vertical}
     [:linear-layout {:orientation :horizontal
                      :layout-width :fill
                      :layout-margin-bottom 100}
      [:button {:text "Takeoff"
                :layout-width 0
                :layout-weight 1.0
                :on-touch (action :takeoff)}]
      [:button {:text "Land"
                :layout-width 0
                :layout-weight 1.0
                :on-touch (action :land)}]]
     [:button {:text "↑"
               :layout-gravity :center-horizontal
               :on-touch (action :forward)}]
     [:linear-layout {:orientation :horizontal
                      :layout-width :wrap
                      :layout-gravity :center-horizontal}
      [:button {:text "←"
                :on-touch (action :left)}]
      [:button {:text "→"
                :on-touch (action :right)}]]
     [:button {:text "↓"
               :layout-gravity :center-horizontal
               :on-touch (action :backward)}]
     [:linear-layout {:orientation :horizontal
                      :layout-width :wrap
                      :layout-gravity :center-horizontal
                      :layout-margin-bottom [40 "dp"]}
      [:button {:text "↺"
                :layout-margin-right [120 "dp"]
                :on-touch (action :counter-clockwise)}]
      [:button {:text "↻"
                :layout-margin-left [120 "dp"]
                :on-touch (action :clockwise)}]]
     (data-display "ALT" :alt)
     (data-display "SPD" :spd)
     (data-display "HDG" :hdg)
     (data-display "LAT" :lat)
     (data-display "LON" :lon)
     (data-display "BAT" :batt)]))


(defn set-elmt [elmt s]
  (threading/on-ui (ui/config (elmt (.getTag mylayout)) :text s))
  s)


(defn magnitude
  "Returns the magnitude of a vector."
  [v]
  (Math/sqrt (reduce + (map #(* % %) v))))


(defn display-navdata [navdata]
  (let [batt (get-in navdata [:demo :battery-percentage])
        pitch (get-in navdata [:demo :theta])
        roll (get-in navdata [:demo :phi])
        hdg (get-in navdata [:magneto :heading :fusion-unwrapped])
        alt (get-in navdata [:demo :altitude])
        vel (get-in navdata [:demo :velocity])
        spd (magnitude (vals vel))
        lat (get-in navdata [:gps :lat-fuse])
        lon (get-in navdata [:gps :lon-fuse])]
    (if spd
      (set-elmt :spd (format "%.1f m/s" (/ spd 1000.0)))
      (set-elmt :spd "unk"))
    (if alt
      (set-elmt :alt (format "%.1f m" (/ alt 1.0)))
      (set-elmt :alt "unk"))
    (if hdg
      (set-elmt :hdg (str (int hdg)))
      (set-elmt :hdg "unk"))
    (if (and lat lon)
      (do
        (set-elmt :lat (format "%.5f" lat))
        (set-elmt :lon (format "%.5f" lon)))
      (do
        (set-elmt :lat "unk")
        (set-elmt :lon "unk")))
    (if batt
      (set-elmt :batt (format "%s %%" batt))
      (set-elmt :batt "unk %"))))


(activity/defactivity com.lemondronor.shrimpdroid.MainActivity
  :key :main
  :on-create
  (fn [this bundle]
    (let [controller {:drone (agent nil)}]
      (log/i "MainActivity activate")
      (threading/on-ui
       (activity/set-content-view!
        (activity/*a)
        (main-layout controller)))
      (send-off
       (:drone controller)
       (fn [_]
         (display-navdata nil)
         (let [drone (ar-drone/make-drone
                      :event-handler (fn [evt navdata]
                                       (when (= evt :navdata)
                                         (display-navdata navdata))))]
           (ar-drone/connect! drone)
           drone))))))
