import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class InferenceEngineMain{
    public static void main(String[] args) {
        FOL fol = new FOL();
        fol.readInputFile();
        fol.parseKB();

        FOLogicOperations logicOperations = new FOLogicOperations();
        List<String> queriesToProve = fol.getQueries();
        String result = "";

        Path path = Paths.get("output.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (int i = 0; i < queriesToProve.size(); i++) {
                String originalQuery = queriesToProve.get(i);
                String negatedQuery = logicOperations.negateQuery(originalQuery);
                addNegatedQueryToKBLiterals(fol, negatedQuery);
                result = logicOperations.performRefutation(originalQuery, negatedQuery, fol);
                removeNegatedQueryFromKBLiterals(fol, negatedQuery);

                if (result.equals("SUCCESS")) {
                    writer.write("TRUE");
                } else {
                    writer.write("FALSE");
                }
                if(i+1 != queriesToProve.size()) {
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot write to output.txt");
        }
    }

    //function that adds the negation of the query to the KbLiterals map before performing refutation step
    private static void addNegatedQueryToKBLiterals(FOL fol, String negatedQuery) {
        String negatedQueryPredicateName = negatedQuery.substring(0, negatedQuery.indexOf("("));
        Map<String, List<String>> temp = fol.getKbLiterals();
        if(temp.containsKey(negatedQueryPredicateName)) {
            temp.get(negatedQueryPredicateName).add(negatedQuery);
        } else {
            List<String> values = new ArrayList<>();
            values.add(negatedQuery);
            temp.put(negatedQueryPredicateName, values);
        }
    }

    //function that deletes the negated query from the KB after proving the query to be true or false
    private static void removeNegatedQueryFromKBLiterals(FOL fol, String negatedQuery) {
        String negatedQueryPredicateName = negatedQuery.substring(0, negatedQuery.indexOf("("));
        Map<String, List<String>> temp = fol.getKbLiterals();
        // get all the sentences with this predicate name
        List<String> values = temp.get(negatedQueryPredicateName);
        // remove only the negated query from among them
        values.remove(negatedQuery);
        // after removal, if the size of the list is zero (i.e. if this was the only sentence in the KB with this predicate name), then remove that entry itself from the Literals map
        if(values.size() == 0) {
            temp.remove(negatedQueryPredicateName);
        }
    }
}