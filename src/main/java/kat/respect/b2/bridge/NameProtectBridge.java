package kat.respect.b2.bridge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class NameProtectBridge {
   private static volatile NameProtectBridge.Hook hook;

   public static void register(NameProtectBridge.Hook h) {
      hook = h;
   }

   public static void unregister(NameProtectBridge.Hook h) {
      if (hook == h) {
         hook = null;
      }
   }

   public static NameProtectBridge.Hook get() {
      return hook;
   }

   public static FormattedCharSequence transformSequence(FormattedCharSequence sequence) {
      NameProtectBridge.Hook h = hook;
      if (h != null && h.isEnabled() && sequence != null) {
         String target = h.getTarget();
         String replacement = h.getReplacement();
         return target != null && !target.isEmpty() && replacement != null && !replacement.isEmpty()
            ? replaceSequence(sequence, target, replacement)
            : sequence;
      } else {
         return sequence;
      }
   }

   public static String transformString(String text) {
      NameProtectBridge.Hook h = hook;
      if (h != null && h.isEnabled() && text != null && !text.isEmpty()) {
         String target = h.getTarget();
         String replacement = h.getReplacement();
         if (target == null || target.isEmpty() || replacement == null || replacement.isEmpty()) {
            return text;
         } else {
            return indexOfIgnoreCase(text, target, 0) < 0 ? text : replaceIgnoreCase(text, target, replacement);
         }
      } else {
         return text;
      }
   }

   private static FormattedCharSequence replaceSequence(FormattedCharSequence sequence, String target, String replacement) {
      List<Style> styles = new ArrayList<>();
      List<Integer> codepoints = new ArrayList<>();
      StringBuilder plain = new StringBuilder();
      sequence.accept((index, style, codepoint) -> {
         styles.add(style);
         codepoints.add(codepoint);
         plain.appendCodePoint(codepoint);
         return true;
      });
      String text = plain.toString();
      if (indexOfIgnoreCase(text, target, 0) < 0) {
         return sequence;
      } else {
         int[] charToCodepoint = new int[text.length() + 1];
         int codepointIndex = 0;

         for (int charIndex = 0; charIndex < text.length(); codepointIndex++) {
            charToCodepoint[charIndex] = codepointIndex;
            int cp = text.codePointAt(charIndex);
            charIndex += Character.charCount(cp);
         }

         charToCodepoint[text.length()] = codepointIndex;
         List<int[]> matches = new ArrayList<>();
         int at = 0;

         while ((at = indexOfIgnoreCase(text, target, at)) >= 0) {
            matches.add(new int[]{at, target.length()});
            at += target.length();
         }

         List<Integer> outCodepoints = new ArrayList<>(codepoints.size() + 8);
         List<Style> outStyles = new ArrayList<>(styles.size() + 8);
         int i = 0;

         while (i < text.length()) {
            int[] match = matchAt(matches, i);
            if (match != null) {
               Style style = styleAt(styles, charToCodepoint[i]);
               replacement.codePoints().forEach(cpx -> {
                  outCodepoints.add(cpx);
                  outStyles.add(style);
               });
               i += match[1];
            } else {
               int cp = text.codePointAt(i);
               outCodepoints.add(cp);
               outStyles.add(styleAt(styles, charToCodepoint[i]));
               i += Character.charCount(cp);
            }
         }

         return sink -> {
            int index = 0;

            for (int k = 0; k < outCodepoints.size(); k++) {
               if (!sink.accept(index++, outStyles.get(k), outCodepoints.get(k))) {
                  return false;
               }
            }

            return true;
         };
      }
   }

   private static Style styleAt(List<Style> styles, int index) {
      if (styles.isEmpty()) {
         return Style.EMPTY;
      } else {
         int clamped = Math.max(0, Math.min(index, styles.size() - 1));
         return styles.get(clamped);
      }
   }

   private static int[] matchAt(List<int[]> matches, int index) {
      for (int[] match : matches) {
         if (match[0] == index) {
            return match;
         }
      }

      return null;
   }

   private static int indexOfIgnoreCase(String text, String target, int from) {
      int max = text.length() - target.length();

      for (int i = Math.max(0, from); i <= max; i++) {
         if (text.regionMatches(true, i, target, 0, target.length())) {
            return i;
         }
      }

      return -1;
   }

   private static String replaceIgnoreCase(String text, String target, String replacement) {
      StringBuilder sb = new StringBuilder(text.length() + 16);
      int i = 0;

      while (i < text.length()) {
         if (text.regionMatches(true, i, target, 0, target.length())) {
            sb.append(replacement);
            i += target.length();
         } else {
            sb.append(text.charAt(i));
            i++;
         }
      }

      return sb.toString();
   }

   private NameProtectBridge() {
   }

   public interface Hook {
      boolean isEnabled();

      String getTarget();

      String getReplacement();
   }
}
