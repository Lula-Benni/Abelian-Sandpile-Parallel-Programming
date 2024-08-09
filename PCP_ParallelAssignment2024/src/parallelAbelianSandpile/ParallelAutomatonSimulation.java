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
        int loRow, hiRow, loColumn, hiColumns;
        int [][] grid;
        int [][] updateGrid;

        public AutomatonSimulationThread(int[][] grid, int[][] updateGrid, int loRow, int hiRow, int loColumn, int hiColumns){
            this.grid = grid;
            this.updateGrid = updateGrid;
            this.loRow = loRow;
            this.hiRow = hiRow;
            this.loColumn = loColumn;
            this.hiColumns = hiColumns;
        }
        @Override
        protected Boolean compute() {
            boolean change=false;
            //do not update border
            if(hiRow-loRow<round((hiColumns)*(0.08))) {
                for (int i = loRow; i < hiRow-1; i++) {
                    for (int j = loColumn; j < hiColumns-1; j++) {
                        if (grid[i][j] >= 4) {
                            updateGrid[i][j] = grid[i][j] / 4;
                            grid[i][j] %= 4;
                            /*if(i>loColumn) */grid[i - 1][j] += updateGrid[i][j]; // Left neighbor
                            grid[0][j] = 0;
                            /*if(i<hiColumns-2) */grid[i + 1][j] += updateGrid[i][j]; // Right neighbor
                            grid[hiColumns - 1][j] = 0;
                            /*if(j>loRow) */grid[i][j - 1] += updateGrid[i][j]; // Top neighbor
                            grid[i][0] = 0;
                            /*if(j<hiRow-2) */grid[i][j + 1] += updateGrid[i][j]; // Bottom neighbor
                            grid[i][hiColumns-1] = 0;
                            //(black for 0, green for 1, blue for 2 and red for 3)
                            //if (grid[i][j]!=updateGrid[i][j]) {
                            change=true;
                            //}
                        }
                    }
                }

                /*for (int i = loRow+1; i < hiRow - 1; i++) {
                    for (int j = loColumn+1; j < hiColumns - 1; j++) {
                        updateGrid[i][j] = (grid[i][j] % 4) +
                                (grid[i - 1][j] / 4) +
                                grid[i + 1][j] / 4 +
                                grid[i][j - 1] / 4 +
                                grid[i][j + 1] / 4;
                        if (grid[i][j] != updateGrid[i][j]) {
                            change = true;
                        }
                    }
                } //end nested for
                if (change) {
                    for(int i=loRow+1; i<hiRow-1; i++ ) {
                        for( int j=loColumn+1; j<hiColumns-1; j++ ) {
                            grid[i][j]=updateGrid[i][j];
                        }
                    }
                }
                return change;*/

            }
            return change;
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
        tick(); //start timer
        /*if(DEBUG) {
            System.out.printf("starting config: %d \n",counter);
            simulationGrid.printGrid();
        }*/
        boolean result;
        do {
            /*AutomatonSimulationThread simulationThread = new AutomatonSimulationThread(
                    simulationGrid.getGrid(),
                    simulationGrid.getUpdateGrid(),
                    0,simulationGrid.getRows()+2,
                    0,simulationGrid.getColumns()+2
            );*/
            result = fjPool.invoke(new AutomatonSimulationThread(
                    simulationGrid.getGrid(),
                    simulationGrid.getUpdateGrid(),
                    1,simulationGrid.getRows()+2,
                    1,simulationGrid.getColumns()+2
            ));
            counter++;
        }
        while(result);// {//run until no change
            //if(DEBUG) simulationGrid.printGrid();
        //    counter++;
       // }
        tock();

        System.out.println("Simulation complete, writing image...");
        simulationGrid.gridToImage(outputFileName); //write grid as an image - you must do this.
        //Do NOT CHANGE below!
        //simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
        System.out.printf("Number of steps to stable state: %d \n",counter);
        System.out.printf("Time: %d ms\n",endTime - startTime );
    }
}
