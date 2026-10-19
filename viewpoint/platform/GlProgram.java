package viewpoint.platform;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class GlProgram {
   private static final List<GlProgram> all = new ArrayList<>();
   private static final Map<String, GlProgram> passes = new LinkedHashMap<>();
   private static boolean passesLinked = true;
   public static int undeclared;
   private static int adopted;
   private static final FloatBuffer matrix3 = BufferUtils.createFloatBuffer(9);
   private static ShaderPacks.Active pack;
   private static String lastError = "";
   private static Consumer<GlProgram> onUse = var0 -> {};
   final String name;
   private final String vertexPath;
   private final String fragmentPath;
   private int id;
   private String source = "";
   private boolean fromPack;
   private final HashMap<String, Integer> locations = new HashMap<>();
   private final Uniforms values = new Uniforms();
   private final LinkedHashMap<String, Integer> samplers = new LinkedHashMap<>();
   private final HashMap<String, Long> files = new HashMap<>();

   private GlProgram(String var1, String var2, String var3) {
      this.name = var1;
      this.vertexPath = var2;
      this.fragmentPath = var3;
   }

   public static GlProgram fromSources(String var0, GlProgram.Sources var1, Map<String, Integer> var2, Map<String, Integer> var3, List<String> var4) {
      int var5 = GL20.glCreateProgram();
      int[] var6 = new int[]{
              foreign(var0, 35633, var1.vertex(), var4),
              foreign(var0, 36488, var1.control(), var4),
              foreign(var0, 36487, var1.evaluation(), var4),
              foreign(var0, 36313, var1.geometry(), var4),
              foreign(var0, 35632, var1.fragment(), var4)
      };

      for (int var10 : var6) {
         if (var10 > 0) {
            GL20.glAttachShader(var5, var10);
         } else if (var10 < 0) {
            deleteAll(var5, var6);
            throw new IllegalStateException(lastError);
         }
      }

      var2.forEach((var1x, var2x) -> GL20.glBindAttribLocation(var5, var2x, var1x));
      var3.forEach((var1x, var2x) -> GL30.glBindFragDataLocation(var5, var2x, var1x));
      String var11 = (var1.vertex() == null ? "" : var1.vertex()) + "\n" + (var1.evaluation() == null ? "" : var1.evaluation()) + "\n" + var1.fragment();
      return linkForeign(var0, var5, var6, var11);
   }

   public static GlProgram compute(String var0, String var1, List<String> var2) {
      int var3 = GL20.glCreateProgram();
      int[] var4 = new int[]{foreign(var0, 37305, var1, var2)};
      if (var4[0] < 0) {
         deleteAll(var3, var4);
         throw new IllegalStateException(lastError);
      } else {
         GL20.glAttachShader(var3, var4[0]);
         return linkForeign(var0, var3, var4, var1);
      }
   }

   private static GlProgram linkForeign(String var0, int var1, int[] var2, String var3) {
      GL20.glLinkProgram(var1);
      if (GL20.glGetProgrami(var1, 35714) == 0) {
         String var9 = GL20.glGetProgramInfoLog(var1);
         deleteAll(var1, var2);
         lastError = var0 + " program link failed: " + var9;
         throw new IllegalStateException(lastError);
      }

      for (int var7 : var2) {
         if (var7 > 0) {
            GL20.glDetachShader(var1, var7);
            GL20.glDeleteShader(var7);
         }
      }

      GlProgram var8 = new GlProgram(var0, null, null);
      var8.adopt(new GlProgram.Linked(var1, var3, Map.of(), true));
      return var8;
   }

   private static int foreign(String var0, int var1, String var2, List<String> var3) {
      if (var2 == null) {
         return 0;
      } else {
         int var4 = GL20.glCreateShader(var1);
         GL20.glShaderSource(var4, var2);
         GL20.glCompileShader(var4);
         if (GL20.glGetShaderi(var4, 35713) == 0) {
            lastError = var0 + " shader compile failed: " + Shaders.explain(GL20.glGetShaderInfoLog(var4), var3);
            GL20.glDeleteShader(var4);
            return -1;
         } else {
            return var4;
         }
      }
   }

   private static void deleteAll(int var0, int[] var1) {
      for (int var5 : var1) {
         if (var5 > 0) {
            GL20.glDeleteShader(var5);
         }
      }

      GL20.glDeleteProgram(var0);
   }

   public void delete() {
      if (this.id != 0) {
         GL20.glDeleteProgram(this.id);
         this.id = 0;
      }
   }

   public int id() {
      return this.id;
   }

   public static GlProgram create(String var0, String var1, String var2) {
      GlProgram var3 = new GlProgram(var0, var1, var2);
      GlProgram.Linked var4 = var3.link(pack());
      if (var4 != null) {
         var3.adopt(var4);
      }

      all.add(var3);
      return var3;
   }

   public static ShaderPacks.Active pack() {
      if (pack == null) {
         pack = ShaderPacks.active();
      }

      return pack;
   }

   public static boolean linkPasses() {
      Map<String, GlProgram> var0 = makePasses(pack());
      passesLinked = var0 != null;
      if (var0 != null) {
         passes.putAll(var0);
      }

      return passesLinked;
   }

   public static GlProgram pass(String var0) {
      return passes.get(var0);
   }

   public static String lastError() {
      return lastError;
   }

   public static void forEach(Consumer<GlProgram> var0) {
      all.forEach(var0);
      passes.values().forEach(var0);
   }

   public static int links() {
      return adopted;
   }

   public static String relinkAll(ShaderPacks.Active var0) {
      ArrayList<GlProgram.Linked> var1 = new ArrayList<>();

      for (GlProgram var3 : all) {
         GlProgram.Linked var4 = var3.link(var0);
         if (var4 == null) {
            for (GlProgram.Linked var6 : var1) {
               GL20.glDeleteProgram(var6.id);
            }

            return var3.name + ": " + lastError;
         }

         var1.add(var4);
      }

      Map<String, GlProgram> var7 = makePasses(var0);
      if (var7 == null) {
         for (GlProgram.Linked var13 : var1) {
            GL20.glDeleteProgram(var13.id);
         }

         return lastError;
      } else {
         for (int var8 = 0; var8 < all.size(); var8++) {
            GlProgram var11 = all.get(var8);
            if (var11.id != 0) {
               GL20.glDeleteProgram(var11.id);
            }

            var11.adopt(var1.get(var8));
         }

         for (GlProgram var12 : passes.values()) {
            GL20.glDeleteProgram(var12.id);
         }

         passes.clear();
         passes.putAll(var7);
         passesLinked = true;
         pack = var0;
         GL20.glUseProgram(0);
         return null;
      }
   }

   public static boolean allLinked() {
      boolean var0 = passesLinked;

      for (GlProgram var2 : all) {
         var0 &= var2.id != 0;
      }

      return var0;
   }

   public void use() {
      GL20.glUseProgram(this.id);
      onUse.accept(this);
   }

   public static void onUse(Consumer<GlProgram> var0) {
      onUse = var0;
   }

   public int location(String var1) {
      if (this.id == 0) {
         return -1;
      }

      Integer var2 = this.locations.get(var1);
      if (var2 == null) {
         var2 = GL20.glGetUniformLocation(this.id, var1);
         this.locations.put(var1, var2);
         if (var2 < 0 && !this.fromPack && !this.declares(var1)) {
            undeclared++;
            System.out.println("[Viewpoint] shader program " + this.name + " has no uniform " + var1 + " (a typo, or a leftover)");
         }
      }

      return var2;
   }

   public boolean has(String var1) {
      if (this.id == 0) {
         return false;
      }

      Integer var2 = this.locations.get(var1);
      if (var2 == null) {
         var2 = GL20.glGetUniformLocation(this.id, var1);
         this.locations.put(var1, var2);
      }

      return var2 >= 0;
   }

   private boolean declares(String var1) {
      return Pattern.compile("\\b" + Pattern.quote(var1) + "\\b").matcher(this.source).find();
   }

   public void set(String var1, float var2) {
      this.values.set(this.location(var1), var2);
   }

   public void set(String var1, float var2, float var3) {
      this.values.set(this.location(var1), var2, var3);
   }

   public void set(String var1, float var2, float var3, float var4) {
      this.values.set(this.location(var1), var2, var3, var4);
   }

   public void set(String var1, float var2, float var3, float var4, float var5) {
      this.values.set(this.location(var1), var2, var3, var4, var5);
   }

   public void setInt(String var1, int var2) {
      this.values.setInt(this.location(var1), var2);
   }

   public void set(String var1, Matrix4f var2) {
      this.values.set(this.location(var1), var2);
   }

   public void set(String var1, Matrix3f var2) {
      matrix3.clear();
      var2.get(matrix3);
      GL20.glUniformMatrix3fv(this.location(var1), false, matrix3);
   }

   public void setMatrices(String var1, FloatBuffer var2) {
      GL20.glUniformMatrix4fv(this.location(var1), false, var2);
   }

   public void setVec4s(String var1, FloatBuffer var2) {
      GL20.glUniform4fv(this.location(var1), var2);
   }

   public void setVec3s(String var1, FloatBuffer var2) {
      GL20.glUniform3fv(this.location(var1), var2);
   }

   public void setFloats(String var1, FloatBuffer var2) {
      GL20.glUniform1fv(this.location(var1), var2);
   }

   public void sampler(String var1, int var2) {
      this.samplers.put(var1, var2);
      if (this.id != 0) {
         GL20.glUseProgram(this.id);
         GL20.glUniform1i(this.location(var1), var2);
         this.values.forget(this.location(var1));
      }
   }

   public static void reloadChanged() {
      Map<String, Long> var0 = ShaderWatch.stamps();
      HashSet<String> var1 = new HashSet<>();
      ArrayList<GlProgram> var2 = new ArrayList<>(all);
      var2.addAll(passes.values());

      for (GlProgram var4 : var2) {
         var1.addAll(var4.files.keySet());
         if (var4.changed(var0)) {
            GlProgram.Linked var5 = var4.link(pack());
            if (var5 == null) {
               System.out.println("[Viewpoint] shader reload: " + var4.name + " did not compile, the running version stays");
            } else {
               if (var4.id != 0) {
                  GL20.glDeleteProgram(var4.id);
               }

               var4.adopt(var5);
               System.out.println("[Viewpoint] shader reload: " + var4.name);
            }
         }
      }

      ShaderWatch.watch(var1);
      GL20.glUseProgram(0);
   }

   private boolean changed(Map<String, Long> var1) {
      for (Entry<String, Long> var3 : this.files.entrySet()) {
         Long var4 = var1.get(var3.getKey());
         if (var4 != null && !var4.equals(var3.getValue())) {
            return true;
         }
      }

      return false;
   }

   private void adopt(GlProgram.Linked var1) {
      adopted++;
      this.id = var1.id;
      this.source = var1.source;
      this.fromPack = var1.fromPack;
      this.files.clear();
      this.files.putAll(var1.files);
      this.locations.clear();
      this.values.clear();

      for (Entry<String, Integer> var3 : this.samplers.entrySet()) {
         GL20.glUseProgram(this.id);
         GL20.glUniform1i(this.location(var3.getKey()), var3.getValue());
      }
   }

   private static Map<String, GlProgram> makePasses(ShaderPacks.Active var0) {
      LinkedHashMap<String, GlProgram> var1 = new LinkedHashMap<>();

      for (PackPass var3 : var0.passes) {
         GlProgram var4 = new GlProgram("pass " + var3.id, var3.vertex, var3.fragment);
         GlProgram.Linked var5 = var4.link(var0);
         if (var5 == null) {
            for (GlProgram var7 : var1.values()) {
               GL20.glDeleteProgram(var7.id);
            }

            return null;
         }

         var4.adopt(var5);
         var1.put(var3.id, var4);
      }

      return var1;
   }

   private GlProgram.Linked link(ShaderPacks.Active var1) {
      Shaders.Source var2;
      Shaders.Source var3;
      try {
         var2 = Shaders.load(this.vertexPath, var1);
         var3 = Shaders.load(this.fragmentPath, var1);
      } catch (IllegalStateException var9) {
         return failed(this.name + " program: " + var9.getMessage());
      }

      HashMap<String, Long> var4 = new HashMap<>();

      for (String var6 : var2.files) {
         var4.put(var6, Shaders.modified(var6, var1));
      }

      for (String var12 : var3.files) {
         var4.put(var12, Shaders.modified(var12, var1));
      }

      int var11 = this.compile(35633, var2);
      int var13 = this.compile(35632, var3);
      if (var11 != 0 && var13 != 0) {
         int var7 = GL20.glCreateProgram();
         GL20.glAttachShader(var7, var11);
         GL20.glAttachShader(var7, var13);
         GL20.glLinkProgram(var7);
         GL20.glDeleteShader(var11);
         GL20.glDeleteShader(var13);
         if (GL20.glGetProgrami(var7, 35714) == 0) {
            String var8 = GL20.glGetProgramInfoLog(var7);
            GL20.glDeleteProgram(var7);
            return failed(this.name + " program link failed: " + var8);
         } else {
            return new GlProgram.Linked(var7, var2.text + "\n" + var3.text, var4, var2.fromPack || var3.fromPack);
         }
      } else {
         GL20.glDeleteShader(var11);
         GL20.glDeleteShader(var13);
         return null;
      }
   }

   private int compile(int var1, Shaders.Source var2) {
      int var3 = GL20.glCreateShader(var1);
      GL20.glShaderSource(var3, var2.text);
      GL20.glCompileShader(var3);
      if (GL20.glGetShaderi(var3, 35713) == 0) {
         failed(this.name + " shader compile failed (" + var2.files.get(0) + "): " + Shaders.explain(GL20.glGetShaderInfoLog(var3), var2.files));
         GL20.glDeleteShader(var3);
         return 0;
      } else {
         return var3;
      }
   }

   private static GlProgram.Linked failed(String var0) {
      lastError = var0;
      System.out.println("[Viewpoint] " + var0);
      return null;
   }

   private record Linked(int id, String source, Map<String, Long> files, boolean fromPack) {
   }

   public record Sources(String vertex, String control, String evaluation, String geometry, String fragment) {
   }
}