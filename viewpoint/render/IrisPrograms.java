package viewpoint.render;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import viewpoint.iris.Directives;
import viewpoint.iris.GeometryPassThrough;
import viewpoint.iris.IrisAssembler;
import viewpoint.iris.IrisPack;
import viewpoint.iris.IrisTransform;
import viewpoint.iris.Preprocessor;
import viewpoint.iris.ProgramSet;
import viewpoint.iris.SmoothNormals;
import viewpoint.iris.Tessellation;
import viewpoint.platform.GlProgram;

final class IrisPrograms {
   static volatile File sources;
   private static final Pattern IVEC3 = Pattern.compile("ivec3\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
   private static final Pattern VEC2 = Pattern.compile("vec2\\s*\\(\\s*([\\d.]+)\\s*,\\s*([\\d.]+)\\s*\\)");
   private static final int MAX_DRAW_BUFFERS = 8;
   final Map<IrisPrograms.Draw, IrisProgram> draws = new EnumMap<>(IrisPrograms.Draw.class);
   final Map<ProgramSet.Stage, List<IrisPrograms.Pass>> passes = new EnumMap<>(ProgramSet.Stage.class);
   final Map<String, String> constants = new LinkedHashMap<>();
   final Set<Integer> buffers = new LinkedHashSet<>();
   final Set<Integer> shadowColors = new LinkedHashSet<>();
   final List<String> notes = new ArrayList<>();
   private final IrisPack pack;
   private final IrisPack.Configured configured;
   private final Map<String, String> standard;
   private final Map<String, String> forced;
   private final IrisLibrary library = new IrisLibrary();

   private IrisPrograms(IrisPack var1, IrisPack.Configured var2, Map<String, String> var3) {
      this.pack = var1;
      this.configured = var2;
      this.standard = var3;
      this.forced = var1.clock ? Map.of("sunPathRotation", "-38.0") : Map.of();
   }

   static IrisPrograms link(IrisPack var0, IrisPack.Configured var1, Map<String, String> var2) {
      IrisPrograms var3 = new IrisPrograms(var0, var1, var2);

      try {
         for (IrisPrograms.Draw var7 : IrisPrograms.Draw.values()) {
            ProgramSet.Program var8 = var1.programs().geometry(var7.program);
            if (var8 != null) {
               var3.draws.put(var7, var3.geometry(var7, var8));
            }
         }

         for (ProgramSet.Stage var16 : ProgramSet.Stage.values()) {
            ArrayList var17 = new ArrayList();

            for (ProgramSet.Program var10 : var1.programs().passes(var16)) {
               var17.add(var3.pass(var10));
            }

            var3.passes.put(var16, var17);
         }

         return var3;
      } catch (IOException | IllegalArgumentException var11) {
         var3.delete();
         throw new IllegalStateException(var0.id + ": " + var11.getMessage(), var11);
      } catch (IllegalStateException var12) {
         var3.delete();
         throw var12;
      }
   }

   private IrisProgram geometry(IrisPrograms.Draw var1, ProgramSet.Program var2) throws IOException {
      Preprocessor.Result var3 = this.pack.preprocess(this.configured, var2.vertex(), this.standard, this.forced);
      Preprocessor.Result var4 = this.pack.preprocess(this.configured, var2.fragment(), this.standard, this.forced);
      errors(var2.name(), var3, var4);
      boolean var5 = var1.source.name().startsWith("FAR_");
      IrisTransform.Stage var6 = IrisTransform.vertex(var3, IrisTransform.Kind.GEOMETRY, var5);
      IrisTransform.Stage var7 = IrisTransform.fragment(var4, IrisTransform.Kind.GEOMETRY, var6.lightmap, var6.colour);
      if (smooth(var1)) {
         var6.code = SmoothNormals.unflatten(var6.code);
         var7.code = SmoothNormals.unflatten(var7.code);
      }

      List var8 = drawBuffers(var4.text(), var7);
      boolean var9 = var1 == IrisPrograms.Draw.SHADOW || var1 == IrisPrograms.Draw.SHADOW_MODELS || var1 == IrisPrograms.Draw.DH_SHADOW;
      (var9 ? this.shadowColors : this.buffers).addAll(var8);
      this.directives(var3.text(), var4.text());
      int var10 = tintOutput(var1, var8, var7);
      String var11 = IrisAssembler.fragment(var7, this.library.fragment(var1.source), var1.source, var10);
      this.notes(var2.name(), var6, var7);
      LinkedHashMap var12 = new LinkedHashMap<>(var6.samplers);
      var12.putAll(var7.samplers);
      var12.put("vp_albedoTex", "sampler2D");
      GlProgram.Sources var13 = this.stages(var1, var2, var6, var11, var12);
      GlProgram var14 = link(this.pack.id + " " + var2.name(), var13, Map.of(), outputs(var7), var4.files());
      IrisProgram var15 = new IrisProgram(var2.name(), var14, var8, var10, var12);
      var14.sampler("uLight", 5);
      var14.sampler("uPalettes", 31);
      var14.sampler("uDraws", 40);
      var14.sampler("uRainMap", 24);
      var14.sampler("uTop", 25);
      var14.sampler("uSide", 26);
      var14.sampler("uMask", 27);
      var14.sampler("uTrees", 43);
      var14.sampler("uKinds", 44);
      var14.sampler("uShellMask", 28);
      var14.sampler("uTexture", 0);
      var14.sampler("uFloorPages", 41);
      var14.sampler("uTreeAtlas", 42);
      var14.sampler("uPlantSlots", 48);
      var14.sampler("uPackTable", 88);
      var14.sampler("uMeshRecords", 89);
      var14.sampler("uShellRecords", 94);
      var14.sampler("uCardTexels", 90);

      for (int var16 = 0; var1.source == IrisAssembler.Source.MODEL && var16 < 16; var16++) {
         var14.sampler("uTextures[" + var16 + "]", 64 + var16);
      }

      if (var1.source == IrisAssembler.Source.VEHICLE) {
         VehiclePass.samplers(var14);
      }

      Spots.sampler(var14);
      LampShadows.sampler(var14);
      var14.sampler("uLampFields", 111);
      var15.checkUnits();
      this.referenced(var12);
      return var15;
   }

   private IrisPrograms.Pass pass(ProgramSet.Program var1) throws IOException {
      IrisProgram var2 = null;
      if (var1.fragment() != null) {
         String var3 = var1.vertex();
         Preprocessor.Result var4 = this.pack.preprocess(this.configured, var1.fragment(), this.standard, this.forced);
         Preprocessor.Result var5 = var3 == null ? null : this.pack.preprocess(this.configured, var3, this.standard, this.forced);
         errors(var1.name(), var5, var4);
         IrisTransform.Stage var6 = IrisTransform.fragment(var4, IrisTransform.Kind.COMPOSITE, null);
         String var7 = IrisAssembler.plain(var6, this.library.composite(var6.lampTints > 0));
         String var8 = null;
         LinkedHashMap var9 = new LinkedHashMap<>(var6.samplers);
         if (var6.lampTints > 0) {
            var9.put("vp_tintTex", "sampler2D");
         }

         if (var5 != null) {
            IrisTransform.Stage var10 = IrisTransform.vertex(var5, IrisTransform.Kind.COMPOSITE);
            var9.putAll(var10.samplers);
            var8 = IrisAssembler.vertex(var10, this.library.vertex(IrisAssembler.Source.QUAD), IrisAssembler.Source.QUAD);
            this.directives(var5.text(), null);
         }

         List var20 = drawBuffers(var4.text(), var6);
         this.buffers.addAll(var20);
         this.directives(var4.text(), null);
         GlProgram var11 = link(
            this.pack.id + " " + var1.name(), new GlProgram.Sources(var8, null, null, null, var7), Map.of("vp_aQuad", 0), outputs(var6), var4.files()
         );
         var2 = new IrisProgram(var1.name(), var11, var20, -1, var9);
         var2.checkUnits();
         this.referenced(var9);
      }

      ArrayList var13 = new ArrayList();
      ArrayList var14 = new ArrayList();
      ArrayList var15 = new ArrayList();

      for (String var17 : var1.computes()) {
         Preprocessor.Result var18 = this.pack.preprocess(this.configured, var17, this.standard, this.forced);
         errors(var17, var18, null);
         IrisTransform.Stage var19 = IrisTransform.plain(var18, IrisTransform.Kind.COMPUTE);
         GlProgram var21 = GlProgram.compute(this.pack.id + " " + var17, IrisAssembler.plain(var19, ""), var18.files());
         IrisProgram var22 = new IrisProgram(var17, var21, List.of(), -1, var19.samplers);
         var22.checkUnits();
         var13.add(var22);
         Directives var12 = Directives.scan(var18.text());
         var14.add(groups(var12));
         var15.add(scale(var12));
         this.referenced(var19.samplers);
      }

      return new IrisPrograms.Pass(var1.name(), var2, var13, var14, var15);
   }

   private static int tintOutput(IrisPrograms.Draw var0, List<Integer> var1, IrisTransform.Stage var2) {
      int var3 = Math.max(var1.size(), var2.outputs.size());

      for (int var5 : var2.outputs.values()) {
         var3 = Math.max(var3, var5 + 1);
      }

      return var0.tint && var3 < 8 ? var3 : -1;
   }

   private static int[] groups(Directives var0) {
      String var1 = var0.constants.get("workGroups");
      if (var1 != null) {
         Matcher var2 = IVEC3.matcher(var1);
         if (var2.find()) {
            return new int[]{Integer.parseInt(var2.group(1)), Integer.parseInt(var2.group(2)), Integer.parseInt(var2.group(3))};
         }
      }

      return var0.constants.containsKey("workGroupsRender") ? null : new int[]{1, 1, 1};
   }

   private static float[] scale(Directives var0) {
      String var1 = var0.constants.get("workGroupsRender");
      float var2 = 1.0F;
      float var3 = 1.0F;
      if (var1 != null) {
         Matcher var4 = VEC2.matcher(var1);
         if (var4.find()) {
            var2 = Float.parseFloat(var4.group(1));
            var3 = Float.parseFloat(var4.group(2));
         }
      }

      return new float[]{var2 / var0.localSize[0], var3 / var0.localSize[1]};
   }

   private static List<Integer> drawBuffers(String var0, IrisTransform.Stage var1) {
      Directives var2 = Directives.scan(var0);
      if (var2.drawBuffers != null) {
         return List.copyOf(var2.drawBuffers);
      }

      int var3 = var1.fragData ? highestFragData(var1.code) + 1 : var1.outputs.size();
      ArrayList var4 = new ArrayList();

      for (int var5 = 0; var5 < Math.max(var3, 1); var5++) {
         var4.add(var5);
      }

      return var4;
   }

   private static int highestFragData(String var0) {
      Matcher var1 = Pattern.compile("gl_FragData\\s*\\[\\s*(\\d+)\\s*\\]").matcher(var0);
      int var2 = 0;

      while (var1.find()) {
         var2 = Math.max(var2, Integer.parseInt(var1.group(1)));
      }

      return var2;
   }

   private static Map<String, Integer> outputs(IrisTransform.Stage var0) {
      LinkedHashMap var1 = new LinkedHashMap();
      int var2 = 0;

      for (Entry var4 : var0.outputs.entrySet()) {
         if ((Integer)var4.getValue() < 0) {
            Matcher var5 = Pattern.compile("outColor(\\d)").matcher((CharSequence)var4.getKey());
            var1.put((String)var4.getKey(), var5.matches() ? Integer.parseInt(var5.group(1)) : var2);
         }

         var2++;
      }

      return var1;
   }

   private void directives(String var1, String var2) {
      for (String var6 : new String[]{var1, var2}) {
         if (var6 != null) {
            Directives.scan(var6).constants.forEach(this.constants::putIfAbsent);
         }
      }
   }

   private void referenced(Map<String, String> var1) {
      for (String var3 : var1.keySet()) {
         int var4 = IrisBindings.colortex(var3);
         if (var4 >= 0) {
            this.buffers.add(var4);
         }

         int var5 = IrisBindings.shadowcolor(var3);
         if (var5 >= 0) {
            this.shadowColors.add(var5);
         }
      }
   }

   private GlProgram.Sources stages(IrisPrograms.Draw var1, ProgramSet.Program var2, IrisTransform.Stage var3, String var4, Map<String, String> var5) throws IOException {
      String var6 = this.library.vertex(var1.source);
      boolean var7 = var1.tessellated();
      String var8 = var7 ? Tessellation.vertex(var6, var1.source) : IrisAssembler.vertex(var3, var6, var1.source);
      String var9 = var7 ? Tessellation.control(var6) : null;
      String var10 = var7 ? Tessellation.evaluation(var3, var6, var1.source) : null;
      if (var2.geometry() == null) {
         return new GlProgram.Sources(var8, var9, var10, null, var4);
      }

      Preprocessor.Result var11 = this.pack.preprocess(this.configured, var2.geometry(), this.standard, this.forced);
      errors(var2.name(), var11, null);
      IrisTransform.Stage var12 = GeometryPassThrough.stage(var11);
      if (smooth(var1)) {
         var12.code = SmoothNormals.unflatten(var12.code);
      }

      var5.putAll(var12.samplers);
      String var13 = GeometryPassThrough.geometry(var12, this.library.geometry(), var7 ? var10 : var8);
      return var7
         ? new GlProgram.Sources(var8, var9, GeometryPassThrough.renamed(var10), var13, var4)
         : new GlProgram.Sources(GeometryPassThrough.renamed(var8), null, null, var13, var4);
   }

   private static boolean smooth(IrisPrograms.Draw var0) {
      return var0.source == IrisAssembler.Source.MODEL || var0.source == IrisAssembler.Source.VEHICLE;
   }

   private static GlProgram link(String var0, GlProgram.Sources var1, Map<String, Integer> var2, Map<String, Integer> var3, List<String> var4) {
      if (sources != null) {
         String[] var5 = new String[]{var1.vertex(), var1.control(), var1.evaluation(), var1.geometry(), var1.fragment()};
         String[] var6 = new String[]{"vs", "tcs", "tes", "gs", "fs"};

         for (int var7 = 0; var7 < var5.length; var7++) {
            if (var5[var7] != null) {
               try {
                  Files.writeString(sources.toPath().resolve(var0.replace(' ', '.') + "." + var6[var7]), var5[var7]);
               } catch (IOException var9) {
                  throw new UncheckedIOException(var9);
               }
            }
         }
      }

      return GlProgram.fromSources(var0, var1, var2, var3, var4);
   }

   private static void errors(String var0, Preprocessor.Result var1, Preprocessor.Result var2) {
      ArrayList var3 = new ArrayList();
      if (var1 != null) {
         var3.addAll(var1.errors());
      }

      if (var2 != null) {
         var3.addAll(var2.errors());
      }

      if (!var3.isEmpty()) {
         throw new IllegalArgumentException(var0 + ": " + String.join("; ", var3));
      }
   }

   private void notes(String var1, IrisTransform.Stage var2, IrisTransform.Stage var3) {
      for (String var5 : var2.notes) {
         this.notes.add(var1 + ": " + var5);
      }

      for (String var7 : var3.notes) {
         this.notes.add(var1 + ": " + var7);
      }
   }

   void delete() {
      for (IrisProgram var2 : this.draws.values()) {
         var2.delete();
      }

      for (List<IrisPrograms.Pass> var6 : this.passes.values()) {
         for (IrisPrograms.Pass var4 : var6) {
            if (var4.program() != null) {
               var4.program().delete();
            }

            var4.computes().forEach(IrisProgram::delete);
         }
      }
   }

   enum Draw {
      TERRAIN("gbuffers_terrain", IrisAssembler.Source.MESH, true),
      WATER("gbuffers_water", IrisAssembler.Source.MESH, false),
      SKY("gbuffers_skybasic", IrisAssembler.Source.PLAIN, false),
      SKY_TEXTURED("gbuffers_skytextured", IrisAssembler.Source.PLAIN, false),
      ENTITIES("gbuffers_entities", IrisAssembler.Source.MODEL, true),
      VEHICLES("gbuffers_entities", IrisAssembler.Source.VEHICLE, true),
      BLOCKS("gbuffers_block", IrisAssembler.Source.MODEL, true),
      VEHICLE_GLASS("gbuffers_entities_translucent", IrisAssembler.Source.VEHICLE, false),
      BLOCK_GLASS("gbuffers_entities_translucent", IrisAssembler.Source.MODEL, false),
      WEATHER("gbuffers_weather", IrisAssembler.Source.WEATHER, false),
      LIGHTNING("gbuffers_lightning", IrisAssembler.Source.PLAIN, false),
      DH_TERRAIN("dh_terrain", IrisAssembler.Source.FAR_BOX, false),
      DH_TREES("dh_terrain", IrisAssembler.Source.FAR_TREE, false),
      DH_SHELL("dh_terrain", IrisAssembler.Source.FAR_SHELL, false),
      DH_SHADOW("dh_shadow", IrisAssembler.Source.FAR_BOX, false),
      SHADOW("shadow", IrisAssembler.Source.MESH, false),
      SHADOW_MODELS("shadow", IrisAssembler.Source.MODEL, false);

      final String program;
      final IrisAssembler.Source source;
      final boolean tint;

      boolean tessellated() {
         return this == SHADOW || this == DH_SHADOW || this == TERRAIN || this == WATER;
      }

      Draw(String nullxx, IrisAssembler.Source nullxxx, boolean nullxxxx) {
         this.program = nullxx;
         this.source = nullxxx;
         this.tint = nullxxxx;
      }
   }

   record Pass(String name, IrisProgram program, List<IrisProgram> computes, List<int[]> groups, List<float[]> scales) {
   }
}
