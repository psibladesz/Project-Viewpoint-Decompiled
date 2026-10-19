package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.platform.KeyInput;
import viewpoint.platform.Keys;

@LuaClass(name = "Viewpoint.Keys")
public final class KeysLua {
   public static int count() {
      return Keys.optionCount();
   }

   public static String id(int var0) {
      return Keys.option(var0);
   }

   public static String label(String var0) {
      return Keys.label(var0);
   }

   public static String tooltip(String var0) {
      return Keys.tooltip(var0);
   }

   public static String group(String var0) {
      return Keys.group(var0);
   }

   public static boolean loot(String var0) {
      return Keys.loot(var0);
   }

   public static String get(String var0) {
      return Keys.text(var0);
   }

   public static String fallback(String var0) {
      return Keys.fallbackText(var0);
   }

   public static String display(String var0) {
      return Keys.display(var0);
   }

   public static int trigger(String var0) {
      return Keys.trigger(var0);
   }

   public static boolean holds(String var0, String var1) {
      return Keys.holds(var0, var1);
   }

   public static String captured(int var0) {
      return KeyInput.captured(var0);
   }

   public static boolean waits(int var0) {
      return KeyInput.waits(var0);
   }

   public static String without(String var0, String var1, String var2, String var3) {
      return Keys.without(var0, var1, var2, var3);
   }

   public static void set(String var0, String var1) {
      Keys.set(var0, var1);
   }

   private KeysLua() {
   }
}
