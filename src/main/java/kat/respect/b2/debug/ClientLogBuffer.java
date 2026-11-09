package kat.respect.b2.debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ClientLogBuffer {
   private static final int MAX_LINES = 800;
   private static final int MAX_LEN = 500;
   private static final ArrayDeque<String> LINES = new ArrayDeque<>(816);
   private static boolean installed = false;
   private static File logFile;
   private static PrintWriter fileOut;

   private ClientLogBuffer() {
   }

   public static synchronized void push(String line) {
      try {
         if (line == null) {
            return;
         }

         if (line.length() > 500) {
            line = line.substring(0, 500) + "…";
         }

         LINES.addLast(line);

         while (LINES.size() > 800) {
            LINES.removeFirst();
         }

         if (fileOut != null) {
            fileOut.println(line);
         }
      } catch (Throwable var2) {
      }
   }

   public static synchronized void setFile(File file) {
      try {
         if (file == null) {
            return;
         }

         if (file.equals(logFile) && fileOut != null) {
            return;
         }

         File parent = file.getParentFile();
         if (parent != null) {
            parent.mkdirs();
         }

         if (fileOut != null) {
            try {
               fileOut.close();
            } catch (Throwable var5) {
            }
         }

         logFile = file;
         fileOut = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8), true);
         fileOut.println("=== Kami log session " + new Date() + " ===");

         for (String line : LINES) {
            fileOut.println(line);
         }
      } catch (Throwable var6) {
         Throwable t = var6;

         try {
            System.err.println("[Kami] kami log file failed (" + file + "): " + t);
         } catch (Throwable var4) {
         }
      }
   }

   public static synchronized List<String> snapshot() {
      return new ArrayList<>(LINES);
   }

   public static synchronized void absorb(List<String> lines) {
      if (lines != null) {
         for (String line : lines) {
            push(line);
         }
      }
   }

   public static synchronized List<String> snapshotKamiOnly() {
      List<String> out = new ArrayList<>();

      for (String line : LINES) {
         String l = line.toLowerCase(Locale.ROOT);
         if (l.contains("kami") || l.contains("respect") || l.contains("[ct]") || l.contains("ct]") || l.contains("clickgui") || l.contains("overlay")) {
            out.add(line);
         }
      }

      return out;
   }

   public static synchronized void install() {
      if (!installed) {
         installed = true;

         try {
            PrintStream out = System.out;
            PrintStream err = System.err;
            System.setOut(new PrintStream(new ClientLogBuffer.TeeStream(out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new ClientLogBuffer.TeeStream(err), true, StandardCharsets.UTF_8));
            push("[Kami] log terminal attached (ring buffer, 800 lines)");
         } catch (Throwable var3) {
            Throwable t = var3;

            try {
               System.err.println("[Kami] log tee install failed: " + t);
            } catch (Throwable var2) {
            }
         }
      }
   }

   private static final class TeeStream extends OutputStream {
      private final PrintStream downstream;
      private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

      TeeStream(PrintStream downstream) {
         this.downstream = downstream;
      }

      @Override
      public synchronized void write(int b) {
         try {
            this.downstream.write(b);
         } catch (Throwable var3) {
         }

         if (b == 10) {
            this.flushLine();
         } else if (b != 13) {
            this.buf.write(b);
            if (this.buf.size() > 564) {
               this.flushLine();
            }
         }
      }

      @Override
      public synchronized void write(byte[] b, int off, int len) {
         for (int i = 0; i < len; i++) {
            this.write(b[off + i] & 255);
         }
      }

      private void flushLine() {
         try {
            if (this.buf.size() > 0) {
               ClientLogBuffer.push(this.buf.toString(StandardCharsets.UTF_8));
               this.buf.reset();
            } else {
               ClientLogBuffer.push("");
            }
         } catch (Throwable var2) {
         }
      }

      @Override
      public void flush() {
         try {
            this.downstream.flush();
         } catch (Throwable var2) {
         }
      }
   }
}
