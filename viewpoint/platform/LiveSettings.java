package viewpoint.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import zombie.ZomboidFileSystem;

public final class LiveSettings {
   public static final String FILE = "viewpoint-live.properties";
   private static final List<LiveSettings.Setting> ALL = Collections.synchronizedList(new ArrayList<>());
   private static final Map<String, String> pending = new HashMap<>();
   private static final Map<String, String> carried = new HashMap<>();
   private static volatile boolean loaded;
   private static volatile Properties saved = new Properties();

   public static LiveSettings.Number number(String var0, String var1, String var2, float var3, float var4, float var5, float var6) {
      return add(new LiveSettings.Number(var0, var1, var2, var3, var4, var5, var6));
   }

   public static LiveSettings.Toggle toggle(String var0, String var1, String var2, boolean var3) {
      return add(new LiveSettings.Toggle(var0, var1, var2, var3));
   }

   public static LiveSettings.Choice choice(String var0, String var1, String var2, String[] var3, int var4) {
      return choice(var0, var1, var2, var3, new String[0], var4);
   }

   public static LiveSettings.Choice choice(String var0, String var1, String var2, String[] var3, String[] var4, int var5) {
      return add(new LiveSettings.Choice(var0, var1, var2, var3, var4, var5));
   }

   static <T extends LiveSettings.Setting> T add(T var0) {
      String var1;
      synchronized (ALL) {
         for (LiveSettings.Setting var4 : ALL) {
            if (var4.key.equals(var0.key)) {
               System.out.println("[Viewpoint] setting " + var0.key + " declared twice: the window lists both");
            }
         }

         ALL.add(var0);
         var1 = pending.remove(var0.key);
      }

      if (loaded) {
         apply(var0, saved);
      }

      if (var1 != null) {
         var0.read(var1);
      }

      return (T)var0;
   }

   static void remove(LiveSettings.Setting var0) {
      synchronized (ALL) {
         ALL.remove(var0);
      }
   }

   static List<LiveSettings.Setting> all() {
      synchronized (ALL) {
         return new ArrayList<>(ALL);
      }
   }

   static LiveSettings.Setting find(String var0) {
      synchronized (ALL) {
         for (LiveSettings.Setting var3 : ALL) {
            if (var3.key.equals(var0)) {
               return var3;
            }
         }

         return null;
      }
   }

   static void toDefault(String var0) {
      LiveSettings.Setting var1;
      synchronized (ALL) {
         var1 = find(var0);
         if (var1 == null) {
            pending.remove(var0);
            saved.remove(var0);
            return;
         }
      }

      toDefault(var1);
   }

   static void toDefault(LiveSettings.Setting var0) {
      if (var0 instanceof LiveSettings.Number var2) {
         var2.set(var2.fallback);
      } else if (var0 instanceof LiveSettings.Toggle var3) {
         var3.set(var3.fallback);
      } else {
         if (!(var0 instanceof LiveSettings.Choice var1)) {
            throw new IllegalArgumentException(var0.key + " is not a number, a toggle or a choice");
         }

         var1.set(var1.fallback);
      }
   }

   static void put(String var0, String var1) {
      LiveSettings.Setting var2;
      synchronized (ALL) {
         var2 = find(var0);
         if (var2 == null) {
            pending.put(var0, var1);
            return;
         }
      }

      var2.read(var1);
   }

   static String value(String var0) {
      synchronized (ALL) {
         LiveSettings.Setting var2 = find(var0);
         if (var2 != null) {
            return var2.text();
         }

         String var3 = pending.get(var0);
         return var3 != null ? var3 : saved.getProperty(var0);
      }
   }

   static String saved(String var0) {
      return saved.getProperty(var0);
   }

   static boolean forget(Predicate<String> var0) {
      Properties var1 = new Properties();
      saved.forEach((var2x, var3) -> {
         if (!var0.test((String) var2x)) {
            var1.put(var2x, var3);
         }
      });
      boolean var2 = var1.size() < saved.size();
      saved = var1;
      return var2;
   }

   static void carryOver(String var0, String var1) {
      synchronized (ALL) {
         carried.put(var0, var1);
      }

      if (loaded && saved.getProperty(var0) == null) {
         put(var0, var1);
      }
   }

   static boolean load() {
      if (loaded) {
         return false;
      }

      load(read());
      return true;
   }

   static void load(Properties var0) {
      saved = var0;
      loaded = true;

      for (LiveSettings.Setting var2 : all()) {
         apply(var2, saved);
      }

      HashMap<String, String> var5;
      synchronized (ALL) {
         var5 = new HashMap<>(carried);
      }

      for (Entry<String, String> var3 : var5.entrySet()) {
         if (saved.getProperty(var3.getKey()) == null) {
            put(var3.getKey(), var3.getValue());
         }
      }
   }

   private static void apply(LiveSettings.Setting var0, Properties var1) {
      String var2 = var1.getProperty(var0.key);
      if (var2 != null && var0.kept) {
         var0.read(var2);
      }
   }

   private static Properties read() {
      Properties var0 = new Properties();
      File var1 = file();
      if (var1.isFile()) {
         try (FileInputStream var2 = new FileInputStream(var1)) {
            var0.load(var2);
         } catch (Exception var7) {
            System.out.println("[Viewpoint] could not read " + var1 + ": " + var7);
         }
      }

      return var0;
   }

   static void save() {
      Properties var0 = new Properties();
      var0.putAll(saved);
      synchronized (ALL) {
         var0.putAll(pending);
      }

      for (LiveSettings.Setting var2 : all()) {
         if (var2.kept) {
            var0.setProperty(var2.key, var2.text());
         } else {
            var0.remove(var2.key);
         }
      }

      saved = var0;

      try (FileOutputStream var9 = new FileOutputStream(file())) {
         var0.store(var9, "Viewpoint's live settings: the settings window (Delete) writes this file. Edit it by hand only with the game closed.");
      } catch (Exception var6) {
         System.out.println("[Viewpoint] could not write " + file() + ": " + var6);
      }
   }

