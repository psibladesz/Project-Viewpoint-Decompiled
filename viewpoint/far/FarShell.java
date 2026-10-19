package viewpoint.far;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import viewpoint.core.Frame;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.LongMap;
import viewpoint.platform.Pool;
import viewpoint.platform.Profile;
import viewpoint.render.FarGpu;
import viewpoint.render.GroundPage;
import viewpoint.render.GroundTiles;
import viewpoint.render.SceneData;
import viewpoint.render.ShellGround;
import viewpoint.render.TreeAtlas;
import viewpoint.world.Cook;
import viewpoint.world.GridRoofs;
import viewpoint.world.MapRoofs;

public final class FarShell {
   private static final LiveSettings.Number FULL_BLOCKS = LiveSettings.number(
           "lod.shellFullBlocks", "Full shell up to (blocks)", "World/Levels of detail", 0.0F, 12.0F, 1.0F, 3.0F
   );
   private static final LiveSettings.Number LITE_BLOCKS = LiveSettings.number(
           "lod.shellLiteBlocks", "Light shell up to (blocks)", "World/Levels of detail", 0.0F, 24.0F, 1.0F, 10.0F
   );
   private static final LiveSettings.Number JOBS = LiveSettings.number("far.shellJobs", "Shell blocks built at once", "World/Far world", 1.0F, 8.0F, 1.0F, 2.0F);
   static final byte MASK_LITE = 1;
   static final byte MASK_FULL = 2;
   static final byte MASK_LITE_GROUND = 3;
   private static final float TREE_HEIGHT = 4.0F;
   private static final double RELAX = 0.6;
   private static int vertices;
   private static long heldBytes;
   private static int groundsFull;
   private static int groundsLite;
   private static int groundMode = -1;
   private static boolean perBuilding = MapRoofs.perBuilding();
   private static float allowedReach = Float.MAX_VALUE;
   private static float allowedFullReach = Float.MAX_VALUE;
   private static final LongMap<ShellBlock> blocks = new LongMap<>(256);
   private static final LongMap<ShellBlock> leaving = new LongMap<>(64);
   private static final ArrayList<ShellBlock> wanted = new ArrayList<>();
   private static int running;
   private static int generation;
   private static final ConcurrentLinkedQueue<FarShell.Done> DONE = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<FarShell.Facades> FACADES = new ConcurrentLinkedQueue<>();

   static void update(Frame var0, float var1, float var2, long var3, long var5) {
      SceneData var7 = var0.scene;
      long var8 = System.nanoTime();
      takeIn();
      byte var10 = 64;
      int var11 = 256 / var10;
      int var12 = Math.floorDiv((int)Math.floor(var1), var10);
      int var13 = Math.floorDiv((int)Math.floor(var2), var10);
      var7.shellMaskX = var12 - 26;
      var7.shellMaskY = var13 - 26;
      Arrays.fill(var7.shellMask, (byte)0);
      float var14 = var7.nearEnd() - 8.0F;
      float var15 = LITE_BLOCKS.get() * var10;
      float var16 = Math.min(FULL_BLOCKS.get(), LITE_BLOCKS.get()) * var10;
      wanted.clear();
      int var17 = (int)Math.ceil((var15 + 32.0F) / var10);

      for (int var18 = var13 - var17; var18 <= var13 + var17; var18++) {
         for (int var19 = var12 - var17; var19 <= var12 + var17; var19++) {
            visit(var0, var19, var18, var14, var15, var16, var3, var8);
         }
      }

      leave(var7, var0, var3, var8);
      makeRoom(var16);
      startJobs(var11, var5);
   }

