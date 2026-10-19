package viewpoint.iris;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Preprocessor {
   private static final Pattern DIRECTIVE = Pattern.compile("^\\s*#\\s*(\\w+)(.*)$", 32);
   private static final Pattern COMMENTED_DEFINE = Pattern.compile("^\\s*//+\\s*#\\s*define\\s+(\\w+)\\s*(//.*)?$");
   private static final Pattern VALUE_DEFINE = Pattern.compile("^(\\s*#\\s*define\\s+(\\w+)\\s+)([^\\s/]+)(.*)$");
   private static final Pattern CONST_OPTION = Pattern.compile("^(\\s*const\\s+\\w+\\s+(\\w+)\\s*=\\s*)([^;]+)(;.*)$");
   private static final Pattern INCLUDE = Pattern.compile("^\\s*[\"<]([^\">]+)[\">]");
   private static final int MAX_DEPTH = 64;
   private final SourceFiles source;
   private final boolean rootIncludes;
   private final Map<String, String> options;
   private final Set<String> toggles;
   private final Macros macros = new Macros();
   private final List<String> files = new ArrayList<>();
   private final Set<String> extensions = new LinkedHashSet<>();
   private final List<String> errors = new ArrayList<>();
   private final StringBuilder out = new StringBuilder(65536);
   private final Deque<String> including = new ArrayDeque<>();
   private String version;
   private boolean properties;

   public Preprocessor(SourceFiles var1, Map<String, String> var2, Map<String, String> var3, Set<String> var4) {
      this(var1, false, var2, var3, var4);
   }

   public Preprocessor(SourceFiles var1, boolean var2, Map<String, String> var3, Map<String, String> var4, Set<String> var5) {
      this.source = var1;
      this.rootIncludes = var2;
      this.options = var4;
      this.toggles = var5;

      for (Entry var7 : var3.entrySet()) {
         this.macros.define((String)var7.getKey(), (String)var7.getValue());
      }
   }

   public Preprocessor.Result run(String var1) throws IOException {
      this.file(PackSource.normalize(var1));
      return new Preprocessor.Result(this.out.toString(), List.copyOf(this.files), this.version, List.copyOf(this.extensions), List.copyOf(this.errors));
   }

   public String runProperties(String var1, String var2) throws IOException {
      this.properties = true;
      this.files.add(var1);
      this.lines(var1, var2, 0);
      return this.out.toString();
   }

   public boolean test(String var1) {
      return IfExpression.test(var1, this.macros);
   }

   public Map<String, String> macros() {
      return this.macros.objectLike();
   }

   private void file(String var1) throws IOException {
      if (this.including.size() <= 64 && !this.including.contains(var1)) {
         String var2 = this.source.read(var1);
         if (var2 == null) {
            throw new IllegalArgumentException("missing file " + var1 + (this.including.isEmpty() ? "" : " (included from " + this.including.peekLast() + ")"));
         }

         this.including.addLast(var1);
         int var3 = this.files.size();
         this.files.add(var1);
         this.out.append("#line 1 ").append(var3).append('\n');
         this.lines(var1, var2, var3);
         this.including.removeLast();
      } else {
         throw new IllegalArgumentException("include cycle: " + String.join(" > ", this.including) + " > " + var1);
      }
   }

   private void lines(String var1, String var2, int var3) throws IOException {
      String[] var4 = var2.split("\n", -1);
      ArrayDeque var5 = new ArrayDeque();
      StringBuilder var6 = new StringBuilder();
      boolean var7 = false;

      for (int var8 = 0; var8 < var4.length; var8++) {
         int var9 = var8;
         String var10 = var4[var8];

         while (var4[var8].endsWith("\\") && var8 + 1 < var4.length) {
            var10 = var10.substring(0, var10.length() - 1) + var4[++var8];
         }

         if (var8 > var9) {
            var10 = var10.replaceAll("\\\\+$", "");
         }

         boolean var11 = var5.isEmpty() || ((boolean[])var5.peek())[0];
         boolean var12 = var7;
         var7 = endsInComment(var10, var7);
         Matcher var13 = var12 ? null : DIRECTIVE.matcher(var10);
         if (var13 != null && var13.matches()) {
            this.flush(var6);
            this.directive(var13.group(1), var13.group(2), var5, var11, var1);
            if (var13.group(1).equals("include") && var11) {
               this.out.append("#line ").append(var8 + 2).append(' ').append(var3).append('\n');
            } else {
               this.appendBlankLines(var8 - var9 + 1);
            }
         } else if (!var11) {
            this.flush(var6);
            this.appendBlankLines(var8 - var9 + 1);
         } else {
            var6.append(this.optionLine(var10)).append('\n');

            for (int var14 = var9; var14 < var8; var14++) {
               var6.append('\n');
            }
         }
      }

      this.flush(var6);
      if (!var5.isEmpty()) {
         this.errors.add(var1 + ": #if without #endif");
      }
   }

   private void flush(StringBuilder var1) {
      if (var1.length() != 0) {
         this.out.append(this.properties ? var1 : this.macros.expand(var1.toString()));
         var1.setLength(0);
      }
   }

   private void appendBlankLines(int var1) {
      for (int var2 = 0; var2 < var1; var2++) {
         this.out.append('\n');
      }
   }

   private void directive(String var1, String var2, Deque<boolean[]> var3, boolean var4, String var5) throws IOException {
      switch (var1) {
         case "if":
         case "ifdef":
         case "ifndef":
            boolean var11 = var4 && this.condition(var1, var2, var5);
            var3.push(new boolean[]{var11, var11, var4});
            break;
         case "elif":
            boolean[] var10 = this.top(var3, var5, var1);
            boolean var9 = var10[2] && !var10[1] && this.condition("if", var2, var5);
            var10[0] = var9;
            var10[1] |= var9;
            break;
         case "else":
            boolean[] var8 = this.top(var3, var5, var1);
            var8[0] = var8[2] && !var8[1];
            var8[1] = true;
            break;
         case "endif":
            if (var3.isEmpty()) {
               this.errors.add(var5 + ": #endif without #if");
            } else {
               var3.pop();
            }
            break;
         default:
            if (var4) {
               this.activeDirective(var1, var2, var5);
            }
      }
   }

   private void activeDirective(String var1, String var2, String var3) throws IOException {
      switch (var1) {
         case "define":
            this.define(var2);
            break;
         case "undef":
            this.macros.undefine(var2.strip().split("\\s+")[0]);
            break;
         case "include":
            this.include(var2, var3);
            break;
         case "version":
            if (this.version == null) {
               this.version = Macros.stripComments(var2).strip();
            }
            break;
         case "extension":
            this.extensions.add("#extension " + Macros.stripComments(var2).strip());
            break;
         case "error":
            this.errors.add(var3 + ": #error " + var2.strip());
            break;
         case "pragma":
            this.out.append("#pragma").append(var2);
      }
   }

   private boolean[] top(Deque<boolean[]> var1, String var2, String var3) {
      if (var1.isEmpty()) {
         this.errors.add(var2 + ": #" + var3 + " without #if");
         var1.push(new boolean[]{false, true, false});
      }

      return (boolean[])var1.peek();
   }

   private boolean condition(String var1, String var2, String var3) {
      String var4 = Macros.stripComments(var2).strip();

      try {
         return switch (var1) {
            case "ifdef" -> this.macros.defined(var4.split("\\s+")[0]);
            case "ifndef" -> !this.macros.defined(var4.split("\\s+")[0]);
            default -> IfExpression.test(var4, this.macros);
         };
      } catch (IllegalArgumentException var7) {
         this.errors.add(var3 + ": " + var7.getMessage());
         return false;
      }
   }

   private void define(String var1) {
      Matcher var2 = VALUE_DEFINE.matcher("#define" + var1);
      String var3 = var1.strip().split("[\\s(]", 2)[0];
      if (!this.toggles.contains(var3) || !"false".equals(this.options.get(var3))) {
         if (var2.matches() && this.options.containsKey(var3) && !this.toggles.contains(var3)) {
            this.macros.define(var3, this.options.get(var3));
         } else {
            this.macros.defineFrom(var1);
         }
      }
   }

   private String optionLine(String var1) {
      if (!var1.contains("define") && !var1.contains("const")) {
         return var1;
      }

      Matcher var2 = COMMENTED_DEFINE.matcher(var1);
      if (var2.matches() && this.toggles.contains(var2.group(1))) {
         if ("true".equals(this.options.get(var2.group(1)))) {
            this.macros.define(var2.group(1), "");
         }

         return "";
      } else {
         Matcher var3 = CONST_OPTION.matcher(var1);
         return var3.matches() && this.options.containsKey(var3.group(2)) && !this.toggles.contains(var3.group(2))
            ? var3.group(1) + this.options.get(var3.group(2)) + var3.group(4)
            : var1;
      }
   }

   private void include(String var1, String var2) throws IOException {
      Matcher var3 = INCLUDE.matcher(var1);
      if (!var3.find()) {
         this.errors.add(var2 + ": cannot read #include" + var1);
      } else {
         String var4 = var3.group(1);
         String var5 = var2.contains("/") ? var2.substring(0, var2.lastIndexOf(47) + 1) : "";
         String var6 = PackSource.normalize(!var4.startsWith("/") && !this.rootIncludes ? var5 + var4 : var4);
         if (var6 == null) {
            throw new IllegalArgumentException(var2 + ": #include " + var4 + " leaves the shaders folder");
         }

         this.file(var6);
      }
   }

   private static boolean endsInComment(String var0, boolean var1) {
      int var2 = 0;
      int var3 = var0.length();

      while (var2 < var3) {
         if (!var1) {
            int var5 = var0.indexOf(47, var2);
            if (var5 < 0 || var5 + 1 >= var3) {
               return false;
            }

            if (var0.charAt(var5 + 1) == '/') {
               return false;
            }

            if (var0.charAt(var5 + 1) == '*') {
               var1 = true;
               var2 = var5 + 2;
            } else {
               var2 = var5 + 1;
            }
         } else {
            int var4 = var0.indexOf("*/", var2);
            if (var4 < 0) {
               return true;
            }

            var1 = false;
            var2 = var4 + 2;
         }
      }

      return var1;
   }

   public record Result(String text, List<String> files, String version, List<String> extensions, List<String> errors) {
   }
}
