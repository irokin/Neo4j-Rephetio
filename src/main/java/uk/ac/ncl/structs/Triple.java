package uk.ac.ncl.structs;

public class Triple {
    public String sub, obj, pred;
    public boolean label;

    public Triple(String sub, String pred, String obj, boolean label) {
        this(sub, pred, obj);
        this.label = label;
    }

    public Triple(String sub, String pred, String obj) {
        this.sub = sub;
        this.obj = obj;
        this.pred = pred;
    }

    @Override
    public int hashCode() {
        return sub.hashCode() + obj.hashCode() + pred.hashCode() + 12;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Triple) {
            Triple other = (Triple) obj;
            return other.sub.equals(this.sub) && other.obj.equals(this.obj) && other.pred.equals(this.pred);
        }
        return false;
    }

    @Override
    public String toString() {
        return pred + "(" + sub + ", " + obj + ")";
    }

    public String tabSepString() {
        return String.join("\t", new String[]{sub, pred, obj});
    }
}
