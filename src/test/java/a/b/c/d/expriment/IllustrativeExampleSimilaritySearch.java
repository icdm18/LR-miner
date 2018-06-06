package a.b.c.d.expriment;

import a.b.c.d.tslrm.correlation.PearsonsCorrelationTest;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class IllustrativeExampleSimilaritySearch {

    public static void main(String args[]) throws IOException {
        double epsilon = 0.1;

        IllustrativeExampleSimilaritySearch iess = new IllustrativeExampleSimilaritySearch();


        Point2D[] data = iess.readFromFile("illustrative_example_data_4.csv", 0, 1);

        List list = FileUtils.readLines(new File("illustrative_example_segment_4T1.csv"));
        int cnt = 0;
        for (Object aList : list) {
            String s = (String) aList;
            if (s.startsWith("1")) {
                cnt++;
            }
        }
        Point2D[] data2 = new Point2D[cnt];
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            if (s.startsWith("1")) {
                data2[cnt] = data[i];
                cnt++;
            }
        }
        double pearsonCorrelation = PearsonsCorrelationTest.correlation(data2);
        System.out.println(pearsonCorrelation);

    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB) throws IOException {
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;









        data = new Point2D[list.size()];
        int cnt2 = 0;
        for (int i = 0; i < list.size(); i++) {

            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        return data;
    }

    private void printResultFile(String outFile, Point2D[] points, double k, double b, double errorBound) throws IOException {
        int consecutiveNum = 0;
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        for (Point2D point : points) {
            double x = point.getX();
            double y = point.getY();
            double estimateY = k * x + b;
            if (Math.abs(estimateY - y) < errorBound) {
                consecutiveNum++;
            } else {
                pw.println("0");
                if (consecutiveNum >= 3) {
                    while (consecutiveNum > 0) {
                        pw.println("1");
                        consecutiveNum--;
                    }
                } else {
                    while (consecutiveNum > 0) {
                        pw.println("0");
                        consecutiveNum--;
                    }
                }
            }
        }
        if (consecutiveNum >= 3) {
            while (consecutiveNum > 0) {
                pw.println("1");
                consecutiveNum--;
            }
        } else {
            while (consecutiveNum > 0) {
                pw.println("0");
                consecutiveNum--;
            }
        }
        pw.close();
    }

}
