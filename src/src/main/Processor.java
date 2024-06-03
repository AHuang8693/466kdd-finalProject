package src.main;

import java.util.List;

public interface Processor {
    List<String[]> readData(String filePath);
    void processData(List<String[]> records);
}
