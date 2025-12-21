(ns com.moclojer.specs.postman
  (:require
   [clojure.string :as str]))

(defn- parse-url
  "Parses Postman URL object or string to extract path.

  Postman URL can be:
  - String: 'http://localhost:3000/pets/:id'
  - Object: {:raw '...' :path ['pets' ':id']}

  Returns path in moclojer format: /pets/:id"
  [url]
  (cond
    (string? url)
    (-> url
        (str/replace #"^https?://[^/]+" "")
        (str/replace #"\?.*$" "")
        (#(if (str/starts-with? % "/") % (str "/" %))))

    (map? url)
    (let [path-segments (or (:path url) [])]
      (str "/" (str/join "/" path-segments)))

    :else "/"))

(defn- extract-headers
  "Extracts headers from Postman request.

  Postman headers format: [{:key 'Content-Type' :value 'application/json'}]
  Returns: {'Content-Type' 'application/json'}"
  [headers]
  (when (seq headers)
    (reduce (fn [acc {:keys [key value disabled]}]
              (if disabled
                acc
                (assoc acc key value)))
            {}
            headers)))

(defn- extract-response-body
  "Extracts body from Postman response example.

  Returns the body as string or nil if not present."
  [response-example]
  (when-let [body (:body response-example)]
    (if (string? body)
      body
      (pr-str body))))

(defn- pick-response-example
  "Picks the first response example from Postman request.

  Returns map with :status, :body, :headers or default 200 response."
  [response-examples]
  (if-let [example (first response-examples)]
    (let [body (extract-response-body example)
          headers (extract-headers (:header example))]
      (cond-> {:status (or (:code example) 200)}
        body (assoc :body body)
        headers (assoc :headers headers)))
    {:status 200
     :body "{}"
     :headers {"Content-Type" "application/json"}}))

(defn- process-request
  "Converts a Postman request item to moclojer endpoint format.

  Input: Postman item with :request key
  Output: {:endpoint {:method ... :path ... :response ...}}"
  [{:keys [request response]}]
  (when request
    (let [method (str/upper-case (or (:method request) "GET"))
          path (parse-url (:url request))
          response-data (pick-response-example response)]
      {:endpoint {:method method
                  :path path
                  :response response-data}})))

(defn- process-item
  "Recursively processes Postman collection items.

  Items can be:
  - Request: has :request key
  - Folder: has :item key (array of items)

  Returns flat list of endpoints."
  [item]
  (cond
    ;; Folder with nested items
    (:item item)
    (mapcat process-item (:item item))

    ;; Request item
    (:request item)
    [(process-request item)]

    ;; Unknown/empty item
    :else
    []))

(defn ->moclojer
  "Converts Postman Collection v2.1 to moclojer spec.

  Input: Postman collection map with :info and :item keys
  Output: Vector of {:endpoint {...}} maps

  Example:
    Input: {:info {...} :item [{:request {:method 'GET' :url {...}}}]}
    Output: [{:endpoint {:method 'GET' :path '/pets' :response {...}}}]"
  [collection]
  (->> (:item collection)
       (mapcat process-item)
       (filterv some?)))
