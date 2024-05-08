(ns ssonin.memcached.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(declare echo-client)

(defn -main
  [& args]
  (let [socket (ServerSocket. 11211)]
    (echo-client (.accept socket))))

(defn- echo-client
  [client-socket]
  (let [in (io/reader client-socket)
        out (io/writer client-socket)]
    (while true
      (doseq [line (line-seq in)]
        (.write out line)
        (.flush out)))))