(ns shadow.devtools.server.web.api
  (:require [shadow.devtools.server.web.common :as common]
            [shadow.devtools.server.services.config :as config]))



(defn root [{:keys [build-config] :as req}]
  (common/transit
    req
    (config/get-configured-builds build-config)))
