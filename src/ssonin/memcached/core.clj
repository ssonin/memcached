(ns ssonin.memcached.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket SocketException]))

(defn- echo-client
  [client-socket]
  (try
    (with-open [in (io/reader client-socket)
                out (io/writer client-socket)]
      (doseq [line (line-seq in)]
        (.write out line)
        (.flush out))
      (println "Client disconnected"))
    (catch SocketException e
      (.println System/err (str "Client disconnected abruptly: " (.getMessage e))))))

(defn -main
  [& args]
  (let [socket (ServerSocket. 11211)]
    (println "Started server on port 11211")
    (while true
      (try
        (let [client-socket (.accept socket)]
          (println "New client connected")
          (future
            (try
              (with-open [client-socket client-socket]
                (echo-client client-socket))
              (catch Exception e
                (.println System/err
                          (str "Unexpected error handling client: " (.getMessage e)))))))
        (catch Exception e
          (.println System/err (str "Server error: " (.getMessage e))))))))
