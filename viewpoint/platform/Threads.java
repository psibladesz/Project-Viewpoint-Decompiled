package viewpoint.platform;

public final class Threads {
   private static volatile Thread game;
   private static volatile Thread render;
   private static volatile boolean reported;

   public static void game() {
      if (Settings.dev) {
         game = check(game, "game");
      }
   }

   public static void render() {
      if (Settings.dev) {
         render = check(render, "render");
      }
   }

   private static Thread check(Thread var0, String var1) {
      Thread var2 = Thread.currentThread();
      if (var0 == null) {
         return var2;
      }

      if (var0 != var2 && !reported) {
         reported = true;
         System.out.println("[Viewpoint] " + var1 + "-thread code ran on " + var2.getName() + " (its thread is " + var0.getName() + "):");
         new Throwable().printStackTrace(System.out);
      }

      return var0;
   }

   private Threads() {
   }
}
