package com.example;
import weka.classifiers.lazy.IBk;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.Filter;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.DenseInstance;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class KNNClassifier {
    public static void main(String[] args) throws Exception {
        // Load CSV file
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("C:\\Users\\yashi\\Desktop\\CAC2\\cleaned_data.csv"));
        Instances data = loader.getDataSet();
        data.setClassIndex(data.numAttributes() - 1); // Assuming the class attribute is the last one

        // Convert numeric class attribute to nominal
        NumericToNominal converter = new NumericToNominal();
        converter.setAttributeIndices("last");
        converter.setInputFormat(data);
        data = Filter.useFilter(data, converter);

        // Initialize and build the classifier (K-Nearest Neighbors)
        IBk knn = new IBk();
        knn.setKNN(5);

        // Assuming 70% of data for training, 30% for testing
        int trainSize = (int) Math.round(data.size() * 0.70);
        int testSize = data.size() - trainSize;
        Instances trainData = new Instances(data, 0, trainSize);
        Instances testData = new Instances(data, trainSize, testSize);

        knn.buildClassifier(trainData);

        // Visualize
        visualize(trainData, knn);

        // Evaluate the model
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(knn, testData);

        // Print the evaluation results
        System.out.println(eval.toSummaryString());

        // Print the class predictions
        System.out.println("Predictions:");
        for (int i = 0; i < testData.numInstances(); i++) {
            Instance instance = testData.instance(i);
            double actual = instance.classValue();
            String actualClass = testData.classAttribute().value((int) actual);
            double[] distribution = knn.distributionForInstance(instance);
            String predictedClass = (distribution[0] >= 0.5) ? "No loan default" : "loan default";
            System.out.println("Instance " + (i + 1) + ": Actual=" + actualClass + ", Predicted=" + predictedClass + " "
                    + distribution[0]);
        }

        // Print accuracy
        double accuracy = eval.pctCorrect();
        System.out.println("Accuracy: " + accuracy + "%");

        // Print confusion matrix
        double[][] confusionMatrix = eval.confusionMatrix();
        System.out.println("\nConfusion Matrix:");
        for (int row = 0; row < confusionMatrix.length; row++) {
            for (int col = 0; col < confusionMatrix[row].length; col++) {
                System.out.print(confusionMatrix[row][col] + "\t");
            }
            System.out.println();
        }

    }

    private static void visualize(Instances data, IBk knn) throws Exception {
        // Create series for each class
        XYSeries series1 = new XYSeries("Class 1");
        XYSeries series2 = new XYSeries("Class 2");
    
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            double[] values = new double[2]; // Assuming only 2 attributes for visualization
            values[0] = instance.value(0);
            values[1] = instance.value(1);
    
            // Determine class
            int classIndex = (int) instance.classValue();
            if (classIndex == 0) {
                series1.add(values[0], values[1]);
            } else {
                series2.add(values[0], values[1]);
            }
        }
    
        // Create dataset and add series
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
    
        // Create scatter plot
        JFreeChart chart = ChartFactory.createScatterPlot(
                "KNN Visualization", "Attribute 1", "Attribute 2", dataset);
    
        // Set colors and shapes for series
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.RED); // Class 1 - Red
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.BLUE); // Class 2 - Blue
        chart.getXYPlot().getRenderer().setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6)); // Circle for Class 1
        chart.getXYPlot().getRenderer().setSeriesShape(1, new java.awt.geom.Rectangle2D.Double(-3, -3, 6, 6)); // Square for Class 2
    
        // Display in a frame
        JFrame frame = new JFrame("KNN Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new ChartPanel(chart), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}    