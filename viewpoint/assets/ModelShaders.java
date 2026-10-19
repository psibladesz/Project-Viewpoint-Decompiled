package viewpoint.assets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import viewpoint.platform.BuildPin;
import viewpoint.platform.LiveSettings;
import zombie.core.Core;
import zombie.core.opengl.RenderThread;
import zombie.core.skinnedmodel.ModelManager;
import zombie.core.skinnedmodel.model.Model;
import zombie.core.skinnedmodel.shader.Shader;
import zombie.core.skinnedmodel.shader.ShaderManager;
import zombie.scripting.ScriptManager;
import zombie.scripting.objects.ModelScript;

public final class ModelShaders {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("assets.modelShaders", "Model shaders without waiting", "Game/Loading ahead", true);
   private static final String BASIC = "basicEffect";
   private static final Set<String> REFLECTING = Set.of("vehicle", "vehicle_multiuv", "vehicle_norandom_multiuv");
   static final ConcurrentHashMap<String, Shader> made = new ConcurrentHashMap<>();
   private static final Set<String> waitedFor = ConcurrentHashMap.newKeySet();

   public static boolean take(Model var0, String var1) {
      if (ON.get() && BuildPin.supported() && !ModelManager.noOpenGL && var1 != null && Thread.currentThread() != RenderThread.renderThread) {
         Shader var2 = made.get(key(var1, var0.isStatic));
         if (var2 == null) {
            if (waitedFor.add(key(var1, var0.isStatic))) {
               System.out
                  .println(
                     "[Viewpoint] model shader "
                        + var1
                        + (var0.isStatic ? " (static)" : "")
                        + " waited for: not made ahead (asked for before a save loaded, or named by no model script)"
                  );
            }

            return false;
         } else {
            var0.effect = var2;
            return true;
         }
      } else {
         return false;
      }
   }

   public static void made(Model var0, String var1) {
      Shader var2 = var0.effect;
      if (var2 != null && var1 != null && var1.equals(var2.getName()) && !var2.isInstanced()) {
         made.putIfAbsent(key(var1, var2.isStatic()), var2);
      }
   }

   static List<ModelShaders.Wanted> named() {
      LinkedHashSet var0 = new LinkedHashSet();
      var0.add(new ModelShaders.Wanted("basicEffect", true));
      var0.add(new ModelShaders.Wanted("basicEffect", false));
      boolean var1 = Core.getInstance().getPerfReflectionsOnLoad() && !Core.getInstance().getUseOpenGL21();

      for (ModelScript var3 : ScriptManager.instance.getAllModelScripts()) {
         var0.add(new ModelShaders.Wanted(gameName(var3.getShaderName(), var1), var3.isStatic));
      }

      return new ArrayList<>(var0);
   }

   static String gameName(String var0, boolean var1) {
      return !var1 && REFLECTING.contains(var0) ? var0 + "_noreflect" : var0;
   }

   static boolean has(ModelShaders.Wanted var0) {
      return made.containsKey(key(var0.name(), var0.isStatic()));
   }

   static void make(ModelShaders.Wanted var0) {
      try {
         Shader var1 = ShaderManager.instance.getOrCreateShader(var0.name(), var0.isStatic(), false);
         made.putIfAbsent(key(var0.name(), var0.isStatic()), var1);
      } catch (RuntimeException var2) {
         System.out.println("[Viewpoint] model shader " + var0.name() + " could not be made ahead: " + var2);
      }
   }

   static String key(String var0, boolean var1) {
      return var1 ? var0 + "|static" : var0;
   }

   private ModelShaders() {
   }

   static {
      ON.describe(
         "A model shown for the first time takes its shader from those already made, instead of waiting for the render thread to make it (up to 20 ms each). The shaders the game's model scripts name are made as a save loads."
      );
   }

   record Wanted(String name, boolean isStatic) {
   }
}
