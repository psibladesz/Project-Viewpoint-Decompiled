package viewpoint.world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.lwjgl.system.MemoryUtil;
import viewpoint.packs.ModelPacks;
import viewpoint.platform.Keys;
import zombie.ZomboidFileSystem;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;

public final class MeshRecorder {
   static final String FOLDER = "viewpoint-recordings";
   static final String SUFFIX = ".meshrec";
   private static final int RING = 2;
   private static final int MAGIC = 1348095314;
   private static final int FORMAT = 1;

   public static void poll(IsoCell var0, int var1, float var2, float var3, float var4) {
      boolean var5 = Keys.MESH_VERIFY.pressed();
      if (var5 || Keys.MESH_RECORD.pressed()) {
         try {
            if (var5) {
               verify(var0, var1);
            } else {
               record(var0, var1, var2, var3, var4);
            }
         } catch (IOException | RuntimeException var7) {
            System.out.println("[Viewpoint] mesh " + (var5 ? "verify" : "recording") + " failed: " + var7);
         }
      }
   }

   private static void record(IsoCell var0, int var1, float var2, float var3, float var4) throws IOException {
      IsoChunkMap var5 = var0.getChunkMap(var1);
      int var6 = Math.floorDiv((int)Math.floor(var2), 8);
      int var7 = Math.floorDiv((int)Math.floor(var3), 8);
      ArrayList<MeshRecorder.Entry> var8 = new ArrayList<>();
      int var9 = 0;

      for (int var10 = var7 - 2; var10 <= var7 + 2; var10++) {
         for (int var11 = var6 - 2; var11 <= var6 + 2; var11++) {
            IsoChunk var12 = loadedChunk(var5, var11, var10);
            if (var12 != null) {
               for (int var13 = var12.minLevel; var13 <= var12.maxLevel; var13++) {
                  MeshRecorder.Entry var14 = capture(var12, var13, var1);
                  if (var14 == null) {
                     var9++;
                  } else {
                     var8.add(var14);
                  }
               }
            }
         }
      }

      Path var15 = folder().resolve("recording-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".meshrec");
      write(var15, new MeshRecorder.Recording(var2, var3, var4, var8));
      System.out
              .println(
                      "[Viewpoint] mesh recording: "
                              + var8.size()
                              + " levels around "
                              + (int)var2
                              + ","
                              + (int)var3
                              + ","
                              + (int)var4
                              + " into "
                              + var15
                              + (var9 > 0 ? "; " + var9 + " levels still loading were left out" : "")
              );
   }

   private static void verify(IsoCell var0, int var1) throws IOException {
      Path var2 = newest(folder());
      if (var2 == null) {
         System.out.println("[Viewpoint] mesh verify: no recording in " + folder() + " yet (" + Keys.MESH_RECORD.display() + " records one)");
      } else {
         MeshRecorder.Recording var3 = read(var2);
         IsoChunkMap var4 = var0.getChunkMap(var1);
         ArrayList<String> var5 = new ArrayList<>();
         int var6 = 0;
         int var7 = 0;
         int var8 = 0;

         for (MeshRecorder.Entry var10 : var3.entries()) {
            IsoChunk var11 = loadedChunk(var4, var10.chunkX(), var10.chunkY());
            if (var11 != null && var10.level() >= var11.minLevel && var10.level() <= var11.maxLevel) {
               Recipe var12 = WorldMesher.gather(var11, var10.level(), var1, true, packs(), true);
               if (var12.pending) {
                  var8++;
               } else if (RecipeCodec.recipeDigest(var12, pageNames(var12)).equals(var10.recipeDigest())) {
                  var6++;
               } else {
                  var5.add(var10.chunkX() + "," + var10.chunkY() + "," + var10.level());
               }
            } else {
               var7++;
            }
         }

         System.out
                 .println(
                         "[Viewpoint] mesh verify against "
                                 + var2.getFileName()
                                 + ": "
                                 + var6
                                 + " levels match, "
                                 + var5.size()
                                 + " differ"
                                 + (var5.isEmpty() ? "" : " " + var5)
                                 + (var7 > 0 ? ", " + var7 + " not loaded here" : "")
                                 + (var8 > 0 ? ", " + var8 + " still loading (press again)" : "")
                 );
      }
   }

   private static boolean packs() {
      return ModelPacks.ring() >= 2;
   }

   private static MeshRecorder.Entry capture(IsoChunk var0, int var1, int var2) throws IOException {
      Recipe var3 = WorldMesher.gather(var0, var1, var2, true, packs(), true);
      if (var3.pending) {
         return null;
      }

      List<String> var4 = pageNames(var3);
      Cook.Cooked var5 = Cook.vertices(var3);

      String var6;
      try {
         var6 = RecipeCodec.cookDigest(var5, var4);
      } finally {
         if (var5 != null) {
            MemoryUtil.memFree(var5.verts());
         }
      }

      ByteArrayOutputStream var7 = new ByteArrayOutputStream();

      try (DataOutputStream var8 = new DataOutputStream(var7)) {
         RecipeCodec.write(var3, var8);
      }

      return new MeshRecorder.Entry(var0.wx, var0.wy, var1, RecipeCodec.recipeDigest(var3, var4), var6, var7.toByteArray());
   }

   private static IsoChunk loadedChunk(IsoChunkMap var0, int var1, int var2) {
      IsoChunk var3 = var0.getChunkForGridSquare(var1 * 8, var2 * 8);
      boolean var4 = var3 != null && var3.loaded && var3.wx == var1 && var3.wy == var2;
      return var4 ? var3 : null;
   }

   private static List<String> pageNames(Recipe var0) {
      ArrayList<String> var1 = new ArrayList<>(var0.batches.size());

      for (Recipe.Batch var3 : var0.batches) {
         var1.add(RecipeCodec.pageName(var3.page));
      }

      return var1;
   }

   private static Path folder() throws IOException {
      Path var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-recordings").toPath();
      Files.createDirectories(var0);
      return var0;
   }

   static void write(Path var0, MeshRecorder.Recording var1) throws IOException {
      try (DataOutputStream var2 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(var0))))) {
         var2.writeInt(1348095314);
         var2.writeInt(1);
         var2.writeUTF("E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33");
         var2.writeFloat(var1.x());
         var2.writeFloat(var1.y());
         var2.writeFloat(var1.z());
         var2.writeInt(var1.entries().size());

         for (MeshRecorder.Entry var4 : var1.entries()) {
            var2.writeInt(var4.chunkX());
            var2.writeInt(var4.chunkY());
            var2.writeInt(var4.level());
            var2.writeUTF(var4.recipeDigest());
            var2.writeUTF(var4.cookDigest());
            var2.writeInt(var4.recipe().length);
            var2.write(var4.recipe());
         }
      }
   }

   static MeshRecorder.Recording read(Path var0) throws IOException {
      try (DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(var0))))) {
         if (var1.readInt() != 1348095314 || var1.readInt() != 1) {
            throw new IOException(var0 + " is not a mesh recording this build reads");
         }

         var1.readUTF();
         float var2 = var1.readFloat();
         float var3 = var1.readFloat();
         float var4 = var1.readFloat();
         int var5 = var1.readInt();
         ArrayList<MeshRecorder.Entry> var6 = new ArrayList<>(var5);

         for (int var7 = 0; var7 < var5; var7++) {
            int var8 = var1.readInt();
            int var9 = var1.readInt();
            int var10 = var1.readInt();
            String var11 = var1.readUTF();
            String var12 = var1.readUTF();
            byte[] var13 = new byte[var1.readInt()];
            var1.readFully(var13);
            var6.add(new MeshRecorder.Entry(var8, var9, var10, var11, var12, var13));
         }

         return new MeshRecorder.Recording(var2, var3, var4, var6);
      }
   }

   static List<Path> recordings(Path var0) throws IOException {
      if (!Files.isDirectory(var0)) {
         return List.of();
      }

      try (Stream<Path> var1 = Files.list(var0)) {
         return var1.filter(var0x -> var0x.getFileName().toString().endsWith(".meshrec")).sorted(Comparator.comparing(Path::getFileName)).toList();
      }
   }

   private static Path newest(Path var0) throws IOException {
      List<Path> var1 = recordings(var0);
      return var1.isEmpty() ? null : var1.get(var1.size() - 1);
   }

   private MeshRecorder() {
   }

   record Entry(int chunkX, int chunkY, int level, String recipeDigest, String cookDigest, byte[] recipe) {
   }

   record Recording(float x, float y, float z, List<MeshRecorder.Entry> entries) {
   }
}