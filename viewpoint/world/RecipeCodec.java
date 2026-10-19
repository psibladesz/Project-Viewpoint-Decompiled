package viewpoint.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.List;
import viewpoint.render.PackModel;
import viewpoint.render.PackModels;
import zombie.core.textures.TextureID;

final class RecipeCodec {
   static final int VERSION = 6;
   static final int OLDEST = 2;
   static final int MODELS_ORDERED = 6;
   private static final int MODELS_VERSION = 3;
   private static final int ROOF_VERSION = 4;
   private static final int UV_FIRST = 3;
   private static final int UV_COUNT = 6;

   static void write(Recipe var0, DataOutputStream var1) throws IOException {
      ArrayList<TileMesh> var2 = new ArrayList<>();
      IdentityHashMap var3 = new IdentityHashMap();

      for (Recipe.Batch var5 : var0.batches) {
         for (Recipe.Op var7 : var5.ops) {
            if (var7.mesh != null && !var3.containsKey(var7.mesh)) {
               var3.put(var7.mesh, var2.size());
               var2.add(var7.mesh);
            }
         }
      }

      var1.writeInt(6);
      var1.writeInt(var0.level);
      var1.writeInt(var2.size());

      for (TileMesh var11 : var2) {
         writeFloats(var1, var11.data);
      }

      var1.writeInt(var0.batches.size());

      for (Recipe.Batch var12 : var0.batches) {
         var1.writeUTF(pageName(var12.page));
         var1.writeInt(var12.ops.size());

         for (Recipe.Op var15 : var12.ops) {
            writeOp(var15, var15.mesh == null ? -1 : (Integer)var3.get(var15.mesh), var1);
         }
      }

      var1.writeInt(var0.models == null ? 0 : var0.models.size());

      for (int var10 = 0; var0.models != null && var10 < var0.models.size(); var10++) {
         Recipe.Op var13 = var0.models.get(var10);
         var1.writeUTF(var13.model.obj.getPath());
         writeOp(var13, -1, var1);
         var1.writeFloat(var13.cos);
         var1.writeFloat(var13.sin);
         var1.writeFloat(var13.scale);
         var1.writeInt(var13.square);
      }

      writeRoof(var0.roof, var1);
   }

   static RecipeCodec.Decoded read(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      if (var1 >= 2 && var1 <= 6) {
         Recipe var2 = new Recipe(var0.readInt());
         TileMesh[] var3 = new TileMesh[var0.readInt()];

         for (int var4 = 0; var4 < var3.length; var4++) {
            var3[var4] = new TileMesh(readFloats(var0));
         }

         int var11 = var0.readInt();
         ArrayList var5 = new ArrayList(var11);

         for (int var6 = 0; var6 < var11; var6++) {
            Recipe.Batch var7 = new Recipe.Batch();
            var5.add(var0.readUTF());
            int var8 = var0.readInt();

            for (int var9 = 0; var9 < var8; var9++) {
               var7.ops.add(readOp(var0, var3));
            }

            var2.batches.add(var7);
         }

         int var12 = var1 >= 3 ? var0.readInt() : 0;

         for (int var13 = 0; var13 < var12; var13++) {
            PackModel var15 = PackModels.of(new File(var0.readUTF()));
            Recipe.Op var17 = readOp(var0, var3);
            var17.model = var15;
            var17.cos = var0.readFloat();
            var17.sin = var0.readFloat();
            var17.scale = var0.readFloat();
            var17.square = var0.readInt();
            if (var2.models == null) {
               var2.models = new ArrayList<>();
            }

            var2.models.add(var17);
         }

         PackModels.publish();
         var2.roof = var1 >= 4 ? readRoof(var0) : null;

         for (Recipe.Batch var16 : var2.batches) {
            for (Recipe.Op var10 : var16.ops) {
               var10.roof = var10.kind != 6 && var10.kind != 7 ? null : var2.roof;
            }
         }

         return new RecipeCodec.Decoded(var2, var5, var1);
      } else {
         throw new IOException("recipe format " + var1 + ", this build reads 2 to 6");
      }
   }

