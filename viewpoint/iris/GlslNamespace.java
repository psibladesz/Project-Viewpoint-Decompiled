package viewpoint.iris;

import java.util.HashSet;
import java.util.List;

public final class GlslNamespace {
   public static final String PREFIX = "vp_";

   private GlslNamespace() {
   }

   public static String prefixed(String source) {
      List<GlslTokens.Token> tokens = GlslTokens.lex(source);
      GlslDeclarations declarations = GlslDeclarations.read(tokens);
      HashSet<String> names = new HashSet<>();

      for (GlslDeclarations.Function function : declarations.functions) {
         if (!function.name.equals("main")) {
            names.add(function.name);
         }
      }

      for (GlslDeclarations.Declaration declaration : declarations.declarations) {
         if (!declaration.has("uniform") && !declarations.insideFunction(declaration.first)) {
            names.addAll(declaration.names);
         }
      }

      // struct names
      for (int i = 0; i + 2 < tokens.size(); i++) {
         if (tokens.get(i).is("struct")) {
            int nameIndex = GlslTokens.next(tokens, i + 1);
            if (nameIndex < tokens.size() && tokens.get(nameIndex).isName()) {
               names.add(tokens.get(nameIndex).text);
            }
         }
      }

      // mark struct bodies, whose field names must stay untouched
      boolean[] insideStruct = new boolean[tokens.size()];
      for (int i = 0; i < tokens.size(); i++) {
         if (tokens.get(i).is("struct")) {
            int brace = GlslTokens.next(tokens, GlslTokens.next(tokens, i + 1) + 1);
            if (brace < tokens.size() && tokens.get(brace).is("{")) {
               int end = GlslTokens.close(tokens, brace);
               for (int j = brace; j <= end && j >= 0; j++) {
                  insideStruct[j] = true;
               }
            }
         }
      }

      for (int i = 0; i < tokens.size(); i++) {
         GlslTokens.Token token = tokens.get(i);
         if (!token.isName()
                 || !names.contains(token.text)
                 || token.text.startsWith(PREFIX)
                 || insideStruct[i]) {
            continue;
         }
         int previous = GlslTokens.previous(tokens, i);
         if (previous >= 0 && tokens.get(previous).is(".")) {
            continue;
         }
         token.text = PREFIX + token.text;
      }

      return GlslTokens.join(tokens);
   }
}