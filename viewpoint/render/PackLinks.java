package viewpoint.render;

import viewpoint.platform.GlProgram;
import viewpoint.platform.ShaderPacks;

final class PackLinks {
   private static int packTried;

   private PackLinks() {
   }

   static void start() {
      ShaderPacks.Active var0 = GlProgram.pack();
      packTried = var0.generation;
      if (!GlProgram.allLinked() && !var0.pack.builtIn) {
         String var1 = GlProgram.lastError();
         GlProgram.relinkAll(ShaderPacks.library(var0.generation));
         ShaderPacks.linked(GlProgram.pack());
         ShaderPacks.failed(var0, var1);
      } else {
         ShaderPacks.linked(var0);
      }
   }

   static void follow() {
      ShaderPacks.Active var0 = ShaderPacks.active();
      if (var0.generation != packTried) {
         packTried = var0.generation;
         String var1 = GlProgram.relinkAll(var0);
         if (var1 != null) {
            ShaderPacks.failed(var0, var1);
         } else {
            ShaderPacks.linked(var0);
            System.out.println("[Viewpoint] shader pack " + var0.pack.id + " linked");
         }
      }
   }
}
