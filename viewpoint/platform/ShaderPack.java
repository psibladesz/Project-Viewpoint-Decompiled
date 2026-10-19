package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShaderPack {
   public static final String PROPERTIES = "pack.properties";
   private static final String SHADERS = "shaders/";
   private static final Pattern OPTION = Pattern.compile("\\$\\{([A-Za-z][A-Za-z0-9]*)\\}");
   public final String id;
   public final String name;
   public final String description;
   public final String author;
   public final ShaderPack base;
   public final boolean builtIn;
   public final List<PackOption> options;
   public final List<PackPreset> presets;
   public final String firstPreset;
   public final List<PackTarget> targets;
   public final List<PackPass> passes;
   private final Map<Pipeline.Knob, String> pipeline;
   private final ShaderPack.PackFiles files;

   private ShaderPack(
           String var1,
           Properties var2,
           ShaderPack var3,
           boolean var4,
           List<PackOption> var5,
           PackPreset.Ladder var6,
           PackPass.Graph var7,
           Map<Pipeline.Knob, String> var8,
           ShaderPack.PackFiles var9
   ) {
      this.id = var1;
      this.name = var2.getProperty("name", var1).trim();
      this.description = var2.getProperty("description", "").trim();
      this.author = var2.getProperty("author", "").trim();
      this.base = var3;
      this.builtIn = var4;
      this.options = List.copyOf(var5);
      this.presets = var6.presets();
      this.firstPreset = var6.first();
      this.targets = var7.targets();
      this.passes = var7.passes();
      this.pipeline = var8;
      this.files = var9;
   }

   static ShaderPack parse(String var0, ShaderPack.PackFiles var1, Function<String, ShaderPack> var2, boolean var3) throws IOException {
      String var4 = var1.read("pack.properties");
      if (var4 == null) {
         throw new IllegalArgumentException("no pack.properties");
      }

      Properties var5 = new Properties();
      var5.load(new StringReader(var4));
      String var6 = var5.getProperty("base", "").trim();
      ShaderPack var7 = var6.isEmpty() ? null : var2.apply(var6);
      if (!var6.isEmpty() && var7 == null) {
         throw new IllegalArgumentException("base " + var6 + ": no such pack");
      }

      ArrayList<PackOption> var8 = new ArrayList<>(var7 == null ? List.<PackOption>of() : var7.options);
      ArrayList<String> var9 = new ArrayList<>();

      for (String var13 : var5.getProperty("dropOptions", "").trim().split("\\s+")) {
         if (!var13.isEmpty() && !var8.removeIf(var1x -> var1x.id.equals(var13))) {
            throw new IllegalArgumentException("dropOptions: " + var13 + " is not an option of its base");
         }
      }

      for (String var24 : var5.getProperty("options", "").trim().split("\\s+")) {
         if (!var24.isEmpty()) {
            PackOption var14 = PackOption.parse(var24, var5);
            var9.add(var24);
            int var15 = indexOf(var8, var24);
            if (var15 >= 0) {
               var8.set(var15, var14);
            } else {
               var8.add(var14);
            }
         }
      }

      EnumMap<Pipeline.Knob, String> var17 = var7 == null ? new EnumMap<>(Pipeline.Knob.class) : new EnumMap<>(var7.pipeline);

      for (String var22 : var5.stringPropertyNames()) {
         if (var22.startsWith("pipeline.")) {
            Pipeline.Knob var25 = Pipeline.Knob.named(var22.substring("pipeline.".length()));
            if (var25 == null) {
               throw new IllegalArgumentException(var22 + ": no such pipeline knob");
            }

            var17.put(var25, var5.getProperty(var22).trim());
         }
      }

      checkPipeline(var17, var8);
      checkDisabledBy(var8);
      PackPreset.Ladder var20 = var7 == null
              ? PackPreset.parse(var5, var8, var9, List.of(), null)
              : PackPreset.parse(var5, var8, var9, var7.presets, var7.firstPreset);
      PackPass.Graph var23 = PackPass.parse(var5, var7 == null ? new PackPass.Graph(List.of(), List.of()) : new PackPass.Graph(var7.targets, var7.passes), var8);
      unusedDrivers(var0, var1);
      return new ShaderPack(var0, var5, var7, var3, var8, var20, var23, var17, var1);
   }

   private static void unusedDrivers(String var0, ShaderPack.PackFiles var1) {
      for (String var3 : Shaders.DRIVERS) {
         if (var1.modified("shaders/" + var3) >= 0L) {
            System.out
                    .println(
                            "[Viewpoint] shader pack "
                                    + var0
                                    + ": its "
                                    + var3
                                    + " is not used: the weather is the renderer's, its look the pack's (the rain's in lib/precip_look.glsl)"
                    );
         }
      }
   }

   private static void checkPipeline(Map<Pipeline.Knob, String> var0, List<PackOption> var1) {
      for (Entry<Pipeline.Knob, String> var3 : var0.entrySet()) {
         Matcher var4 = OPTION.matcher(var3.getValue());
         if (!var4.matches()) {
            var3.getKey().parse(var3.getValue());
         } else {
            int var5 = indexOf(var1, var4.group(1));
            if (var5 < 0) {
               throw new IllegalArgumentException("pipeline." + var3.getKey().key + ": no option " + var4.group(1));
            }

            for (String var7 : var1.get(var5).extremes()) {
               var3.getKey().parse(var7);
            }
         }
      }
   }

   private static void checkDisabledBy(List<PackOption> var0) {
      for (PackOption var2 : var0) {
         if (!var2.disabledBy.isEmpty()) {
            int var3 = indexOf(var0, var2.disabledBy);
            if (var3 < 0 || var2.disabledBy.equals(var2.id) || var0.get(var3).kind == PackOption.Kind.NUMBER) {
               throw new IllegalArgumentException("option." + var2.id + ".disabledBy: " + var2.disabledBy + " is not another toggle or choice of the pack");
            }
         }
      }
   }

   private static int indexOf(List<PackOption> var0, String var1) {
      for (int var2 = 0; var2 < var0.size(); var2++) {
         if (var0.get(var2).id.equals(var1)) {
            return var2;
         }
      }

      return -1;
   }

   public PackOption option(String var1) {
      int var2 = indexOf(this.options, var1);
      return var2 < 0 ? null : this.options.get(var2);
   }

   public PackPreset preset(String var1) {
      for (PackPreset var3 : this.presets) {
         if (var3.id.equals(var1)) {
            return var3;
         }
      }

      throw new IllegalArgumentException("shader pack " + this.id + " has no preset " + var1);
   }

   Pipeline pipeline(Function<PackOption, String> var1) {
      EnumMap<Pipeline.Knob, String> var2 = new EnumMap<>(Pipeline.Knob.class);

      for (Entry<Pipeline.Knob, String> var4 : this.pipeline.entrySet()) {
         Matcher var5 = OPTION.matcher(var4.getValue());
         var2.put(var4.getKey(), var5.matches() ? var1.apply(this.option(var5.group(1))) : var4.getValue());
      }

      return Pipeline.of(var2);
   }

   String defines(Function<PackOption, String> var1, Pipeline var2) {
      StringBuilder var3 = new StringBuilder(var2.defines);

      for (PackOption var5 : this.options) {
         var5.define(var3, var1.apply(var5));
      }

      return var3.toString();
   }

   String shader(String var1) {
      try {
         return this.files.read("shaders/" + var1);
      } catch (IOException var3) {
         throw new IllegalStateException("shader pack " + this.id + ": cannot read " + var1 + ": " + var3, var3);
      }
   }

   long modified(String var1) {
      return this.files.modified("shaders/" + var1);
   }

   static ShaderPack.PackFiles folder(final File var0) {
      return new ShaderPack.PackFiles() {
         @Override
         public String read(String var1) throws IOException {
            File var2 = new File(var0, var1);
            return var2.isFile() ? ShaderPack.plain(Files.readString(var2.toPath(), StandardCharsets.UTF_8)) : null;
         }

         @Override
         public long modified(String var1) {
            File var2 = new File(var0, var1);
            return var2.isFile() ? var2.lastModified() : -1L;
         }
      };
   }

   static ShaderPack.PackFiles jar(final String var0) {
      final String var1 = "/viewpoint/shaderpacks/" + var0 + "/";
      final ConcurrentHashMap<String, Boolean> var2 = new ConcurrentHashMap<>();
      return new ShaderPack.PackFiles() {
         @Override
         public String read(String var1x) throws IOException {
            File var2x = ShaderPack.devFile(var0, var1x);
            if (var2x != null) {
               return ShaderPack.plain(Files.readString(var2x.toPath(), StandardCharsets.UTF_8));
            }

            try (InputStream var3 = ShaderPack.class.getResourceAsStream(var1 + var1x)) {
               return var3 == null ? null : ShaderPack.plain(new String(var3.readAllBytes(), StandardCharsets.UTF_8));
            }
         }

         @Override
         public long modified(String var1x) {
            File var2x = ShaderPack.devFile(var0, var1x);
            if (var2x != null) {
               return var2x.lastModified();
            } else {
               return var2.computeIfAbsent(var1x, var1xxx -> ShaderPack.class.getResource(var1 + var1xxx) != null) ? 0L : -1L;
            }
         }
      };
   }

   private static File devFile(String var0, String var1) {
      String var2 = Settings.dev ? Settings.shaderDir : null;
      if (var2 != null && !var2.isEmpty()) {
         File var3 = new File(new File(new File(var2).getParentFile(), "shaderpacks"), var0 + "/" + var1);
         return var3.isFile() ? var3 : null;
      } else {
         return null;
      }
   }

   private static String plain(String var0) {
      return var0.replace("\r\n", "\n");
   }

   @Override
   public String toString() {
      return this.id;
   }

   interface PackFiles {
      String read(String var1) throws IOException;

      long modified(String var1);
   }
}