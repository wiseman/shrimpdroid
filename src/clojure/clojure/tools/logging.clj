(ns clojure.tools.logging
  (:require
   [clojure.string :as string]
   [neko.log :as log]))


(defmacro error [message & more]
  `(log/e (string/join " " (concat [~message] ~more))))

(defmacro warn [message & more]
  `(log/w (string/join " " (concat [~message] ~more))))

(defmacro info [message & more]
  `(log/i (string/join " " (concat [~message] ~more))))

(defmacro debug [message & more]
  `(log/d (string/join " " (concat [~message] ~more))))
