1. What's the difference between /, // and %?
/ performs float division, // calculates floor (integer) division rounded down, and % returns the remainder of the division.

2. Why does range(1, 6) stop at 5?
Python uses half-open intervals [start, stop) where the upper bound is exclusive, making the length simply stop minus start (6 - 1 = 5) and aligning with zero-based indexing.

3. When would you use a tuple instead of a list? A set instead of a list?
Use a tuple for fixed, immutable collections or dictionary keys, and use a set when you need unique elements and constant-time O(1) membership lookups.

4. What does input() always return, and why does that matter?
input() always returns a string, so you must explicitly cast it to types like int or float before performing arithmetic or numerical logic.

5. What is a venv and why does each project get its own?
A venv is an isolated Python runtime environment that prevents package version conflicts and dependency pollution across different projects on the same machine.

6. What does if __name__ == "__main__": do?
It ensures that the enclosed block of code executes only when the script is run directly from the terminal, not when it is imported as a module into another file.

7. Java statically vs dynamically typed — which is Java, and what does the compiler catch as a result?
Java is statically typed, allowing the compiler to catch type mismatches, missing methods, and invalid assignments before runtime.

8. Java overloading vs overriding.
Overloading defines multiple methods in the same class with the same name but different parameter signatures at compile time, whereas overriding redefines a parent class's method in a subclass with the exact same signature at runtime.