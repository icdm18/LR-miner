package a.b.c.d.tslrm;

import org.apache.commons.collections.CollectionUtils;

import java.util.Set;


public class CorrelationUtil {
    public static double calcCorrelation(Set set1, Set set2)
    {
        int unionSize = CollectionUtils.union(set1, set2).size();
        int intersectionSize = CollectionUtils.intersection(set1, set2).size();
        return intersectionSize*1.0/unionSize;
    }
}
