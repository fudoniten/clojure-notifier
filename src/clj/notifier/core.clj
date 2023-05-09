(ns notifier.core
  (:import org.freedesktop.Notifications
           org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
           org.freedesktop.dbus.types.UInt32))

(def ^:private ^:const NOTIFICATIONS_PATH "/org/freedesktop/Notifications")
(def ^:private ^:const NOTIFICATIONS_BUS "org.freedesktop.Notifications")

(def urgencies {:low 0 :normal 1 :critical 2})

(defn connect-session-bus []
  (-> (DBusConnectionBuilder/forSessionBus)
      (.build)
      (.getRemoteObject NOTIFICATIONS_BUS NOTIFICATIONS_PATH Notifications)))

(defn send-notification!
  [bus
   {:keys [app replace-id icon summary body actions timeout urgency]
    :or   {replace-id 0
           icon       ""
           actions    []
           timeout    -1
           urgency    :low}
    :as    args}]
  (doseq [arg [:app :summary :body]]
    (when (not (contains? args arg))
      (throw (ex-info (format "missing required argument: %s" arg)
                      {:arg arg}))))
  (let [urgency-lvl (get urgencies urgency)]
    (when (nil? urgency-lvl)
      (throw (ex-info (format "bad urgency level: %s" urgency) {}))
    (.Notify bus
             app
             (UInt32. replace-id)
             icon
             summary
             body
             actions
             { "urgency" urgency-lvl }
             timeout))))