   private static void visit(Frame var0, int var1, int var2, float var3, float var4, float var5, long var6, long var8) {
      byte var10 = 64;
      int var11 = 256 / var10;
      float var12 = var0.camX;
      float var13 = var0.camY;
      float var14 = var1 * var10 - var12;
      float var15 = var14 + var10;
      float var16 = var2 * var10 - var13;
      float var17 = var16 + var10;
      float var18 = Math.max(0.0F, Math.max(var14, -var15));
      float var19 = Math.max(0.0F, Math.max(var16, -var17));
      float var20 = Math.max(Math.abs(var14), Math.abs(var15));
      float var21 = Math.max(Math.abs(var16), Math.abs(var17));
      float var22 = (float)Math.sqrt(var18 * var18 + var19 * var19);
      float var23 = (float)Math.sqrt(var20 * var20 + var21 * var21);
      ShellBlock var24 = blocks.get(FarWorld.key(var1, var2));
      ShellBlock var25 = var24 == null ? leaving.get(FarWorld.key(var1, var2)) : null;
      if (var24 != null) {
         settle(var24, var8);
      }

      boolean var26 = var24 != null ? var24.piece != null || var24.coming != null : var25 != null;
      FarWorld.Slot var27 = ShellTiers.shelled(var22, var23, var26, var3, allowedReach, var4)
              ? FarWorld.readCell(Math.floorDiv(var1, var11), Math.floorDiv(var2, var11))
              : null;
      if (var27 != null) {
         if (var24 == null) {
            var24 = var25 != null ? back(var1, var2, var8) : new ShellBlock(var1, var2);
            blocks.put(FarWorld.key(var1, var2), var24);
         }

         var24.seen = var6;
         var24.distance = var22;
         var24.top = var27.maxHeight + 4.0F;
         boolean var28 = Relief.coarseShows(var0.scene, var1, var2, var22);
         var24.want = ShellTiers.tier(var22, var24.builtTier(), allowedFullReach, var5, var28);
         var24.wantExact = var24.want == 0 && ShellTiers.exact(var22, var0.scene.nearEnd(), var24.exact);
         var24.wantInteriors = interiors(var24, var27, var12, var13, var0.scene.nearEnd());
         if (!var24.busy
                 && (
                 !var26
                         || var24.builtTier() != var24.want
                         || var24.generation != generation
                         || var24.wantInteriors != var24.interiors
                         || var24.wantExact != var24.exact
         )) {
            wanted.add(var24);
         }

         if (var26 || var24.previous != null) {
            add(var0.scene, var24, var0, var8);
         }
      }
   }

   private static int[] interiors(ShellBlock var0, FarWorld.Slot var1, float var2, float var3, float var4) {
      if (var0.want != 0) {
         return ShellInteriors.NONE;
      }

      if (var1.buildingBounds == null) {
         var1.buildingBounds = ShellInteriors.bounds(CellRooms.of(var1), var1.buildings);
      }

      byte var5 = 4;
      return ShellInteriors.select(
              var1.buildingBounds, var1.cellX, var1.cellY, var0.bx - var1.cellX * var5, var0.by - var1.cellY * var5, var2, var3, var4, var0.interiors
      );
   }

   private static void makeRoom(float var0) {
      Pool.SHELL.use(poolBytes());
      if (!Pool.SHELL.over(0.9)) {
         if (!Pool.SHELL.over(0.6)) {
            allowedReach = Float.MAX_VALUE;
            allowedFullReach = Float.MAX_VALUE;
         }
      } else {
         ArrayList<ShellBlock> var1 = new ArrayList<>();
         boolean var2 = false;

         for (ShellBlock var4 : blocks.values()) {
            if (var4.piece != null) {
               var1.add(var4);
               var2 |= var4.tier() == 0;
            }
         }

         var1.sort((var0x, var1x) -> Float.compare(var1x.distance, var0x.distance));

         for (int var5 = 0; var5 < var1.size() && Pool.SHELL.over(0.8); var5++) {
            ShellBlock var6 = (ShellBlock)var1.get(var5);
            if (var6.tier() == 0) {
               allowedFullReach = Math.min(allowedFullReach, var6.distance);
               break;
            }

            if (var6.distance >= var0 || !var2) {
               allowedReach = Math.min(allowedReach, var6.distance);
               drop(var6);
               blocks.remove(FarWorld.key(var6.bx, var6.by));
               Pool.SHELL.use(poolBytes());
            }
         }
      }
   }

   private static void takeIn() {
      FarShell.Done var0;
      while ((var0 = DONE.poll()) != null) {
         install(var0);
      }

      Lotpacks.takeIn();

      FarShell.Facades var2;

      while ((var2 = FACADES.poll()) != null) {
         running--;
         var2.slot.facades = var2.facades;
         var2.slot.roofs = var2.roofs;
         var2.slot.facading = false;
      }

      int var3 = ShellMesher.mode();
      boolean var1 = MapRoofs.perBuilding();
      if (TreeSeasons.update() | Erosion.update() | var3 != groundMode | var1 != perBuilding) {
         groundMode = var3;
         perBuilding = var1;
         generation++;
      }

      FarTreeCheck.update();
   }

   private static boolean bakesGround(int var0) {
      return ShellGround.BAKED.get() && (var0 == 0 ? ShellGround.TEXELS_FULL.getInt() > 0 : ShellGround.TEXELS_LITE.getInt() > 0);
   }

