package viewpoint.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class LauncherCollector {
   static final String FILE = "ProjectZomboid64.json";
   static final String ZGC = "-XX:+UseZGC";
   static final List<String> SHENANDOAH = List.of("-XX:+UseShenandoahGC");
   private static final List<String> NOT_TRIED = List.of("-agentlib:", "-agentpath:", "-javaagent:", "-XX:+AlwaysPreTouch", "-Xms");

   private LauncherCollector() {
   }

   static LauncherCollector.Plan plan(String var0) throws IOException {
      ObjectMapper var1 = new ObjectMapper();
      JsonNode var2 = var1.readTree(var0);
      ArrayList<ArrayNode> var3 = new ArrayList<>();
      lists(var2, var3);
      ArrayList<ArrayNode> var4 = new ArrayList<>();

      for (ArrayNode var6 : var3) {
         if (switchCollector(var6)) {
            var4.add(var6);
         }
      }

      if (var4.isEmpty()) {
         return null;
      }

      List<String> var7 = tried(var2.path("vmArgs"), (ArrayNode)var4.get(0));
      String var8 = var1.writerWithDefaultPrettyPrinter().writeValueAsString(var2);
      return new LauncherCollector.Plan(var8, var7);
   }

   private static void lists(JsonNode var0, List<ArrayNode> var1) {
      var0.fields().forEachRemaining(var1x -> {
         if (((String)var1x.getKey()).equals("vmArgs") && var1x.getValue() instanceof ArrayNode var2) {
            var1.add(var2);
         } else if (((JsonNode)var1x.getValue()).isObject()) {
            lists((JsonNode)var1x.getValue(), var1);
         }
      });
   }

   private static boolean switchCollector(ArrayNode var0) {
      ArrayList<String> var1 = new ArrayList<>();
      boolean var2 = false;

      for (JsonNode var4 : var0) {
         String var5 = var4.asText();
         if (var5.equals("-XX:+UseZGC")) {
            var1.addAll(SHENANDOAH);
            var2 = true;
         } else if (!var5.endsWith("ZGenerational")) {
            var1.add(var5);
         }
      }

      if (var2) {
         var0.removeAll();
         for (String var6 : var1) {
            var0.add(var6);
         }
      }

      return var2;
   }

   private static List<String> tried(JsonNode var0, ArrayNode var1) {
      ArrayList<String> var2 = new ArrayList<>();
      JsonNode[] var3 = var0 == var1 ? new JsonNode[]{var0} : new JsonNode[]{var0, var1};
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         for (JsonNode var8 : var3[var5]) {
            String var9 = var8.asText();
            if (NOT_TRIED.stream().noneMatch(var9::startsWith)) {
               var2.add(var9);
            }
         }
      }

      return var2;
   }

   record Plan(String json, List<String> tried) {
   }
}