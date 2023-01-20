# Parallel Programming: Recursively Finding the Convex Hull of a set of Points in a Plane
The goal of the assignment is to create a parallel algorithm for creating a convex hull of a set of points. There are multiple ways to do this, but this solution uses the “Divide and Conquer” technique. The set of points is divided into smaller subsets, where each subset gets its own convex hull. When all threads are finished creating their hulls, their points are the basis set for the complete convex hull, found sequentially. The assignment includes both a sequential and parallel implementation, and their runtimes are compared.

## Usage
The program is compiled with:
```bash
javac *.java
```

The program is run with: 
```bash
java Oblig5 <n> <seed> <mode>
```
where <n> and <seed> need to be positive integers and the modes are <seq> or <par>. 

The number of threads on the computer will be used. 

Note:
Both implementations present the convex hull graphically if n <= 1000.
For all the different numbers-n tested <= 100 000, results are stored in text files. They are named CONVEX-HULL-POINTS_<n>.txt
