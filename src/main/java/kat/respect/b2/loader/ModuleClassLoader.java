package kat.respect.b2.loader;

import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ModuleClassLoader extends ClassLoader {
   private final Set<String> definedClasses = ConcurrentHashMap.newKeySet();
   private final ProtectionDomain domain = new ProtectionDomain(null, null, this, null);

   public ModuleClassLoader(ClassLoader parent) {
      super(parent);
   }

   public boolean hasClass(String name) {
      return this.definedClasses.contains(name);
   }

   public synchronized Class<?> defineClassDirect(String name, byte[] bytes) {
      if (this.definedClasses.contains(name)) {
         return this.findLoadedClass(name);
      } else {
         Class<?> clazz = this.defineClass(name, bytes, 0, bytes.length, this.domain);
         this.definedClasses.add(name);
         return clazz;
      }
   }

   public void wipeAndClean() {
   }
}
