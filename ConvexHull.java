import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CyclicBarrier;

class ConvexHull {
    
    int n, k;
    int[] x, y;
    int MAX_X, MAX_Y, MIN_X, MIN_Y; //references to the points
    IntList points;

    CyclicBarrier cb;
    IntList globalConvexHull = new IntList();


    //constructor
    ConvexHull(int n, int seed) {
        this.n = n;
        x = new int[n];
        y = new int[n];
        k = Runtime.getRuntime().availableProcessors();

        NPunkter17 np = new NPunkter17(n, seed);

        //fill the x and y arrays with points 
        np.fyllArrayer(x, y);

        points = np.lagIntList();
    }


    //local worker class
    class Worker implements Runnable {

        int ind, start, end, local_max_x, local_min_x;
        IntList points;
        IntList convexHull = new IntList();
        IntList localPoints = new IntList();

        public Worker(int ind, int start, int end, IntList points) {
            this.ind = ind;
            this.start = start;
            this.end = end;
            this.points = points;
        }

        public void run() {

            //Divide the points into 2 parts
            for (int i = start; i < end; i++) {
                if (x[i] > x[local_max_x]) {
                    local_max_x = i;
                }
                else if (x[i] < x[local_min_x]) {
                    local_min_x = i;
                }

                localPoints.add(points.get(i));
            }
            
            //find points to the left (over)
            convexHull.add(local_max_x);
            findPointsToLeft(local_min_x, local_max_x, localPoints, convexHull);

            //find points to the right (under)
            convexHull.add(local_min_x);
            findPointsToLeft(local_max_x, local_min_x, localPoints, convexHull);
        
            //add the local convex hull to the global, and update the global max/min
            updateGlobalValues(local_max_x, local_min_x, convexHull);

            try {
                cb.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    synchronized void updateGlobalValues(int local_max_x, int local_min_x, IntList local) {
        if (x[local_max_x] > x[MAX_X]) {
            MAX_X = local_max_x;
        }

        if (x[local_min_x] < x[MIN_X]) {
            MIN_X = local_min_x;
        }

        globalConvexHull.append(local);
    }


    //the sequential solution
    IntList quickHull() {

        //Find any two points on the convex hull: min and max x
        for (int i = 0; i < points.size(); i++) {
            if (x[i] > x[MAX_X]) {
                MAX_X = i;
            }
            else if (x[i] < x[MIN_X]) {
                MIN_X = i;
            }

            //for the precode
            if (y[i] > y[MAX_Y]) {
                MAX_Y = i;
            }
        }

        IntList convexHull = new IntList();

        //find points to the left (over)
        convexHull.add(MAX_X);
        findPointsToLeft(MIN_X, MAX_X, points, convexHull);

        //find points to the right (under)
        convexHull.add(MIN_X);
        findPointsToLeft(MAX_X, MIN_X, points, convexHull);
    
        return convexHull;
    }


    //recursive method finding the next point in the line
    void findPointsToLeft(int point1, int point2, IntList points, IntList convexHull) {

        //ax + by + c = 0
        int a = y[point1] - y[point2];
        int b = x[point2] - x[point1];
        int c = (y[point2] * x[point1]) - (y[point1] * x[point2]);

        IntList pointsToLeft = new IntList();
        IntList pointsOnLine = new IntList();
        int maxDistance = 0;
        int maxPoint = -1;

        for (int i = 0; i < points.size(); i++) {
            int p = points.get(i);
            int d = a * x[p] + b * y[p] + c; //the distance from a point to a line

            //positive distance means it's to the left
            if (d > 0) {
                pointsToLeft.add(p);
                
                //check if it's max
                if (d > maxDistance) {
                    maxDistance = d;
                    maxPoint = p;
                }
            }

            //distance 0 means they are on the same line
            if (d == 0) {
                //no need to add point1 and point2 as new points 
                if (p != point1 && p != point2) {
                    pointsOnLine.add(p);
                }
            }

        }


        //recursive call on function 
        if (maxPoint >= 0) {
            //l2 line first
            findPointsToLeft(maxPoint, point2, pointsToLeft, convexHull);
            convexHull.add(maxPoint);
            //then l1
            findPointsToLeft(point1, maxPoint, pointsToLeft, convexHull);
        }

        //no points over the line found, add the points on the line in the right order
        if (maxDistance == 0) {
            
            Integer[] tempList = new Integer[pointsOnLine.size()]; //Integer instead of int to be able to use Comparator

            for (int i = 0; i < pointsOnLine.size(); i++) {
                tempList[i] = pointsOnLine.get(i);
            }

            //sort the point based on which is closest to point2 (d)
            Arrays.sort(tempList, Comparator.comparingInt(i -> ( (int) Math.pow((y[i] - y[point2]), 2) + (int) Math.pow((x[point2] - x[i]), 2))));

            for (int i = 0; i < tempList.length; i++) {
                pointsOnLine.data[i] = tempList[i];
            }

            //append the sorted list
            convexHull.append(pointsOnLine);
        }

    }


    //the parallel solution
    IntList parallelQuickHull() {

        cb = new CyclicBarrier(k+1); 
        IntList convexHull = new IntList();
        int sizeOfSegment = n/k;        

        //divide the points into k parts and make k small convex hulls
        for (int i = 0; i < k-1; i++) {
            new Thread(new Worker(i, i * sizeOfSegment, (i+1) * sizeOfSegment, points)).start();
        }
        new Thread(new Worker(k-1, (k-1) * sizeOfSegment, n, points)).start();

        //wait for the threads to finish
        try {
            cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Now have k small convex hulls in the global IntList. Find the global hull sequentially

        //find points to the left (over)
        convexHull.add(MAX_X);
        findPointsToLeft(MIN_X, MAX_X, globalConvexHull, convexHull);

        //find points to the right (under)
        convexHull.add(MIN_X);
        findPointsToLeft(MAX_X, MIN_X, globalConvexHull, convexHull);
    
        return convexHull;
    }
}
