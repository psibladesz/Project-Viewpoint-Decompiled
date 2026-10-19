package viewpoint.models;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import viewpoint.platform.Profile;

final class ModelAsks {
   private static final long BUDGET_NANOS = 2000000L;
   private static Set<Object> before = Collections.newSetFromMap(new IdentityHashMap<>());
   private static Set<Object> now = Collections.newSetFromMap(new IdentityHashMap<>());
   private static long spent;
   private static int asked;
   private static int waiting;

   static void begin() {
      Set var0 = before;
      before = now;
      now = var0;
      now.clear();
      spent = 0L;
      asked = 0;
      waiting = 0;
   }

   static boolean due(Object var0) {
      if (!before.contains(var0) && spent >= 2000000L) {
         waiting++;
         return false;
      } else {
         return true;
      }
   }

   static void asked(Object var0, long var1) {
      if (!before.contains(var0)) {
         spent += var1;
         asked++;
      }

      now.add(var0);
   }

   static void end() {
      Profile.newModelsAsked = asked;
      Profile.newModelsWaiting = waiting;
   }

   private ModelAsks() {
   }
}
