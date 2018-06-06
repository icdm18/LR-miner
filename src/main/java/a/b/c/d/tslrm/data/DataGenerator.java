package a.b.c.d.tslrm.data;

import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.random.RandomData;

import java.io.*;
import java.util.*;


public class DataGenerator {
    public static Point2D[] randomWalk(int count, double step) {
        Point2D[] ret = new Point2D[count];

        double x = 0, y = 0;

        for (int i = 0; i < count; i++) {
            ret[i] = new Point2D(x, y);
            double r = Math.random();
            x = x + (r - 0.5) * step;
            r = Math.random();
            y = y + (r - 0.5) * step;
        }
        return ret;
    }

    public static Point2D[] randomWalk1(int count, double step) {
        Point2D[] ret = new Point2D[count];

        double x = 0, y = 0;

        for (int i = 0; i < count; i++) {
            ret[i] = new Point2D(x, y);
            double r = Math.random();
            x = x + Math.signum(r - 0.5) * step;
            r = Math.random();
            y = y + Math.signum(r - 0.5) * step;
        }
        return ret;
    }

    public static Point2D[] randomLinearPoints(double slope, double intercept, int count, double minX, double maxX) {
        Point2D[] ret = new Point2D[count];
        for (int i = 0; i < count; i++) {
            double x = randomValue(minX, maxX);
            double y = slope * x + intercept;
            ret[i] = new Point2D(x, y);
        }
        return ret;
    }


    public static Point2D[] randomLinearPoints(double[] slopes, double intercepts[], double[] weights, int count, int maxSegLength, double minX, double maxX, int[] models) {
        double totalWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            double v = weights[i];
            totalWeight += v;
        }

        Point2D[] ret = new Point2D[count];

        int c = 0;
        while (c < count) {

            double w = randomValue(0, totalWeight);
            double wt = 0;
            int p = 0;
            for (int i = 0; i < weights.length; i++) {
                double v = weights[i];
                wt += v;
                if (w < wt) {
                    p = i;
                    break;
                }
            }


            double slope = slopes[p];
            double intercept = intercepts[p];
            int segLength = (int) randomValue(1, maxSegLength);
            int maxLength = count - c;
            segLength = Math.min(maxLength, segLength);


            Point2D[] segment = randomLinearPoints(slope, intercept, segLength, minX, maxX);
            Arrays.fill(models, c, c + segLength, p);
            System.arraycopy(segment, 0, ret, c, segment.length);
            c = c + segment.length;
        }

        return ret;
    }


    public static Point2D[] addError(Point2D[] points, double xErrorBound, double yErrorBound) {
        Point2D[] ret = new Point2D[points.length];
        for (int i = 0; i < ret.length; i++) {
            double xError = (Math.random() - 0.5) * 2 * xErrorBound;
            double yError = (Math.random() - 0.5) * 2 * yErrorBound;
            ret[i] = new Point2D(points[i].getX() + xError, points[i].getY() + yError);
        }
        return ret;
    }

    public static double randomValue(double minX, double maxX) {
        return minX + (maxX - minX) * Math.random();
    }

    public static void main(String[] args) {
        Point2D[] point2Ds = randomWalk1(100, 2);
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            System.out.println("(x,y) = (" + point2D.getX() + "," + point2D.getY() + ")");
        }
    }

    public static void savePoints2File(Point2D[] points, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            Point2D point = points[i];
            sb.append(point.getX()).append(",");

        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        for (int i = 0; i < points.length; i++) {
            Point2D point = points[i];
            sb.append(point.getY()).append(",");

        }
        sb.append("\n");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        fos.flush();
        fos.close();
    }

    public static Point2D[] readPointsFromFile(File file) throws IOException {
        String s = FileUtils.readFileToString(file);
        String[] split = s.split("\n");
        String[] xs = split[0].split(",");
        String[] ys = split[1].split(",");        
        Point2D[] ret = new Point2D[xs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Point2D(Double.parseDouble(xs[i]), Double.parseDouble(ys[i]));

        }
        return ret;
    }

    public static void savePointsModel2File(double[] slopes, double[] intercepts, int[] models, File file) throws IOException {
        int[] lengths = new int[slopes.length];
        List<int[]> segmentList = new ArrayList<int[]>();
        int model = -1;
        for (int i = 0; i < models.length; i++) {
            if (model != models[i]) {
                segmentList.add(new int[]{models[i], i});
                model = models[i];
            }
            lengths[models[i]]++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("there are " + slopes.length + " linear models!").append("\n");
        for (int i = 0; i < slopes.length; i++) {
            double slope = slopes[i];
            double intercept = intercepts[i];
            int length = lengths[i];
            sb.append("slope=").append(slope).append(",").append("intercept=").append(intercept).append(",length=").append(length).append("\n");
        }

        sb.append("\n");
        sb.append("model segment info:").append("count=").append(segmentList.size()).append("\n");

        int s = 0;
        for (int i = 0; i < segmentList.size(); i++) {
            int[] ints = segmentList.get(i);
            double slope = slopes[ints[0]];
            double intercept = intercepts[ints[0]];
            int start = ints[1];
            int end;
            if (i < segmentList.size() - 1)
                end = segmentList.get(i + 1)[1] - 1;
            else
                end = models.length - 1;
            sb.append("seg no:").append(i).append(",model=").append(ints[0]).append(",[slope=").append(slope).append(",").append("intercept=").append(intercept).append("],[")
                    .append(start).append(",").append(end).append("],length=").append(end - start + 1).append("\n");
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        fos.flush();
        fos.close();
    }

    public static double[] generateData(double min, double max, int count, int bitLength, double slope, double intercept) {
        double[] ret = new double[count];

        double span = max - min;

        double ladderNumber = Math.pow(2, bitLength);
        System.out.println("ladderNumber = " + ladderNumber);

        double resolution = span / ladderNumber;
        System.out.println("resolution = " + resolution);

        for (int i = 0; i < ret.length; i++) {
            double v = randomValue(min, max);


            double v1 = Math.floor((v - min) / span * ladderNumber) / ladderNumber * span + min;


            assert (v1 <= v);
            assert Math.abs(v1 - v) < resolution;
            ret[i] = v1 * slope + intercept;

        }

        return ret;
    }

    public static int detectBitLength(double[] values, int min, int max) {
        int ret = 0;

        Arrays.sort(values);


        double[] deltaValues = new double[values.length];
        deltaValues[0] = 0;
        for (int i = 1; i < deltaValues.length; i++) {
            deltaValues[i] = values[i] - values[i-1];
        }

        for (int i = 0; i < deltaValues.length; i++) {
            double deltaValue = deltaValues[i];
            System.out.println("==>deltaValue[i] = " + deltaValue);
        }

        Arrays.sort(deltaValues);

        double last = -1;

        double[] distinctDelta = new double[deltaValues.length];

        int j = 0;
        for (int i = 0; i < deltaValues.length; i++) {
            if ((deltaValues[i]) >= 0) {
                if (last != deltaValues[i]) {
                    System.out.println("deltaValues[i] = " + deltaValues[i]);
                    last = deltaValues[i];
                    distinctDelta[j] = last;
                    j++;
                }
            }
        }
        System.out.println("j = " + j);

        double[] newValues = new double[j];
        System.arraycopy(distinctDelta,0,newValues,0,j);

        if (newValues.length > 2)
            return detectBitLength(newValues,min,max);
        else {
            double bitLength = Math.log((max - min) / newValues[1]) / Math.log(2);
            System.out.println("bitLength = " + bitLength);
            return (int)bitLength;
        }
    }
}