   private static void writeRoof(RoofWindow var0, DataOutputStream var1) throws IOException {
      if (var0 == null) {
         var1.writeInt(-1);
      } else {
         var1.writeInt(var0.width);
         var1.writeInt(var0.height);
         var1.writeInt(var0.inner.x());
         var1.writeInt(var0.inner.z());

         for (int var2 = 0; var2 < var0.pieces.length; var2++) {
            RoofPiece var3 = var0.pieces[var2];
            var1.writeByte(var3 == null ? -1 : var3.kind);
            if (var3 != null) {
               var1.writeByte(var3.facing);
               var1.writeByte(var3.other);
               var1.writeByte(var3.perStorey);
               var1.writeByte(var3.step);
            }

            if (var3 != null && var3.kind == 7) {
               var1.writeFloat(var3.rise);
               var1.writeFloat(var3.middle);
            }

            var1.writeByte(var0.flags[var2]);
            var1.writeByte(var0.lifts[var2]);
         }

         for (int var5 : var0.lenders) {
            var1.writeInt(var5);
         }
      }
   }

   private static RoofWindow readRoof(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      if (var1 < 0) {
         return null;
      }

      int var2 = var0.readInt();
      int var3 = var0.readInt();
      int var4 = var0.readInt();
      RoofPiece[] var5 = new RoofPiece[var1 * var2];
      byte[] var6 = new byte[var1 * var2];
      byte[] var7 = new byte[var1 * var2];

      for (int var8 = 0; var8 < var5.length; var8++) {
         byte var9 = var0.readByte();
         if (var9 == 7) {
            byte var10 = var0.readByte();
            var0.skipBytes(3);
            var5[var8] = RoofPiece.exact(var10, var0.readFloat(), var0.readFloat());
         } else if (var9 >= 0) {
            var5[var8] = new RoofPiece(var9, var0.readByte(), var0.readByte(), var0.readByte(), var0.readByte());
         }

         var6[var8] = var0.readByte();
         var7[var8] = var0.readByte();
      }

      int[] var11 = new int[64];

      for (int var12 = 0; var12 < var11.length; var12++) {
         var11[var12] = var0.readInt();
      }

      return new RoofWindow(var1, var2, RoofWindow.Inner.chunk(var3, var4), var5, var6, var7, var11);
   }

   private static void writeOp(Recipe.Op var0, int var1, DataOutputStream var2) throws IOException {
      var2.writeByte(var0.kind);
      var2.writeByte(var0.face);
      var2.writeInt(var0.flags);
      var2.writeInt(var0.lift);
      var2.writeInt(var1);
      writeFloats(var2, var0.map);
      writeFloats(var2, var0.raw);
      writeFloats(var2, var0.card);
      var2.writeFloat(var0.dx);
      var2.writeFloat(var0.dy);
      var2.writeFloat(var0.height);
      var2.writeFloat(var0.dirX);
      var2.writeFloat(var0.dirZ);
      var2.writeFloat(var0.fill);
   }

   private static Recipe.Op readOp(DataInputStream var0, TileMesh[] var1) throws IOException {
      Recipe.Op var2 = new Recipe.Op();
      var2.kind = var0.readByte();
      var2.face = var0.readByte();
      var2.flags = var0.readInt();
      var2.lift = var0.readInt();
      int var3 = var0.readInt();
      var2.mesh = var3 < 0 ? null : var1[var3];
      var2.map = readFloats(var0);
      var2.raw = readFloats(var0);
      var2.card = readFloats(var0);
      var2.dx = var0.readFloat();
      var2.dy = var0.readFloat();
      var2.height = var0.readFloat();
      var2.dirX = var0.readFloat();
      var2.dirZ = var0.readFloat();
      var2.fill = var0.readFloat();
      return var2;
   }

   private static void writeFloats(DataOutputStream var0, float[] var1) throws IOException {
      if (var1 == null) {
         var0.writeInt(-1);
      } else {
         var0.writeInt(var1.length);

         for (float var5 : var1) {
            var0.writeFloat(var5);
         }
      }
   }

   private static float[] readFloats(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      if (var1 < 0) {
         return null;
      }

      float[] var2 = new float[var1];

      for (int var3 = 0; var3 < var1; var3++) {
         var2[var3] = var0.readFloat();
      }

      return var2;
   }

