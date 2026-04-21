# Object-Oriented Programming in Java

> **Source:** OOP IN JAVA.pdf – 35 pages

---

## 1. Classes & Objects

### What is a Class?

A class is a **template/blueprint** that defines the structure (fields) and behavior (methods) of objects. It doesn't occupy memory on its own.

```java
class Student {
    String name;    // Field (instance variable)
    int age;        // Field
    
    void display() {   // Method
        System.out.println(name + " - " + age);
    }
}
```

### What is an Object?

An object is an **instance** of a class — a concrete entity created at runtime that occupies memory.

```java
Student s1 = new Student();   // Object creation
s1.name = "Swayam";           // Access via dot operator
s1.age = 20;
s1.display();
```

### Object Creation Breakdown

```
Student s1 = new Student();
  │      │    │      │
  │      │    │      └── Constructor call
  │      │    └── Allocates memory on Heap (JVM's job)
  │      └── Reference variable (stored on Stack)
  └── Compile-time type check (Compiler's job)
```

- **LHS (Compiler):** Type checking, reference declaration
- **RHS (JVM):** Memory allocation, object construction

### Primitives vs Object References

| Feature | Primitive | Object Reference |
|---------|-----------|-----------------|
| Storage | Stack | Heap (ref on stack) |
| Default | 0, false, etc. | `null` |
| Copy | Copies value | Copies reference (same object!) |
| Comparison | `==` compares value | `==` compares reference |
| Memory | Fixed (4/8 bytes) | Variable |

### Copying References

```java
Student a = new Student();
a.name = "Alice";

Student b = a;       // b points to SAME object as a
b.name = "Bob";
System.out.println(a.name);  // "Bob" ← a is also changed!
```

```
Stack:              Heap:
a ─────┐
       ├──→ [Student: name="Bob", age=0]
b ─────┘
```

### Parameters vs Arguments

| Term | Definition | Example |
|------|-----------|---------|
| **Parameter** | Variable in method **declaration** | `void greet(String name)` |
| **Argument** | Value passed in method **call** | `greet("Swayam")` |

---

## 2. The `this` Keyword

`this` is a reference to the **current invoking object** — the object on which the method was called.

### Uses

```java
class Person {
    String name;
    int age;
    
    // 1. Distinguish fields from parameters (same name)
    Person(String name, int age) {
        this.name = name;    // this.name = field, name = parameter
        this.age = age;
    }
    
    // 2. Call another constructor
    Person(String name) {
        this(name, 0);       // Calls Person(String, int)
    }
    
    // 3. Return current object (method chaining)
    Person setName(String name) {
        this.name = name;
        return this;
    }
    
    // 4. Pass current object as argument
    void register() {
        Registry.add(this);
    }
}
```

> ⚠️ `this()` must be the **first statement** in a constructor.

---

## 3. The `final` Keyword

`final` means **"cannot be changed after initialization."** It applies to variables, methods, and classes.

### final Variables

```java
final int MAX = 100;          // Constant — value can't change
// MAX = 200;                 // ❌ Compile error

final Student s = new Student();
// s = new Student();         // ❌ Can't reassign reference
s.name = "Changed";           // ✅ Can modify object's fields!
```

> **Key:** `final` on a reference prevents **reassignment**, not mutation of the object.

### final Methods

```java
class Parent {
    final void display() {     // Cannot be overridden
        System.out.println("Parent");
    }
}

class Child extends Parent {
    // void display() { }     // ❌ Compile error — can't override final method
}
```

### final Classes

```java
final class Utility {         // Cannot be extended
    // ...
}

// class SubUtility extends Utility { }  // ❌ Compile error
```

### Summary

| Applied To | Effect |
|-----------|--------|
| Variable (primitive) | Value cannot change (constant) |
| Variable (reference) | Reference cannot be reassigned |
| Method | Cannot be overridden by subclass |
| Class | Cannot be inherited |

---

## 4. Object Cleanup — `finalize()`

The `finalize()` method is called by the **Garbage Collector** before reclaiming an object's memory.

```java
class Resource {
    @Override
    protected void finalize() throws Throwable {
        System.out.println("Cleaning up resources...");
        super.finalize();
    }
}
```

> ⚠️ `finalize()` is **deprecated** since Java 9. Use **try-with-resources** and `AutoCloseable` instead.

