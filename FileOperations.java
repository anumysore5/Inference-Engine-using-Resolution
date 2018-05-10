import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

class FileOperations {
    BufferedReader reader;

    public FileOperations() {
        try {
            reader = new BufferedReader(new FileReader("input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find \"input.txt\" in the current directory");
        }
    }

    public void readInputFile(FOL fol) throws IOException {
        int noOfQueries = Integer.parseInt(reader.readLine());
        fol.setNoOfQueries(noOfQueries);

        List<String> queries = fol.getQueries();
        for(int i=0; i<noOfQueries; i++) {
            queries.add(reader.readLine());
        }

        int noOfSentences = Integer.parseInt(reader.readLine());
        fol.setNoOfSentencesInKB(noOfSentences);

        List<String> sentences = fol.getKbSentences();
        for(int i=0; i<noOfSentences; i++) {
            sentences.add(reader.readLine());
        }
    }
}