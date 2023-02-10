import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSet {
    // the name of the file (assuming it's in the same directory)
    private final String filename;
    public DataSet(String filename){
        this.filename = filename;
    }

    public List<boolean[]> readData(){
        List<boolean[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.filename))) {
            String line;
            // skip the headers
            br.readLine();
            while ((line = br.readLine()) != null) {
                // split columns on commas
                String[] stringValues = line.split(",");
                boolean[] booleanValues = new boolean[stringValues.length];
                for(int i = 0; i< stringValues.length; i++){
                    // parse from string to boolean
                    int aux = Integer.parseInt(stringValues[i]);
                    if(aux == 1){
                        booleanValues[i] = true;
                    } else {
                        booleanValues[i] = false;
                    }
                }
                records.add(booleanValues);
            }
            return records;
        } catch (IOException e) {
            e.printStackTrace();
            return records;
        }
    }
}