---

## 5. Constructors

A constructor is a **special method** that initializes an object when it's created. It has the **same name as the class** and **no return type** (not even `void`).

### Types

```java
class Car {
    String model;
    int year;
    
    // 1. Default Constructor (provided by JVM if none defined)
    Car() {
        this.model = "Unknown";
        this.year = 2024;
    }
    
    // 2. Parameterized Constructor
    Car(String model, int year) {
        this.model = model;
        this.year = year;
    }
    
    // 3. Copy Constructor (Java doesn't have built-in)
    Car(Car other) {
        this.model = other.model;
        this.year = other.year;
    }
}
```

### Constructor Rules

1. **Same name** as the class.
2. **No return type** — not even `void`.
3. Called **automatically** when object is created with `new`.
4. If you define **any** constructor, JVM **does NOT** provide default.
5. In inheritance, **superclass constructor** is called first (implicitly `super()`).

### Constructor Chaining

```java
class A {
    A() { System.out.println("A"); }
}

class B extends A {
    B() { 
        // super(); ← automatically inserted by compiler
        System.out.println("B"); 
    }
}

new B();  // Output: A, then B (superclass first!)
```

---

## 6. Packages

A package is a **namespace** that organizes related classes and controls access. It maps to **directory structure** on the filesystem.

### Declaration

```java
package com.swayam.dsa;   // Must be first statement in file

public class MyClass {
    // File must be in: com/swayam/dsa/MyClass.java
}
```

### Importing

```java
import java.util.ArrayList;       // Single class
import java.util.*;               // All classes in package (not sub-packages)
import static java.lang.Math.PI; // Static import
```

### Built-in Packages

| Package | Purpose |
|---------|---------|
| `java.lang` | Auto-imported (String, Math, Object, System) |
| `java.util` | Collections, Scanner, Random, Date |
| `java.io` | File I/O, streams |
| `java.net` | Networking |
| `java.sql` | Database connectivity |

### CLASSPATH

The environment variable that tells JVM **where to find** compiled `.class` files.

---

## 7. The `static` Keyword

`static` members belong to the **class**, not to any instance. They're shared across all objects.

### Static Variables

```java
class Counter {
    static int count = 0;    // Shared by ALL objects
    String name;
    
    Counter(String name) {
        this.name = name;
        count++;              // Increments for EVERY object created
    }
}

new Counter("A");   // count = 1
new Counter("B");   // count = 2
Counter.count;      // Access via class name (preferred)
```

### Static Methods

```java
class MathUtils {
    static int add(int a, int b) {
        return a + b;
    }
}

MathUtils.add(3, 5);  // Called without creating an object
```

**Rules for static methods:**
- ✅ Can access static variables and other static methods.
- ❌ Cannot access instance variables or instance methods.
- ❌ Cannot use `this` or `super`.

### Static Blocks

Executed **once** when the class is **loaded** — before any objects are created or static methods are called.

```java
class Config {
    static String dbUrl;
    
    static {
        System.out.println("Static block executed!");
        dbUrl = "jdbc:mysql://localhost/mydb";
    }
}

// Just accessing Config.dbUrl will trigger the static block
```

### Static Inner Classes

```java
class Outer {
    static int x = 10;
    
    static class Inner {
        void display() {
            System.out.println(x);  // Can access outer static members
        }
    }
}

Outer.Inner obj = new Outer.Inner();  // No outer instance needed
```

| Feature | static Inner Class | Non-static Inner Class |
|---------|-------------------|----------------------|
| Needs outer instance | ❌ No | ✅ Yes |
| Access outer instance members | ❌ No | ✅ Yes |
| Access outer static members | ✅ Yes | ✅ Yes |

---

## 8. Inheritance

Inheritance allows a class to **acquire properties and behaviors** of another class — promoting code reuse.

```java
class Animal {             // Superclass / Parent / Base
    String name;
    void eat() { System.out.println(name + " eats"); }
}

class Dog extends Animal { // Subclass / Child / Derived
    void bark() { System.out.println(name + " barks"); }
}

Dog d = new Dog();
d.name = "Rex";
d.eat();     // Inherited from Animal
d.bark();    // Own method
```

### Types of Inheritance

