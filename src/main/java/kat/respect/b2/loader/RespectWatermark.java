package kat.respect.b2.loader;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class RespectWatermark {
   private static final byte[] BEGIN = "\n-----BEGIN RESPECT WATERMARK-----".getBytes(StandardCharsets.UTF_8);
   private static final byte[] END = "-----END RESPECT WATERMARK-----\n".getBytes(StandardCharsets.UTF_8);

   private RespectWatermark() {
   }

   static byte[] strip(byte[] jar) {
      if (jar != null && jar.length >= BEGIN.length + END.length) {
         int endIdx = lastIndexOf(jar, END);
         if (endIdx < 0) {
            return jar;
         } else {
            int beginIdx = lastIndexOf(jar, BEGIN, endIdx);
            return beginIdx < 0 ? jar : Arrays.copyOf(jar, beginIdx);
         }
      } else {
         return jar;
      }
   }

   private static int lastIndexOf(byte[] haystack, byte[] needle) {
      return lastIndexOf(haystack, needle, haystack.length);
   }

   private static int lastIndexOf(byte[] haystack, byte[] needle, int limit) {
      int maxStart = Math.min(limit, haystack.length) - needle.length;

      for (int i = maxStart; i >= 0; i--) {
         boolean match = true;

         for (int j = 0; j < needle.length; j++) {
            if (haystack[i + j] != needle[j]) {
               match = false;
               break;
            }
         }

         if (match) {
            return i;
         }
      }

      return -1;
   }
}
