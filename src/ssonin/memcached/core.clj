(ns ssonin.memcached.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :as cli])
  (:import [java.net ServerSocket SocketException]))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 11211
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 65536) "Must be a number between 0 and 65536"]]])

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

(defn- start
  [port]
  (let [socket (ServerSocket. port)]
    (println "Started server on port" port)
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

(defn -main
  [& args]
  (let [{:keys [options errors]} (cli/parse-opts args cli-options)]
    (cond
      errors
      (doseq [error errors]
        (println "Error:" error)
        (System/exit 1))
      :else
      (start (:port options)))))
