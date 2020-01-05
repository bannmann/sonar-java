package org.sonar.java.checks;

class A {

  void foo() {
    int i = 1;
    int j = 4828;
    boolean b = false;

    i = ~getInt(); // Noncompliant [[sc=9;ec=10]] {{Avoid using bitwise operators.}}

    i = 7 & 42; // Noncompliant [[sc=11;ec=12]] {{Avoid using bitwise operators.}}
    if (bar() & quux()) { // Compliant
      i &= getInt(); // Noncompliant
      b &= fizzle(i); // Compliant
    }

    store(getInt() | j); // Noncompliant
    store(b | bar()); // Compliant

    j = i ^ 123; // Noncompliant
    b = b ^ bar(); // Compliant

    j <<= 3; // Noncompliant
    j >>= i; // Noncompliant
    j >>>= 1; // Noncompliant [[sc=7;ec=11]] {{Avoid using bitwise operators.}}

    store(j << 5); // Noncompliant
    store(j >> i); // Noncompliant
    store(j >>> 2); // Noncompliant
  }

  int getInt() { return 32453; }

  boolean bar()  { return false; }
  boolean quux()  { return true; }
  boolean fizzle(int arg)  { return arg % 7 == 0; }

  void store(int arg) {}
  void store(boolean arg) {}
}