```
Single:          Multilevel:         Hierarchical:
  A                 A                    A
  |                 |                  / | \
  B                 B                B   C   D
                    |
                    C

Multiple (❌ NOT allowed with classes in Java — use interfaces)
```

### The `super` Keyword

```java
class Vehicle {
    int speed = 100;
    
    Vehicle(int speed) {
        this.speed = speed;
    }
    
    void display() { System.out.println("Vehicle: " + speed); }
}

class Car extends Vehicle {
    int speed = 200;
    
    Car() {
        super(150);  // 1. Call superclass constructor — MUST be first line
    }
    
    void display() {
        System.out.println(super.speed);  // 2. Access superclass field (150)
        System.out.println(this.speed);   // Access own field (200)
        super.display();                   // 3. Call superclass method
    }
}
```

### Constructor Execution Order

In an inheritance chain, constructors execute **top-down** (superclass first):

```java
class A { A() { System.out.println("A"); } }
class B extends A { B() { System.out.println("B"); } }
class C extends B { C() { System.out.println("C"); } }

new C();
// Output: A → B → C
```

> Compiler inserts `super()` as the first line of every constructor if not explicitly written.

---

## 9. Polymorphism

Polymorphism means **"many forms"** — the same method name behaves differently depending on the context.

### Compile-Time (Method Overloading)

Same method name, **different parameters** (number, type, or order).

```java
class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}
```

### Runtime (Method Overriding + Dynamic Dispatch)

Subclass provides its own implementation of a superclass method.

```java
class Shape {
    void draw() { System.out.println("Drawing shape"); }
}

class Circle extends Shape {
    @Override
    void draw() { System.out.println("Drawing circle"); }
}

Shape s = new Circle();   // Reference: Shape, Object: Circle
s.draw();                 // "Drawing circle" ← Dynamic dispatch!
```

### Early vs Late Binding

| Aspect | Early Binding | Late Binding |
|--------|--------------|-------------|
| When | Compile time | Runtime |
| How | Compiler resolves | JVM resolves |
| For | `static`, `final`, `private` methods | Overridden methods |
| Also called | Static binding | Dynamic method dispatch |

---

## 10. Access Modifiers

| Modifier | Same Class | Same Package | Subclass (other pkg) | Other Package |
|----------|-----------|-------------|---------------------|--------------|
| `private` | ✅ | ❌ | ❌ | ❌ |
| `default` (no keyword) | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

### Visibility Hierarchy

```
private → default → protected → public
  (most restrictive)         (least restrictive)
```

> **Best Practice:** Use the **most restrictive** modifier that still works. Default to `private` for fields with `public` getters/setters (**encapsulation**).

---

## 11. Abstraction

Abstraction **hides implementation details** and shows only essential features. Achieved through **abstract classes** and **interfaces**.

### Abstract Classes

```java
abstract class Shape {
    String color;
    
    // Abstract method — NO body, MUST be overridden
    abstract double area();
    
    // Concrete method — HAS body, can be inherited as-is
    void display() {
        System.out.println("Color: " + color);
    }
}

class Circle extends Shape {
    double radius;
    
    Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    double area() {                   // MUST implement
        return Math.PI * radius * radius;
    }
}
```

### Rules

1. **Cannot instantiate** an abstract class directly: `new Shape()` → ❌
2. Can have **both abstract and concrete** methods.
3. Can have **constructors** (called via subclass `super()`).
4. Can have **fields** (instance variables).
5. If a class has **even one** abstract method, the class **must** be abstract.
6. Subclass **must implement all** abstract methods, or itself be abstract.

---

## 12. Interfaces

An interface is a **fully abstract contract** — it defines **what** a class must do, but not **how**.

### Declaration

```java
interface Drawable {
    void draw();              // Public & abstract by default
    double getArea();         // Public & abstract by default
    
    int MAX_SIZE = 100;       // Public, static, and final by default
}
```

### Implementation

```java
class Circle implements Drawable {
    double radius;
    
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
    
    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
}
```

### Multiple Interfaces (Java's Multiple Inheritance)

```java
interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();
}

class Duck implements Flyable, Swimmable {
    @Override
    public void fly() { System.out.println("Duck flies"); }
    
    @Override
    public void swim() { System.out.println("Duck swims"); }
}
```

### Interface Inheritance

