package viewpoint.packs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class PackManifest {
   private static final String MODEL = "model.";
   private static final String BIND = "bind.";
   private static final int BIND_FIELDS = 6;
   final String id;
   final String name;
   final LinkedHashMap<String, String> models;
   final LinkedHashMap<String, PackManifest.Bind> binds;
   final List<String> errors;

   private PackManifest(String var1, String var2, LinkedHashMap<String, String> var3, LinkedHashMap<String, PackManifest.Bind> var4, List<String> var5) {
      this.id = var1;
      this.name = var2;
      this.models = var3;
      this.binds = var4;
      this.errors = var5;
   }

   static PackManifest parse(List<String> var0) {
      String var1 = null;
      String var2 = null;
      LinkedHashMap<String, String> var3 = new LinkedHashMap<>();
      LinkedHashMap<String, PackManifest.Bind> var4 = new LinkedHashMap<>();
      ArrayList<String> var5 = new ArrayList<>();

      for (int var6 = 0; var6 < var0.size(); var6++) {
         String var7 = ((String)var0.get(var6)).strip();
         int var8 = var7.indexOf(61);
         if (!var7.isEmpty() && !var7.startsWith("#")) {
            if (var8 <= 0) {
               var5.add("line " + (var6 + 1) + ": no key=value: " + var7);
            } else {
               String var9 = var7.substring(0, var8).strip();
               String var10 = var7.substring(var8 + 1).strip();
               if (var9.equals("id")) {
                  var1 = var10;
               } else if (var9.equals("name")) {
                  var2 = var10;
               } else if (var9.startsWith("model.") && var9.length() > "model.".length() && !var10.isEmpty()) {
                  var3.put(var9.substring("model.".length()), var10);
               } else if (var9.startsWith("bind.") && var9.length() > "bind.".length()) {
                  PackManifest.Bind var11 = bind(var10);
                  if (var11 == null) {
                     var5.add("line " + (var6 + 1) + ": a bind is model,degrees,x,y,z,scale: " + var7);
                  } else {
                     var4.put(var9.substring("bind.".length()), var11);
                  }
               }
            }
         }
      }

      var4.entrySet().removeIf(var2x -> {
         boolean var3x = !var3.containsKey(((PackManifest.Bind)var2x.getValue()).model());
         if (var3x) {
            var5.add("bind." + (String)var2x.getKey() + ": no model " + ((PackManifest.Bind)var2x.getValue()).model());
         }

         return var3x;
      });
      return new PackManifest(var1, var2, var3, var4, var5);
   }

   private static PackManifest.Bind bind(String var0) {
      String[] var1 = var0.split(",");
      if (var1.length == 6 && !var1[0].isBlank()) {
         try {
            float var2 = Float.parseFloat(var1[5].strip());
            return var2 > 0.0F
                    ? new PackManifest.Bind(
                    var1[0].strip(),
                    Float.parseFloat(var1[1].strip()),
                    Float.parseFloat(var1[2].strip()),
                    Float.parseFloat(var1[3].strip()),
                    Float.parseFloat(var1[4].strip()),
                    var2
            )
                    : null;
         } catch (NumberFormatException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   record Bind(String model, float degrees, float x, float y, float z, float scale) {
   }
}