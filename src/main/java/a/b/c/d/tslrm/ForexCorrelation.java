package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.ForexData;

import java.io.Serializable;
import java.util.Set;


public class ForexCorrelation implements Serializable {
    public String currencyX;
    public String currencyY;
    public double error;
    public double k;
    public double b;
    public int maxLength;

    public transient ForexData forexDataX;
    public transient ForexData forexDataY;

    public Set<Integer> positions;

    public ForexCorrelation(String currencyX, String currencyY, double error, double k, double b, int maxLength, ForexData forexDataX, ForexData forexDataY, Set<Integer> positions) {
        this.currencyX = currencyX;
        this.currencyY = currencyY;
        this.error = error;
        this.k = k;
        this.b = b;
        this.maxLength = maxLength;
        this.forexDataX = forexDataX;
        this.forexDataY = forexDataY;
        this.positions = positions;
    }

    @Override
    public String toString() {
        return "ForexCorrelation{" +
                "currencyX='" + currencyX + '\'' +
                ", currencyY='" + currencyY + '\'' +
                ", error=" + error +
                ", k=" + k +
                ", b=" + b +
                ", maxLength=" + maxLength +
                ", forexDataX=" + forexDataX +
                ", forexDataY=" + forexDataY +
                ", positions.size()=" + positions.size() +
                '}';
    }
}
