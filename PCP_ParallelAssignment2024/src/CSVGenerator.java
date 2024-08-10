import java.io.FileWriter;
import java.io.IOException;

public class CSVGenerator {
    public static void main(String[] args) {
        int rows = 2002;
        int cols = 2002;
        String fileName = "2002_by_2002_all_8.csv";

        try (FileWriter writer = new FileWriter("PCP_ParallelAssignment2024/input/"+fileName)) {
            // Loop through rows
            for (int i = 0; i < rows; i++) {
                // Loop through columns
                for (int j = 0; j < cols; j++) {
                    writer.append("8");
                    if (j < cols - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
            System.out.println("CSV file generated successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the CSV file.");
            e.printStackTrace();
        }
    }
}
