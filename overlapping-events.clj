(require '[clojure.set :refer [union]])

; This parses the input and assigns each event an id based on the indexed order in
; which is was entered. It also marks each point as start or end.
; 1 means a starting point, 0 means an ending point. This is so that
; in the case of a tie for time, end-points are sorted before start-points.
; This way, two events that are touching are not considered to overlap.
(defn
  map-input
  [index [start-time end-time]]
  [{:event-id index :point-type 1 :time start-time}
   {:event-id index :point-type 0 :time end-time}])

; The reducer evaluates each element in the start/end-point list.
; It accumulates the open intervals we are currently in.
; For each start-point, it also accumulates any overlapping pairs of events (by id), which will be
; any event that has a start point when we are already in one or more open intervals.
(defn
  reducer
  [{open-intervals :open-intervals overlapping-pairs :overlapping-pairs}
   {event-id :event-id point-type :point-type time :time}]
  {:open-intervals
   (if (= point-type 1)
     (conj open-intervals event-id)
     (disj open-intervals event-id))
   :overlapping-pairs
   (if (= point-type 1)
     (union
      overlapping-pairs
      (set
       (map
        (fn
          [interval]
          [interval event-id])
        open-intervals)))
     overlapping-pairs)})

(println "Enter your events in the form of a nested vector like [[start-time end-time] [start-time end-time]] where each element in the outer vector is a tuple of the start and end time of the event.")
(println "Times can only be numbers, e.g., [[1 2.5] [8 10]].")
(println "The results will be in the form of pairs of overlapping event ids, where the id is based on the 0-indexed position in which the event was entered.")
(println "So, e.g., a response of ([0 1] [2 3]) means that the 0th and 1st event and the 2nd and 3rd event overlap.")
(println
 (sort
  (into
   []
   (:overlapping-pairs
    (reduce
     reducer
     {:open-intervals #{} :overlapping-pairs []}
     (sort-by
      (juxt
       :time
       :point-type)
      (apply
       concat
       (map-indexed
        map-input
        (read-string
         (read-line))))))))))
