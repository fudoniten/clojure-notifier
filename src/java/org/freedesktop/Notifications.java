package org.freedesktop;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public interface Notifications extends DBusInterface {
  public List<String> GetCapabilities();

  public UInt32 Notify(
                       String app_name,
                       UInt32 replaces_id,
                       String app_icon,
                       String summary,
                       String body,
                       List<String> actions,
                       Map<String, Variant<?>> hints,
                       int expire_timeout);
}
