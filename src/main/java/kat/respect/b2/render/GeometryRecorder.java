package kat.respect.b2.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public final class GeometryRecorder implements VertexConsumer {
   private static final byte OP_VERTEX = 0;
   private static final byte OP_COLOR = 1;
   private static final byte OP_NORMAL = 2;
   private static final byte OP_LINE_WIDTH = 3;
   private static final byte OP_UV = 4;
   private static final byte OP_UV1 = 5;
   private static final byte OP_UV2 = 6;
   private byte[] ops = new byte[512];
   private float[] args = new float[2048];
   private int opCount;
   private int argCount;

   public boolean isEmpty() {
      return this.opCount == 0;
   }

   public void reset() {
      this.opCount = 0;
      this.argCount = 0;
   }

   private void push(int op, float a) {
      this.prepare(op, 1);
      this.args[this.argCount++] = a;
   }

   private void push(int op, float a, float b) {
      this.prepare(op, 2);
      this.args[this.argCount++] = a;
      this.args[this.argCount++] = b;
   }

   private void push(int op, float a, float b, float c) {
      this.prepare(op, 3);
      this.args[this.argCount++] = a;
      this.args[this.argCount++] = b;
      this.args[this.argCount++] = c;
   }

   private void push(int op, float a, float b, float c, float d) {
      this.prepare(op, 4);
      this.args[this.argCount++] = a;
      this.args[this.argCount++] = b;
      this.args[this.argCount++] = c;
      this.args[this.argCount++] = d;
   }

   private void prepare(int op, int needed) {
      if (this.opCount == this.ops.length) {
         byte[] grown = new byte[this.ops.length * 2];
         System.arraycopy(this.ops, 0, grown, 0, this.ops.length);
         this.ops = grown;
      }

      this.ops[this.opCount++] = (byte)op;
      if (this.argCount + needed > this.args.length) {
         int size = this.args.length;

         while (this.argCount + needed > size) {
            size *= 2;
         }

         float[] grown = new float[size];
         System.arraycopy(this.args, 0, grown, 0, this.args.length);
         this.args = grown;
      }
   }

   @Override
   public VertexConsumer addVertex(float x, float y, float z) {
      this.push(0, x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setColor(int r, int g, int b, int a) {
      this.push(1, r, g, b, a);
      return this;
   }

   @Override
   public VertexConsumer setColor(int argb) {
      this.push(1, argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24 & 0xFF);
      return this;
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      this.push(2, x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setLineWidth(float width) {
      this.push(3, width);
      return this;
   }

   @Override
   public VertexConsumer setUv(float u, float v) {
      this.push(4, u, v);
      return this;
   }

   @Override
   public VertexConsumer setUv1(int u, int v) {
      this.push(5, u, v);
      return this;
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      this.push(6, u, v);
      return this;
   }

   public void replay(Pose pose, VertexConsumer consumer) {
      int arg = 0;

      for (int i = 0; i < this.opCount; i++) {
         switch (this.ops[i]) {
            case 0:
               consumer.addVertex(pose, this.args[arg], this.args[arg + 1], this.args[arg + 2]);
               arg += 3;
               break;
            case 1:
               consumer.setColor((int)this.args[arg], (int)this.args[arg + 1], (int)this.args[arg + 2], (int)this.args[arg + 3]);
               arg += 4;
               break;
            case 2:
               consumer.setNormal(this.args[arg], this.args[arg + 1], this.args[arg + 2]);
               arg += 3;
               break;
            case 3:
               consumer.setLineWidth(this.args[arg]);
               arg++;
               break;
            case 4:
               consumer.setUv(this.args[arg], this.args[arg + 1]);
               arg += 2;
               break;
            case 5:
               consumer.setUv1((int)this.args[arg], (int)this.args[arg + 1]);
               arg += 2;
               break;
            case 6:
               consumer.setUv2((int)this.args[arg], (int)this.args[arg + 1]);
               arg += 2;
         }
      }
   }
}