   private static long poolBytes() {
      return heldBytes
              + TreeAtlas.bytes()
              + groundsFull * ShellGround.pageBytes(ShellGround.TEXELS_FULL.getInt())
              + groundsLite * ShellGround.pageBytes(ShellGround.TEXELS_LITE.getInt());
   }

   private static void startJobs(int var0, long var1) {
      wanted.sort((var0x, var1x) -> Float.compare(var0x.distance, var1x.distance));

      for (ShellBlock var4 : wanted) {
         if (running >= JOBS.getInt() || System.nanoTime() > var1) {
            break;
         }

         if (blocks.get(FarWorld.key(var4.bx, var4.by)) == var4) {
            FarWorld.Slot var5 = FarWorld.readCell(Math.floorDiv(var4.bx, var0), Math.floorDiv(var4.by, var0));
            if (var5.facades == null) {
               if (!var5.facading) {
                  prepare(var5);
               }
            } else {
               start(var4, var5, var4.want);
            }
         }
      }
   }

   private static void drop(ShellBlock var0) {
      hold(var0.piece, -1);
      hold(var0.previous, -1);
      hold(var0.coming, -1);
      var0.piece = var0.previous = var0.coming = null;
   }

   private static void hold(ShellBlock.Piece var0, int var1) {
      if (var0 != null) {
         vertices = vertices + var1 * var0.vertices();
         heldBytes = heldBytes + var1 * var0.bytes();
         if (var0.ground != null && var0.tier == 0) {
            groundsFull += var1;
         } else if (var0.ground != null) {
            groundsLite += var1;
         }

         Profile.shellVertices = vertices;
         if (var1 < 0) {
            var0.retire();
         }
      }
   }

   private static void leave(SceneData var0, Frame var1, long var2, long var4) {
      Iterator var6 = blocks.values().iterator();

      while (var6.hasNext()) {
         ShellBlock var7 = (ShellBlock)var6.next();
         if (var7.seen != var2) {
            var6.remove();
            if (var7.piece == null) {
               drop(var7);
            } else {
               hold(var7.previous, -1);
               hold(var7.coming, -1);
               var7.previous = var7.piece;
               var7.piece = var7.coming = null;
               var7.fade.begin(var4, 600000000L, 0.0F);
               leaving.put(FarWorld.key(var7.bx, var7.by), var7);
            }
         }
      }

      var6 = leaving.values().iterator();

      while (var6.hasNext()) {
         ShellBlock var9 = (ShellBlock)var6.next();
         if (var9.fade.progress(var4) >= 1.0F) {
            drop(var9);
            var6.remove();
         } else {
            add(var0, var9, var1, var4);
         }
      }
   }

   private static ShellBlock back(int var0, int var1, long var2) {
      ShellBlock var4 = leaving.remove(FarWorld.key(var0, var1));
      if (var4 != null) {
         var4.piece = var4.previous;
         var4.previous = null;
         var4.fade.begin(var2, 600000000L, 1.0F - var4.fade.progress(var2));
      }

      return var4;
   }

   private static void add(SceneData var0, ShellBlock var1, Frame var2, long var3) {
      float var5 = var1.fade.progress(var3);
      if (var5 >= 1.0F && var1.previous != null) {
         hold(var1.previous, -1);
         var1.previous = null;
      }

      var1.emit(var0, var2, var5);
   }

