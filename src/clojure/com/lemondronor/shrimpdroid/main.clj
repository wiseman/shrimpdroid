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
   [android.os Bundle]
   [android.view KeyEvent]))


(def drone (agent nil))


(declare ^android.widget.LinearLayout mylayout)


(defn touch-handler []
  (fn [& args]
    (log/i "Got event " args)))


(def main-layout
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
              :on-touch (touch-handler)}]
    [:button {:text "Land"
              :layout-width 0
              :layout-weight 1.0
              :on-touch (touch-handler)}]]
   [:button {:text "↑"
             :layout-gravity :center-horizontal}]
   [:linear-layout {:orientation :horizontal
                    :layout-width :wrap
                    :layout-gravity :center-horizontal}
    [:button {:text "←"}]
    [:button {:text "→"}]]
   [:button {:text "↓"
             :layout-gravity :center-horizontal}]
   [:linear-layout {:orientation :horizontal
                    :layout-width :wrap
                    :layout-gravity :center-horizontal}
    [:button {:text "↺"}]
    [:button {:text "↻"}]]])


(defn set-elmt [elmt s]
  (threading/on-ui (ui/config (elmt (.getTag mylayout)) :text s))
  s)


(activity/defactivity com.lemondronor.shrimpdroid.MainActivity
  :key :main
  :on-create
  (fn [this bundle]
    (log/i "MainActivity activate")
    (threading/on-ui
      (activity/set-content-view!
       (activity/*a)
       main-layout))
    (send-off
     drone
     (fn [_]
       (let [drone (ar-drone/make-drone)]
         (ar-drone/connect! drone)
         drone)))))
