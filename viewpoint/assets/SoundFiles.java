package viewpoint.assets;

import fmod.javafmod;
import fmod.fmod.FMODManager;
import fmod.fmod.FMODSoundBank;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Pool;
import zombie.ZomboidFileSystem;
import zombie.audio.BaseSoundBank;
import zombie.audio.GameSoundClip;
import zombie.core.Core;
import zombie.scripting.ScriptManager;
import zombie.scripting.objects.GameSoundScript;

public final class SoundFiles {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("assets.soundFiles", "Sound files read ahead", "Game/Loading ahead", true);
   private static final long MODE_3D = 16L;
   private static final long PCM_BYTES = 4L;
   private static final int COMPRESSED_RATIO = 10;
   private static final Object LOCK = new Object();
   static final ConcurrentHashMap<String, Long> decoded = new ConcurrentHashMap<>();
   private static final Set<String> claimed = new HashSet<>();
   private static String decoding;
   private static long used;
   private static volatile int listed;
   private static volatile int done;
   private static volatile boolean busy;
   private static volatile int load;

   static void gameLoaded() {
      int var0 = ++load;
      if (ON.get() && !Core.soundDisabled && BaseSoundBank.instance instanceof FMODSoundBank) {
         List var1 = scriptFiles();
         listed = var1.size();
         done = 0;
         busy = true;
         Thread var2 = new Thread(() -> decodeAll(var1, var0), "Viewpoint sound files");
         var2.setDaemon(true);
         var2.setPriority(4);
         var2.start();
      } else {
         done = 0;
         listed = 0;
      }
   }

   public static long take(String var0) {
      String var1 = var0 != null && ON.get() ? ZomboidFileSystem.instance.getAbsolutePath(var0) : null;
      if (var1 == null) {
         return 0L;
      }

      synchronized (LOCK) {
         while (var1.equals(decoding)) {
            try {
               LOCK.wait();
            } catch (InterruptedException var5) {
               Thread.currentThread().interrupt();
               return 0L;
            }
         }

         Long var3 = decoded.get(var1);
         if (var3 != null) {
            return var3;
         }

         claimed.add(var1);
         return 0L;
      }
   }

   public static long decoded(String var0) {
      String var1 = var0 == null ? null : ZomboidFileSystem.instance.getAbsolutePath(var0);
      Long var2 = var1 == null ? null : decoded.get(var1);
      return var2 == null ? 0L : var2;
   }

   static boolean busy() {
      return busy;
   }

   static int listed() {
      return listed;
   }

   static int done() {
      return done;
   }

   private static List<String> scriptFiles() {
      LinkedHashSet var0 = new LinkedHashSet();

      for (GameSoundScript var2 : ScriptManager.instance.getAllGameSounds()) {
         for (GameSoundClip var4 : var2.gameSound.clips) {
            String var5 = var4.getFile();
            String var6 = var5 != null && !var5.isEmpty() ? ZomboidFileSystem.instance.getAbsolutePath(var5) : null;
            if (var6 != null) {
               var0.add(var6);
            }
         }
      }

      return new ArrayList<>(var0);
   }

   private static void decodeAll(List<String> var0, int var1) {
      long var2 = System.nanoTime();
      byte var4 = 0;
      byte var5 = 0;
      byte var6 = 0;

      try {
         ArrayList<SoundFiles.Listed> var7 = new ArrayList<>();

         for (String var9 : var0) {
            var7.add(new SoundFiles.Listed(var9, new File(var9).length()));
         }

         var7.sort(Comparator.comparingLong(SoundFiles.Listed::bytes));

         for (SoundFiles.Listed var15 : var7) {
            if (load != var1 || FMODManager.instance == null) {
               return;
            }

            int var10 = decode(var15.path(), var15.bytes());
            var4 += var10 > 0 ? 1 : 0;
            var5 += var10 < 0 ? 1 : 0;
            var6 += var10 == 0 ? 1 : 0;
            done++;
         }
      } finally {
         if (load == var1) {
            busy = false;
         }
      }

      System.out
         .printf(
            "[Viewpoint] sound files: %d decoded ahead in %.0f ms (%.0f MiB), %d left to the game's first play, %d failed%n",
            Integer.valueOf(var4),
            (System.nanoTime() - var2) / 1000000.0,
            Pool.SOUNDS.used() / 1048576.0,
            Integer.valueOf(var6),
            Integer.valueOf(var5)
         );
   }

   private static int decode(String var0, long var1) {
      long var3 = var1 * (var0.toLowerCase(Locale.ENGLISH).endsWith(".wav") ? 1 : 10);
      synchronized (LOCK) {
         if (claimed.contains(var0) || decoded.containsKey(var0) || used + var3 > Pool.SOUNDS.cap()) {
            return decoded.containsKey(var0) ? 1 : 0;
         }

         decoding = var0;
      }

      long var19 = 0L;

      try {
         var19 = javafmod.FMOD_System_CreateSound(var0, 16L);
      } finally {
         synchronized (LOCK) {
            if (var19 != 0L) {
               decoded.put(var0, var19);
               used = used + Math.max(0L, javafmod.FMOD_Sound_GetLength(var19, 4L));
               Pool.SOUNDS.use(used);
            }

            decoding = null;
            LOCK.notifyAll();
         }
      }

      return var19 != 0L ? 1 : -1;
   }

   private SoundFiles() {
   }

   static {
      ON.describe(
         "Decodes the sound files the game's sound scripts name (mods' sounds) as a save loads, on a thread of its own, so a sound plays the first time without a hitch. Within the cap of the sound files' memory (World, Memory)."
      );
      Pool.SOUNDS
         .describe(
            "The most the sound files decoded ahead hold. They are kept, as the game keeps a sound file it has played; past the cap, a file is decoded as the game decodes it, when it first plays."
         );
   }

   private record Listed(String path, long bytes) {
   }
}