   private static void prepare(FarWorld.Slot var0) {
      short[][] var1 = CellRooms.of(var0);
      String var2 = var0.lotpack;
      byte[] var3 = Lotpacks.get(var2);
      int var4 = var0.minLevel;
      int var5 = var0.maxLevel;
      int var6 = var0.buildings;
      FarTiles.Tile[] var7 = var0.tiles;
      var0.facading = true;
      running++;
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         ShellFacades var8 = ShellFacades.NONE;
         GridRoofs var9 = null;

         try {
            byte[] var10 = Lotpacks.read(var2, var3);
            var8 = ShellFacades.cell(var10, var4, var5, var7, var1, var6);
            var9 = ShellRoofs.cell(var10, var4, var5, var7, var1);
         } catch (Throwable var14) {
            System.out.println("[Viewpoint] far world: the facades or roofs of cell " + var0.cellX + "," + var0.cellY + " failed: " + var14);
         } finally {
            FACADES.add(new FarShell.Facades(var0, var8, var9));
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   private static void start(ShellBlock var0, FarWorld.Slot var1, int var2) {
      short[][] var3 = CellRooms.of(var1);
      String var4 = var1.lotpack;
      byte[] var5 = Lotpacks.get(var4);
      int var6 = var1.minLevel;
      int var7 = var1.maxLevel;
      byte var8 = 4;
      FarTiles.Tile[] var9 = var1.tiles;
      ShellFacades var10 = var1.facades;
      GridRoofs var11 = perBuilding ? var1.roofs : null;
      byte[] var12 = var1.biomes;
      BiomeTrees.Legend var13 = var1.legend;
      BiomeTrees.Table var14 = BiomeTrees.table();
      byte[][] var15 = TreeSeasons.displays();
      if (var1.groundTiles == null && bakesGround(var2)) {
         var1.groundTiles = FarTiles.ground(var9);
      }

      GroundTiles var16 = bakesGround(var2) ? var1.groundTiles : null;
      int var17 = var1.cellX;
      int var18 = var1.cellY;
      int var19 = var0.bx - var17 * var8;
      int var20 = var0.by - var18 * var8;
      int var21 = generation;
      int[] var22 = var0.wantInteriors;
      boolean var23 = var0.wantExact;
      FarTrees.Thinning var24 = FarTrees.Thinning.of(var2, var17, var18);
      byte[] var25 = ShellInteriors.mask(var22, ShellInteriors.FURNITURE.get());
      var0.busy = true;
      running++;
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL
              .execute(
                      () -> {
                         try {
                            byte[] var24x = Lotpacks.read(var4, var5);
                            ShellMesher.Result var25x = ShellMesher.mesh(
                                    var17, var18, var19, var20, var24x, var6, var7, var9, var3, var10, var12, var14, var13, var15, var2, var23, var16, var25, var24, var11
                            );
                            DONE.add(new FarShell.Done(var0, var2, var23, var21, var22, gpu(var25x.shell()), var25x.inside(), var25x.ground(), false));
                         } catch (Throwable var29) {
                            System.out.println("[Viewpoint] far world: the shell of block " + var0.bx + "," + var0.by + " failed: " + var29);
                            DONE.add(new FarShell.Done(var0, var2, var23, var21, var22, null, null, null, true));
                         } finally {
                            FarWorkers.jobs.decrementAndGet();
                         }
                      }
              );
   }

   private static void install(FarShell.Done var0) {
      running--;
      ShellBlock var1 = var0.block;
      if (!var0.failed) {
         var1.busy = false;
         FarGpu.Mesh var2 = new FarGpu.Mesh(1, var0.inside);
         if (blocks.get(FarWorld.key(var1.bx, var1.by)) != var1) {
            FarGpu.retire(var0.shell);
            FarGpu.retire(var2);
         } else {
            hold(var1.coming, -1);
            var1.coming = new ShellBlock.Piece(var0.shell, var2, var0.ground, var0.tier);
            var1.generation = var0.generation;
            var1.interiors = var0.interiors;
            var1.exact = var0.exact;
            hold(var1.coming, 1);
            FarGpu.upload(var0.shell);
            FarGpu.upload(var2);
         }
      }
   }

   private static void settle(ShellBlock var0, long var1) {
      ShellBlock.Piece var3 = var0.coming;
      if (var3 != null && var3.uploaded()) {
         hold(var0.previous, -1);
         var0.previous = var0.piece;
         var0.fade.begin(var1, var0.previous == null ? 600000000L : 400000000L, 0.0F);
         var0.piece = var3;
         var0.coming = null;
      }
   }

   static void clear() {
      for (ShellBlock var1 : blocks.values()) {
         drop(var1);
      }

      for (ShellBlock var3 : leaving.values()) {
         drop(var3);
      }

      blocks.clear();
      leaving.clear();
      Lotpacks.clear();
      vertices = 0;
      heldBytes = 0L;
      groundsLite = 0;
      groundsFull = 0;
      Profile.shellVertices = 0;
      Pool.SHELL.use(0L);
      allowedReach = Float.MAX_VALUE;
      allowedFullReach = Float.MAX_VALUE;
   }

   private static FarGpu.Shell gpu(Cook.Cooked var0) {
      return var0 == null ? null : new FarGpu.Shell(var0.pages(), var0.first(), var0.count(), var0.verts());
   }

   private FarShell() {
   }

   private record Done(
           ShellBlock block, int tier, boolean exact, int generation, int[] interiors, FarGpu.Shell shell, FloatBuffer inside, GroundPage ground, boolean failed
   ) {
   }

   private record Facades(FarWorld.Slot slot, ShellFacades facades, GridRoofs roofs) {
   }
}