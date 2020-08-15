package uk.ac.ncl.structs;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Prediction {
    public String head, tail;
    public long headId, tailId;
    public double confidence;
    public Map<String, Double> positiveEvidences = new LinkedHashMap<>();
    public Map<String, Double> negativeEvidences = new LinkedHashMap<>();

    public Prediction(String head, String tail, double confidence) {
        this.head = head;
        this.tail = tail;
        this.confidence = confidence;
    }

    @Override
    public int hashCode() {
        return head.hashCode() + 3 * tail.hashCode() +  (int) (10 * confidence);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Prediction) {
            Prediction right = (Prediction) obj;
            return this.head.equals(right.head)
                    && this.tail.equals(right.tail)
                    && this.confidence == right.confidence;
        }
        return false;
    }

    @Override
    public String toString() {
        DecimalFormat f = new DecimalFormat("##.####");
        return String.join(",", Arrays.asList(head, tail, f.format(confidence)));
    }
}