   private static File file() {
      return new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-live.properties");
   }

   private LiveSettings() {
   }

   public static final class Choice extends LiveSettings.Setting {
      final String[] options;
      final int fallback;
      private final String[] former;
      private volatile int value;
      private volatile String[] labels;
      private volatile boolean slider;

      Choice(String var1, String var2, String var3, String[] var4, String[] var5, int var6) {
         super(var1, var2, var3);
         this.options = (String[])var4.clone();
         this.former = (String[])var5.clone();
         this.fallback = var6;
         this.value = var6;
      }

      public int get() {
         return this.value;
      }

      public void shownAs(String[] var1) {
         this.labels = var1.length == this.options.length ? (String[])var1.clone() : null;
      }

      public void asSlider() {
         this.slider = true;
      }

      boolean slider() {
         return this.slider;
      }

      String[] labels() {
         String[] var1 = this.labels;
         return var1 != null ? var1 : this.options;
      }

      public void set(int var1) {
         if (var1 >= 0 && var1 < this.options.length) {
            this.value = var1;
         }
      }

      @Override
      String text() {
         return this.options[this.value];
      }

      @Override
      void read(String var1) {
         int var2 = index(this.options, var1);
         if (var2 < 0) {
            var2 = index(this.former, var1);
         }

         if (var2 >= 0) {
            this.value = var2;
         } else {
            System.out.println("[Viewpoint] viewpoint-live.properties: " + this.key + " is none of its options: " + var1);
         }
      }

      private static int index(String[] var0, String var1) {
         for (int var2 = 0; var2 < var0.length; var2++) {
            if (var0[var2].equalsIgnoreCase(var1.trim())) {
               return var2;
            }
         }

         return -1;
      }

      @Override
      boolean matches(String var1) {
         return this.options[this.value].equalsIgnoreCase(var1.trim());
      }
   }

   public static final class Number extends LiveSettings.Setting {
      final float min;
      final float max;
      final float step;
      final float fallback;
      private volatile float value;

      Number(String var1, String var2, String var3, float var4, float var5, float var6, float var7) {
         super(var1, var2, var3);
         this.min = var4;
         this.max = var5;
         this.step = var6;
         this.fallback = var7;
         this.value = var7;
      }

      public float get() {
         return this.value;
      }

      public int getInt() {
         return Math.round(this.value);
      }

      public void set(float var1) {
         this.value = this.snap(var1);
      }

      public LiveSettings.Number unsaved() {
         this.kept = false;
         this.value = this.fallback;
         return this;
      }

      private float snap(float var1) {
         float var2 = this.step > 0.0F ? Math.round((var1 - this.min) / this.step) * this.step + this.min : var1;
         return Math.max(this.min, Math.min(this.max, var2));
      }

      @Override
      String text() {
         return this.step >= 1.0F ? Integer.toString(this.getInt()) : Float.toString(this.value);
      }

      @Override
      void read(String var1) {
         try {
            this.set(Float.parseFloat(var1.trim()));
         } catch (NumberFormatException var3) {
            System.out.println("[Viewpoint] viewpoint-live.properties: " + this.key + " is not a number: " + var1);
         }
      }

      @Override
      boolean matches(String var1) {
         try {
            return Math.abs(this.snap(Float.parseFloat(var1.trim())) - this.value) <= 1.0E-5F * Math.max(1.0F, this.value);
         } catch (NumberFormatException var3) {
            return false;
         }
      }
   }

   public abstract static sealed class Setting permits LiveSettings.Number, LiveSettings.Toggle, LiveSettings.Choice, KeyBind, PadBind {
      final String key;
      final String label;
      final String section;
      private volatile BooleanSupplier shown = () -> true;
      private volatile BooleanSupplier enabled = () -> true;
      private volatile String tooltip = "";
      private volatile List<String> also = List.of();
      volatile boolean kept = true;

      Setting(String var1, String var2, String var3) {
         this.key = var1;
         this.label = var2;
         this.section = var3;
      }

      abstract String text();

      abstract void read(String var1);

      abstract boolean matches(String var1);

      public void shownWhen(BooleanSupplier var1) {
         this.shown = var1;
      }

      boolean shown() {
         return this.shown.getAsBoolean();
      }

      public void enabledWhen(BooleanSupplier var1) {
         this.enabled = var1;
      }

      boolean enabled() {
         return this.enabled.getAsBoolean();
      }

      public void describe(String var1) {
         this.tooltip = var1;
      }

      String tooltip() {
         return this.tooltip;
      }

      public void alsoIn(String var1) {
         ArrayList<String> var2 = new ArrayList<>(this.also);
         var2.add(var1);
         this.also = List.copyOf(var2);
      }

      List<String> also() {
         return this.also;
      }
   }

   public static final class Toggle extends LiveSettings.Setting {
      final boolean fallback;
      private volatile boolean value;

      Toggle(String var1, String var2, String var3, boolean var4) {
         super(var1, var2, var3);
         this.fallback = var4;
         this.value = var4;
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean var1) {
         this.value = var1;
      }

      public LiveSettings.Toggle unsaved() {
         this.kept = false;
         this.value = this.fallback;
         return this;
      }

      @Override
      String text() {
         return Boolean.toString(this.value);
      }

      @Override
      void read(String var1) {
         this.value = Boolean.parseBoolean(var1.trim());
      }

      @Override
      boolean matches(String var1) {
         return Boolean.parseBoolean(var1.trim()) == this.value;
      }
   }
}
