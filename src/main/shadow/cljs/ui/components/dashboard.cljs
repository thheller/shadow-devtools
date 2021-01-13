(ns shadow.cljs.ui.components.dashboard
  (:require
    [shadow.experiments.grove :as sg :refer (<< defc)]
    [shadow.cljs.model :as m]
    [shadow.cljs.ui.components.build-status :as build-status]
    [shadow.cljs.ui.components.runtimes :as runtimes]))

(defc ui-http-servers []
  (bind {::m/keys [http-servers]}
    (sg/query-root
      [{::m/http-servers
        [::m/http-server-id
         ::m/http-url
         ::m/https-url
         ::m/http-config]}]))

  (render
    (<< [:div.mt-6
         [:div.max-w-7xl.mx-auto
          [:h2.text-lg.leading-6.font-medium.text-gray-900 "HTTP Servers"]
          [:div.mt-2.grid.grid-cols-1.gap-5.sm:grid-cols-2.lg:grid-cols-3
           (sg/render-seq http-servers ::m/http-server-id
             (fn [{::m/keys [http-url https-url http-config]}]
               (let [url (-> (or http-url https-url)
                           (clojure.string/split  #"//")
                           (second))
                     display-name (:display-name http-config)]
                 (<< [:div.bg-white.overflow-hidden.shadow.rounded-lg
                      [:div.p-5
                       [:div.flex.items-center
                        [:div.flex-shrink-0
                         [:svg.h-6.w-6.text-gray-400 {:xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
                          [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01"}]]]
                        [:div.ml-5.w-0.flex-1
                         [:dl
                          [:dt.text-sm.font-medium.text-gray-500.truncate (pr-str (first (:roots http-config)))]
                          [:dd
                           [:div.text-lg.font-medium.text-gray-900 url]]]]]]
                      [:div.bg-gray-50.px-5.py-3
                       [:div.text-sm
                        [:a.font-medium.text-green-700.hover:text-green-900 {:href url :target "_blank"} "View"]]]]))))]]])))

(defc ui-active-build [ident]
  (bind {::m/keys [build-status build-id build-target build-warnings-count] :as data}
    (sg/query-ident ident
      [::m/build-id
       ::m/build-target
       ::m/build-warnings-count
       ::m/build-status
       ::m/build-config-raw]))

  (render
    (let [{:keys [status]} build-status]
      (<< [:div.bg-white.overflow-hidden.shadow.rounded-lg
           [:div.p-5
            [:div.flex.items-center
             [:div.flex-shrink-0
              (let [_ (tap> ["bs" build-status])])
              (case status
                :compiling
                (<< [:span.h-6.w-6.bg-blue-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                     [:span.h-3.w-3.bg-blue-400.rounded-full]])

                :completed
                (if (zero? build-warnings-count)
                  (<< [:span.h-6.w-6.bg-green-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                       [:span.h-3.w-3.bg-green-400.rounded-full]])
                  (<< [:span.h-6.w-6.bg-yellow-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                       [:span.h-3.w-3.bg-yellow-400.rounded-full]]))

                :failed
                (<< [:span.h-6.w-6.bg-red-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                     [:span.h-3.w-3.bg-red-400.rounded-full]])

                :inactive
                (<< [:span.h-6.w-6.bg-gray-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                     [:span.h-3.w-3.bg-gray-400.rounded-full]])

                :pending
                (<< [:span.h-6.w-6.bg-blue-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                     [:span.h-3.w-3.bg-blue-400.rounded-full]])

                (<< [:span.h-6.w-6.bg-gray-100.rounded-full.flex.items-center.justify-center {:aria-hidden "true"}
                     [:span.h-3.w-3.bg-gray-400.rounded-full]]))]
             [:div.ml-5.w-0.flex-1
              [:dl
               [:dt.text-sm.font-medium.text-gray-500.truncate (name build-target)]
               [:dd
                [:div.text-lg.font-medium.text-gray-900 (name build-id)]]]]]]
           [:div.bg-gray-50.px-5.py-3
            [:div.text-sm
             [:a.font-medium.text-green-700.hover:text-green-900 {:href (str "/build/" (name build-id))} "View"]]]]))))

(defc ui-active-builds []
  (bind {::m/keys [active-builds]}
    (sg/query-root [::m/active-builds]))

  (render
    (<< [:div.mt-8
         [:div.max-w-7xl.mx-auto
          [:h2.text-lg.leading-6.font-medium.text-gray-900 "Builds"]
          [:div.mt-2.grid.grid-cols-1.gap-5.sm:grid-cols-2.lg:grid-cols-3
           (sg/render-seq active-builds identity ui-active-build)]]])))

(defc ui-active-runtimes []
  (bind {::m/keys [runtimes-sorted]}
    (sg/query-root
      [{::m/runtimes-sorted
        [:runtime-id
         :runtime-info
         :supported-ops]}]))

  (render
    (<< [:div.mt-8
         [:div.max-w-7xl.mx-auto
          [:h2.text-lg.leading-6.font-medium.text-gray-900 "Runtimes"]
          (runtimes/ui-page)]])))

(defn ui-page []
  (<< [:div.flex-1.overflow-auto.py-2
       [:div.max-w-7xl.mx-auto
        [:div.flex.flex-col
         [:div.align-middle.min-w-full.overflow-x-auto.overflow-hidden
          (ui-http-servers)
          (ui-active-builds)
          (ui-active-runtimes)]]]]))