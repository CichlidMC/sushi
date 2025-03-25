package io.github.cichlidmc.sushi.test.app;

import io.github.cichlidmc.sushi.test.hooks.Hooks;

public class OtherDoohickey {
   protected final String getString() {
      Hooks.multiTransformHead();
      Hooks.multiTransformTail();
      return "gerald";
   }

   public int doStuff() {
      Hooks.implicitMultiTarget();
      float x = 4.0F;
      return -100000;
   }
}
