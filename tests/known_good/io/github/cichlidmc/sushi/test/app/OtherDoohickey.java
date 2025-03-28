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
      Doohickey doohickey = new Doohickey();
      int x = 4;
      double h = Hooks.modifyExpression(Hooks.modifyExpression(doohickey.doStuff(x)));
      return (int)(-100000.0 * h);
   }
}