   static String recipeDigest(Recipe var0, List<String> var1) {
      MessageDigest var2 = sha256();
      update(var2, var0.level);

      for (int var3 = 0; var3 < var0.batches.size(); var3++) {
         Recipe.Batch var4 = var0.batches.get(var3);
         update(var2, (String)var1.get(var3));
         update(var2, var4.ops.size());

         for (Recipe.Op var6 : var4.ops) {
            updateOp(var2, var6);
         }
      }

      for (int var7 = 0; var0.models != null && var7 < var0.models.size(); var7++) {
         Recipe.Op var8 = var0.models.get(var7);
         update(var2, var8.model.obj.getPath());
         updateOp(var2, var8);
         update(var2, new float[]{var8.cos, var8.sin, var8.scale}, false);
         update(var2, var8.square);
      }

      if (var0.roof != null) {
         update(var2, (int)(var0.roof.hash >>> 32));
         update(var2, (int)var0.roof.hash);
      }

      return HexFormat.of().withUpperCase().formatHex(var2.digest());
   }

   static String cookDigest(Cook.Cooked var0, List<String> var1) {
      MessageDigest var2 = sha256();
      if (var0 != null) {
         for (int var3 = 0; var3 < var0.first().length; var3++) {
            update(var2, (String)var1.get(var3));
            update(var2, var0.first()[var3]);
            update(var2, var0.count()[var3]);
            update(var2, var0.plantFirst()[var3]);
            update(var2, var0.plantCount()[var3]);
         }

         update(var2, var0.plantBase());
         FloatBuffer var7 = var0.verts();
         int var4 = var7.position() + var0.modelBase() * 14;

         for (int var5 = var7.position(); var5 < var7.limit(); var5++) {
            boolean var6 = var5 >= var4 && (var5 - var4) % 14 == 8;
            if (var6) {
               update(var2, PackModels.get(Float.floatToRawIntBits(var7.get(var5))).obj.getPath());
            } else {
               update(var2, Float.floatToRawIntBits(var7.get(var5)));
            }
         }
      }

      return HexFormat.of().withUpperCase().formatHex(var2.digest());
   }

   private static void updateOp(MessageDigest var0, Recipe.Op var1) {
      update(var0, var1.kind);
      update(var0, var1.face);
      update(var0, var1.flags);
      update(var0, var1.lift);
      update(var0, var1.mesh == null ? null : var1.mesh.data, false);
      update(var0, var1.map, false);
      update(var0, var1.raw, true);
      update(var0, var1.card, false);
      update(var0, Float.floatToRawIntBits(var1.dx));
      update(var0, Float.floatToRawIntBits(var1.dy));
      update(var0, Float.floatToRawIntBits(var1.height));
      update(var0, Float.floatToRawIntBits(var1.dirX));
      update(var0, Float.floatToRawIntBits(var1.dirZ));
      update(var0, Float.floatToRawIntBits(var1.fill));
   }

   private static void update(MessageDigest var0, float[] var1, boolean var2) {
      if (var1 == null) {
         update(var0, -1);
      } else {
         update(var0, var1.length);

         for (int var3 = 0; var3 < var1.length; var3++) {
            int var4 = var3 % 14;
            boolean var5 = var2 && var4 >= 3 && var4 < 9;
            if (!var5) {
               update(var0, Float.floatToRawIntBits(var1[var3]));
            }
         }
      }
   }

   private static void update(MessageDigest var0, int var1) {
      var0.update((byte)(var1 >>> 24));
      var0.update((byte)(var1 >>> 16));
      var0.update((byte)(var1 >>> 8));
      var0.update((byte)var1);
   }

   private static void update(MessageDigest var0, String var1) {
      byte[] var2 = var1.getBytes(StandardCharsets.UTF_8);
      update(var0, var2.length);
      var0.update(var2);
   }

   static String pageName(TextureID var0) {
      String var1 = var0 == null ? null : var0.getPathFileName();
      return var1 == null ? "(generated)" : var1;
   }

   private static MessageDigest sha256() {
      try {
         return MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var1) {
         throw new IllegalStateException("every Java runtime has SHA-256", var1);
      }
   }

   private RecipeCodec() {
   }

   record Decoded(Recipe recipe, List<String> pages, int version) {
   }
}
