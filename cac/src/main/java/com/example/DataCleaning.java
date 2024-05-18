package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DataCleaning {

    static String[][] data;
    public static List<String[]> newDataset = new ArrayList<>();

    public static boolean areRowsEqual(String[] row1, String[] row2) {
        for (int i = 0; i < row1.length; i++) {
            if (!row1[i].equals(row2[i])) {
                return false;
            }
        }
        return true;
    }

    public static void read() {
        try {
            Scanner myReader = new Scanner(new File("C:\\Users\\yashi\\Desktop\\CAC2\\cac\\src\\main\\resources\\loan_default.csv"));

            int rowCount = 0;
            while (myReader.hasNextLine()) {
                rowCount++;
                myReader.nextLine();
            }
            myReader.close();

            data = new String[rowCount][];

            myReader = new Scanner(new File("C:\\Users\\yashi\\Desktop\\CAC2\\cac\\src\\main\\resources\\loan_default.csv"));
            int rowIndex = 0;
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                data[rowIndex] = line.split(",");
                rowIndex++;
            }

            myReader.close();

            int dup = 0;
            for (int i = 0; i < data.length; i++) {
                for (int j = i + 1; j < data.length; j++) {
                    if (areRowsEqual(data[i], data[j])) {
                        dup += 1;
                        break;
                    }
                }
            }
            System.out.println("Number of duplicate rows: " + dup);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void head() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(data[i][j] + "\t");
            }
        }
    }

    public static void tail() {
        for (int i = data.length - 6; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(data[i][j] + "\t");
            }
        }
    }

    public static void mean() {
        int numRows = data.length;
        int numCols = data[0].length - 1;

        double[] colSums = new double[numCols];
        int[] colCounts = new int[numCols];

        for (int i = 1; i < numRows; i++) {
            for (int j = 1; j < numCols; j++) {
                try {
                    double value = Double.parseDouble(data[i][j]);
                    colSums[j] += value;
                    colCounts[j]++;
                } catch (NumberFormatException e) {
                }
            }
        }

        double[] colMeans = new double[numCols];
        for (int j = 1; j < numCols; j++) {
            if (colCounts[j] > 0) {
                colMeans[j] = colSums[j] / colCounts[j];
            } else {
                colMeans[j] = Double.NaN;
            }
        }

        for (int j = 1; j < numCols; j++) {
            if (!Double.isNaN(colMeans[j])) {
                System.out.println("Mean of column " + (j + 1) + ": " + colMeans[j]);
            }
        }
    }

    public static void unique() {
        for (int j = 0; j < data[0].length; j++) {
            String[] uniqueValues = new String[data.length];
            int count = 0;
            for (int i = 0; i < data.length; i++) {
                String value = data[i][j];
                boolean found = false;
                for (int k = 0; k < count; k++) {
                    if (uniqueValues[k].equals(value)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    uniqueValues[count++] = value;
                }
            }
            System.out.print("Unique values in column " + (j + 1) + ": ");
            for (int k = 0; k < count; k++) {
                System.out.print(uniqueValues[k] + " ");
            }
            System.out.println();
        }
    }

    public static void cleanData() {
        String str = "ID,Batch Enrolled,Payment Plan,Accounts Delinquent";
        data = dropColumns(data, str);

        newDataset.add(data[0]);

        for (int j = 0; j < data[0].length; j++) {
            if (!isCategorical(data, j)) {
                for (int i = 1; i < data.length; i++) {
                    if (newDataset.size() <= i) {
                        newDataset.add(new String[data[0].length]);
                    }
                    newDataset.get(i)[j] = data[i][j];
                }
            } else {
                Map<String, Integer> valueMap = new HashMap<>();
                int numericalValue = 0;
                for (int i = 1; i < data.length; i++) {
                    String value = data[i][j];
                    if (!valueMap.containsKey(value)) {
                        valueMap.put(value, numericalValue++);
                    }
                    data[i][j] = String.valueOf(valueMap.get(value));
                    newDataset.get(i)[j] = data[i][j];
                }
            }
        }

        try {
            File myFile = new File("cleaned_data.csv");
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getName());
            } else {
                System.out.println("File already exists");
            }
            FileWriter myWriter = new FileWriter(myFile);
            for (String[] row : newDataset) {
                StringBuilder csvLine = new StringBuilder();
                for (String value : row) {
                    csvLine.append(value).append(",");
                }
                myWriter.write(csvLine.substring(0, csvLine.length() - 1) + '\n');
            }
            myWriter.close();
            System.out.println("Data written to cleaned_data.csv successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    public static boolean isCategorical(String[][] data, int column) {
        try {
            float A = Float.parseFloat(data[1][column]);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static String[][] dropColumns(String[][] data, String columnsToDrop) {
        String[] columns = columnsToDrop.split(",");
        List<String[]> newData = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            List<String> newRow = new ArrayList<>();
            for (int j = 0; j < data[i].length; j++) {
                if (!Arrays.asList(columns).contains(data[0][j])) {
                    newRow.add(data[i][j]);
                }
            }
            newData.add(newRow.toArray(new String[0]));
        }
        return newData.toArray(new String[0][]);
    }

    public static void main(String[] args) {
        read();
        // head();
        // tail();
        // mean();
        unique();
        cleanData();
    }
}
