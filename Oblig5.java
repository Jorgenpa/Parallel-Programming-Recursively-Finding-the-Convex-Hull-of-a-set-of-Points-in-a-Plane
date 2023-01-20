import java.util.Arrays;

class Oblig5 {

    public static void main(String[] args) {
        
        int n,seed;
        int calls = 7;
        String mode = "";
        
        try {
            n = Integer.parseInt(args[0]);
            seed = Integer.parseInt(args[1]);
            mode = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Correct use of program: java Oblig5 <n> <seed> <mode>");
            return;
        } catch (NumberFormatException e) {
            System.out.println("<n> and <seed> need to be positive integers");
            return;
        }

        switch (mode) {

            case "seq":
                seqConvexHull(n, seed, calls);
                break;

            case "par":
                parConvexHull(n, seed, calls);
                break;

            default:
                System.out.println("The modes are <seq> <par>");
        }
    }


    //sequential implementation
    static void seqConvexHull(int n, int seed, int calls) {

        double[] runtimeSeq = new double[calls];

        for (int i = 0; i < calls; i++) {

            long start, end;
            ConvexHull ch = new ConvexHull(n, seed); //create the array (not to be timed)

            start = System.nanoTime();
            IntList convexHull = ch.quickHull();
            end = System.nanoTime();
            runtimeSeq[i] = (end - start) / 1000000.0;

            if (i == calls - 1) {

                Arrays.sort(runtimeSeq);
                double medianSeq = runtimeSeq[calls/2];
                System.out.println("Sequential Convex Hull for n = " + n);
                System.out.printf("Time: %.2f ms\n", medianSeq);

                //present the convex hull
                if (n <= 100000) {
                    Oblig5Precode op = new Oblig5Precode(ch, convexHull);
                    op.writeHullPoints();
                    if (n <= 1000) op.drawGraph();
                }
            }
        }
    }


    //parallel implementation
    static void parConvexHull(int n, int seed, int calls) {

        double[] runtimePar = new double[calls];

        for (int i = 0; i < calls; i++) {

            long start, end;
            ConvexHull ch = new ConvexHull(n, seed); //create the array (not to be timed)

            start = System.nanoTime();
            IntList convexHull = ch.parallelQuickHull();
            end = System.nanoTime();
            runtimePar[i] = (end - start) / 1000000.0;

            if (i == calls - 1) {

                Arrays.sort(runtimePar);
                double medianPar = runtimePar[calls/2];
                System.out.println("Parallel Convex Hull for n = " + n);
                System.out.printf("Time: %.2f ms\n", medianPar);

                //present the convex hull
                if (n <= 100000) {
                    Oblig5Precode op = new Oblig5Precode(ch, convexHull);
                    op.writeHullPoints();
                    if (n <= 1000) op.drawGraph();
                }
            }
        }
    }
}
