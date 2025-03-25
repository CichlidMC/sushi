package io.github.cichlidmc.sushi.test.app;

import io.github.cichlidmc.sushi.test.hooks.Hooks;

public final class Doohickey {
   private final int i = 5;

   public void doStuff() {
      Hooks.simpleInjectHeadExplicitTarget();
      Hooks.simpleInjectHead();
      Hooks.implicitMultiTarget();
      System.out.println("doStuff()V");
   }

   private void doStuff(int x) {
      Hooks.simpleInjectHead();
      Hooks.simpleInjectHeadSpecific();
      System.out.println("doStuff(I)V");
   }
}
