package parallelAbelianSandpile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static java.lang.Math.round;

public class ParallelAutomatonSimulation{

    static final boolean DEBUG=false;//for debugging output, off
    static long startTime = 0;
    static long endTime = 0;

    //timers - note milliseconds
    private static void tick(){ //start timing
        startTime = System.currentTimeMillis();
    }
    private static void tock(){ //end timing
        endTime=System.currentTimeMillis();
    }

    //input is via a CSV file
    public static int [][] readArrayFromCSV(String filePath) {
        int [][] array = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] dimensions = line.split(",");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                System.out.printf("Rows: %d, Columns: %d\n", width, height); //Do NOT CHANGE  - you must ouput this

                array = new int[height][width];
                int rowIndex = 0;

                while ((line = br.readLine()) != null && rowIndex < height) {
                    String[] values = line.split(",");
                    for (int colIndex = 0; colIndex < width; colIndex++) {
                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
                    }
                    rowIndex++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }
    public static class AutomatonSimulationThread extends RecursiveTask<Boolean>{
        private int loRow, hiRow, loColumn, hiColumn;
        private int [][] grid;
        private int [][] updateGrid;
        private static final int SEQUENTIAL_CUTOFF = 5;

        public AutomatonSimulationThread(int[][] grid, int[][] updateGrid, int loRow, int hiRow, int loColumn, int hiColumn){
            this.grid = grid;
            this.updateGrid = updateGrid;
            this.loRow = loRow;
            this.hiRow = hiRow;
            this.loColumn = loColumn;
            this.hiColumn = hiColumn;
        }
        @Override
        protected Boolean compute() {
            boolean change=false;
            //do not update border

            if(hiRow-loRow<SEQUENTIAL_CUTOFF){//*round((hiColumns)*(0.54))*//*) {
                for (int i = loRow; i < hiRow - 1; i++) {
                    for (int j = loColumn; j < hiColumn - 1; j++) {
                        // Calculate the new value for each cell
                        updateGrid[i][j] = (grid[i][j] % 4) +
                                (grid[i - 1][j] / 4) +
                                (grid[i + 1][j] / 4) +
                                (grid[i][j - 1] / 4) +
                                (grid[i][j + 1] / 4);

                        // Check if the value has changed
                        if (grid[i][j] != updateGrid[i][j]) {
                            change = true;
                        }
                    }
                }
                return change;
            }
            else{
                int rowMid = loRow+(hiRow-loRow)/2;
                //int colMid = loColumn+(hiColumn -loColumn)/2;

                AutomatonSimulationThread left = new AutomatonSimulationThread(grid, updateGrid, loRow, rowMid, loColumn, hiColumn);
                AutomatonSimulationThread right = new AutomatonSimulationThread(grid, updateGrid, rowMid, hiRow, loColumn, hiColumn);

                left.fork();

                boolean rightResult = right.compute();
                boolean leftResult = left.join();

                int i = rowMid-1; // The last row of the left region
                for (int j = loColumn; j < hiColumn - 1; j++) {
                    // Calculate the new value for each cell
                    updateGrid[i][j] = (grid[i][j] % 4) +
                            (grid[i - 1][j] / 4) +
                            (grid[i + 1][j] / 4) +
                            (grid[i][j - 1] / 4) +
                            (grid[i][j + 1] / 4);

                    // Check if the value has changed
                    if (grid[i][j] != updateGrid[i][j]) {
                        change = true;
                    }
                }
                return leftResult || rightResult || change;
            }
        }
    }
    public static void main(String[] args) throws IOException {
        final ForkJoinPool fjPool = ForkJoinPool.commonPool();

        if (args.length!=2) {   //input is the name of the input and output files
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }
        /* Read argument values */
        String inputFileName = args[0];  //input file name
        String outputFileName=args[1]; // output file name

        int[][] initialGrid = readArrayFromCSV(inputFileName);
        Grid simulationGrid = new Grid(initialGrid);
        int counter=0;
         //start timer
        if(DEBUG) {
            System.out.printf("starting config: %d \n",counter);
            simulationGrid.printGrid();
        }
        boolean result;
        tick();
        do {
            result = fjPool.invoke(new AutomatonSimulationThread(
                    simulationGrid.getGrid(),
                    simulationGrid.getUpdateGrid(),
                    1,simulationGrid.getRows()+2,
                    1,simulationGrid.getColumns()+2
            ));
            if (result) {
                for (int i = 1; i < simulationGrid.getRows()+1; i++) {
                    for (int j = 1; j < simulationGrid.getColumns()+1; j++) {
                        simulationGrid.getGrid()[i][j] = simulationGrid.getUpdateGrid()[i][j];
                    }
                }
            }
            if(DEBUG) simulationGrid.printGrid();
            counter++;
        }
        while(result);// {//run until no change
        tock();

        System.out.println("Simulation complete, writing image...");
        simulationGrid.gridToImage(outputFileName); //write grid as an image - you must do this.
        //Do NOT CHANGE below!
        //simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
        System.out.printf("Number of steps to stable state: %d \n",counter);
        System.out.printf("Time: %d ms\n",endTime - startTime );
    }
}