```java
interface A { void methodA(); }
interface B { void methodB(); }

interface C extends A, B {     // Interfaces CAN extend multiple interfaces
    void methodC();
}
```

### Default & Static Methods (Java 8+)

```java
interface Logger {
    void log(String msg);
    
    // Default method — provides a body, can be overridden
    default void logInfo(String msg) {
        log("INFO: " + msg);
    }
    
    // Static method — called via interface name
    static Logger getDefault() {
        return msg -> System.out.println(msg);
    }
}
```

### Nested Interfaces

```java
class Outer {
    interface Printable {      // Interface inside a class
        void print();
    }
}

// Implement: class MyPrinter implements Outer.Printable { ... }
```

### Abstract Class vs Interface

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Methods | Abstract + concrete | Abstract + default + static |
| Fields | Instance variables | Only `public static final` |
| Constructors | ✅ Yes | ❌ No |
| Multiple inheritance | ❌ No | ✅ Yes |
| Access modifiers | Any | Public only (methods) |
| `extends` / `implements` | `extends` | `implements` |
| When to use | "IS-A" with shared code | "CAN-DO" contract |

---

## 13. Enumerations (Enums)

An `enum` is a special **class type** for defining a fixed set of **named constants**.

### Basic Declaration

```java
enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

Day today = Day.MONDAY;
System.out.println(today);         // "MONDAY"
System.out.println(today.ordinal()); // 0 (position)
```

### Enum Methods

| Method | Description | Example |
|--------|-------------|---------|
| `values()` | Returns array of all constants | `Day.values()` |
| `ordinal()` | Returns position (0-indexed) | `Day.MONDAY.ordinal()` → 0 |
| `valueOf(String)` | Returns enum constant by name | `Day.valueOf("FRIDAY")` |
| `name()` | Returns the name as String | `Day.MONDAY.name()` → "MONDAY" |

### Enum with Fields, Constructors, and Methods

```java
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6);
    
    private final double mass;
    private final double radius;
    
    // Constructor — always private (implicitly)
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }
    
    double surfaceGravity() {
        return 6.67300E-11 * mass / (radius * radius);
    }
}

Planet.EARTH.surfaceGravity();  // ~9.8 m/s²
```

### Enum in Switch

```java
Day today = Day.WEDNESDAY;

switch (today) {
    case MONDAY:
    case TUESDAY:
    case WEDNESDAY:
    case THURSDAY:
    case FRIDAY:
        System.out.println("Weekday");
        break;
    case SATURDAY:
    case SUNDAY:
        System.out.println("Weekend");
        break;
}
```

### Enum Rules

1. Enums **implicitly extend** `java.lang.Enum` — cannot extend other classes.
2. Enum constructors are **always private**.
3. Enum constants are **public, static, final** by default.
4. Enums **can implement interfaces**.
5. Enums are **type-safe** — only predefined values are allowed.

---

## 14. Four Pillars of OOP — Summary

| Pillar | Definition | Java Mechanism |
|--------|-----------|----------------|
| **Encapsulation** | Binding data + methods, controlling access | `private` fields + `public` getters/setters |
| **Inheritance** | Acquiring properties of parent class | `extends`, `super` |
| **Polymorphism** | One interface, multiple implementations | Overloading, overriding, dynamic dispatch |
| **Abstraction** | Hiding complexity, showing essentials | `abstract` classes, `interface` |

---

## 15. Key Takeaways

1. **Objects** live on the Heap; **references** live on the Stack.
2. `new` creates an object; the **constructor** initializes it.
3. `this` refers to the current object; `super` refers to the parent.
4. `final` prevents reassignment (variables), overriding (methods), and inheritance (classes).
5. `static` members belong to the **class**, not instances — shared across all objects.
6. **Static blocks** run once at class load time — before any object creation.
7. Java supports **single inheritance** for classes but **multiple** for interfaces.
8. **Constructors chain top-down** — superclass constructor always runs first.
9. **Dynamic method dispatch** enables runtime polymorphism via overridden methods.
10. Use **abstract classes** for "is-a" with shared code; **interfaces** for "can-do" contracts.
11. **Enums** are type-safe named constants that can have fields, methods, and implement interfaces.
12. Default to `private` fields with getters/setters — this is **encapsulation**.
