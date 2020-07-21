import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class JavaTests {
    @Test
    public void tables() {
        Table<String, String, Integer> report = HashBasedTable.create();

        report.put("James", "Math", 100);
        report.put("Alice", "Java", 100);

        System.out.println( Math.pow(25, 0.5));

        int startD = 4;
        int endD = 1;

        int[] degrees = new int[]{4,2,1,4};
        double dump = 0.5;
        double pdp = 1;
        for (int degree : degrees) {
            pdp *= (double) 1 / Math.pow(degree, dump);
        }
        System.out.println(pdp);
    }

    @Test
    public void jsonTest() {
        File file = new File("data/Json/t.json");


    }

    public class User {
        private int id;
        private String name;
        private transient String nationality;

        public User(int id, String name, String nationality) {
            this.id = id;
            this.name = name;
            this.nationality = nationality;
        }

        public User(int id, String name) {
            this(id, name, null);
        }
    }

    @Test
    public void test(){
        DecimalFormat f = new DecimalFormat("#.####");
        Double a = 0.12312421412421421;
        System.out.println(f.format(a));
    }

}
