package a.b.c.d.tslrm;


public class Segment {
    int start;
    int end;
    double slope;
    double intercept;
    double mse;

    Segment(int start, int end, double slope, double intercept, double mse) {
        this.start = start;
        this.end = end;
        this.slope = slope;
        this.intercept = intercept;
        this.mse = mse;
    }

    int getLength() {
        return (end - start + 1);
    }

    @Override
    public String toString() {
        return  "["+start + "," + end + "],  [" + slope + "," + intercept + "," + mse +"]";
    }
}
