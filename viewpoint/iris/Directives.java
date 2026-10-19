package viewpoint.iris;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Directives {
   private static final Pattern DRAWBUFFERS = Pattern.compile("DRAWBUFFERS\\s*:\\s*([0-9A-Za-z]+)");
   private static final Pattern RENDERTARGETS = Pattern.compile("RENDERTARGETS\\s*:\\s*([0-9 ,]+)");
   private static final Pattern CONST = Pattern.compile("const\\s+(?:int|float|bool|vec2|vec3|vec4|ivec2|ivec3)\\s+(\\w+)\\s*=\\s*([^;]+?)\\s*;");
   private static final Pattern LOCAL_SIZE = Pattern.compile("local_size_([xyz])\\s*=\\s*(\\d+)");
   public final List<Integer> drawBuffers;
   public final Map<String, String> constants;
   public final int[] localSize = new int[]{1, 1, 1};

   private Directives(List<Integer> var1, Map<String, String> var2) {
      this.drawBuffers = var1;
      this.constants = var2;
   }

   public static Directives scan(String var0) {
      ArrayList var1 = null;
      Matcher var2 = RENDERTARGETS.matcher(var0);

      while (var2.find()) {
         var1 = new ArrayList();

         for (String var6 : var2.group(1).split(",")) {
            if (!var6.isBlank()) {
               var1.add(Integer.parseInt(var6.strip()));
            }
         }
      }

      if (var1 == null) {
         Matcher var8 = DRAWBUFFERS.matcher(var0);

         while (var8.find()) {
            var1 = new ArrayList();

            for (char var7 : var8.group(1).toCharArray()) {
               var1.add(Character.isDigit(var7) ? var7 - '0' : Character.toUpperCase(var7) - 'A' + 10);
            }
         }
      }

      LinkedHashMap var9 = new LinkedHashMap();
      Matcher var11 = CONST.matcher(var0);

      while (var11.find()) {
         if (isDirective(var11.group(1))) {
            var9.put(var11.group(1), var11.group(2).strip());
         }
      }

      Directives var13 = new Directives(var1, var9);
      Matcher var15 = LOCAL_SIZE.matcher(var0);

      while (var15.find()) {
         var13.localSize["xyz".indexOf(var15.group(1))] = Integer.parseInt(var15.group(2));
      }

      return var13;
   }

   static boolean isDirective(String var0) {
      if (!var0.matches("(colortex|gaux|shadowcolor)\\d+(Format|Clear|ClearColor|MipmapEnabled|Nearest)")
         && !var0.matches("(gcolor|gdepth|gnormal|composite|gaux\\d)(Format|Clear|ClearColor|MipmapEnabled)")
         && !var0.matches("shadow(tex\\d|color\\d)?(Nearest|Mipmap|MipmapEnabled)")
         && !var0.matches("shadowHardwareFiltering\\d?")) {
         switch (var0) {
            case "shadowMapResolution":
            case "shadowDistance":
            case "shadowDistanceRenderMul":
            case "shadowIntervalSize":
            case "generateShadowMipmap":
            case "generateShadowColorMipmap":
            case "sunPathRotation":
            case "ambientOcclusionLevel":
            case "noiseTextureResolution":
            case "wetnessHalflife":
            case "drynessHalflife":
            case "eyeBrightnessHalflife":
            case "centerDepthHalflife":
            case "shadowMapFov":
            case "workGroups":
            case "workGroupsRender":
            case "voxelDistance":
               break;
            default:
               return false;
         }
      }

      return true;
   }
}
