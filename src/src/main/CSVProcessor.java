package src.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVProcessor implements Processor {
    public List<String[]> readData(String filePath) {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<String> decimalValues = new ArrayList<>();
                for (String value : values) {
                    if (isDecimal(value)) {
                        decimalValues.add(value);
                    }
                }
                records.add(decimalValues.toArray(new String[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    private boolean isDecimal(String value) {
        try {
            Double.parseDouble(value);
            return value.contains(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void processData(List<String[]> records) {
        for (String[] rec : records) {
            for (String field : rec) {
                System.out.print(field + " ");
            }
            System.out.println();
        }
    }
}
