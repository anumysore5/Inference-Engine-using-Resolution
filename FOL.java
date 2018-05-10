import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FOL {
    private int noOfQueries;
    private int noOfSentencesInKB;
    private List<String> queries = null;
    private List<String> KbSentences = null;
    private Map<String, List<String>> predicateMap = null;
    private Map<String, List<String>> KbLiterals = null;

    public FOL() {
        noOfQueries = 0;
        noOfSentencesInKB = 0;
        if(queries == null)
            queries = new ArrayList<>();
        if(KbSentences == null)
            KbSentences = new ArrayList<>();
        predicateMap = new HashMap<>();
        KbLiterals = new HashMap<>();
    }

    public int getNoOfQueries() {
        return noOfQueries;
    }

    public void setNoOfQueries(int noOfQueries) {
        this.noOfQueries = noOfQueries;
    }

    public int getNoOfSentencesInKB() {
        return noOfSentencesInKB;
    }

    public void setNoOfSentencesInKB(int noOfSentencesInKB) {
        this.noOfSentencesInKB = noOfSentencesInKB;
    }

    public List<String> getQueries() {
        return queries;
    }

    public void setQueries(List<String> queries) {
        this.queries = queries;
    }

    public List<String> getKbSentences() {
        return KbSentences;
    }

    public void setKbSentences(List<String> kbSentences) {
        KbSentences = kbSentences;
    }

    public Map<String, List<String>> getPredicateMap() {
        return predicateMap;
    }

    public void setPredicateMap(Map<String, List<String>> predicateMap) {
        this.predicateMap = predicateMap;
    }

    public Map<String, List<String>> getKbLiterals() {
        return KbLiterals;
    }

    public void setKbLiterals(Map<String, List<String>> kbLiterals) {
        this.KbLiterals = kbLiterals;
    }

    //function that invokes the readInputFile method of FileOperations class
    public void readInputFile() {
        FileOperations fileOperations = new FileOperations();
        try {
            fileOperations.readInputFile(this);
        } catch (IOException e) {
            System.out.println("Some IO exception has occured");
        }
    }

    public void parseKB() {
        List<String> sentences = this.getKbSentences();
        List<String> standardizedKB = new ArrayList<>();

        //For each predicate in the KB, find all the sentences which contain that predicate name. Populate "predicateMap" such that
        //predicate_name is the key and List<all_KBsentences_containing_that_name> are the values.

        //If the there is only literal in a sentence and all its arguments are constants, then add them to a different map KbLiterals
        for(int i=0; i<sentences.size(); i++) {
            String sentence = standardizeInputSentence(i, sentences.get(i));
            standardizedKB.add(sentence);

            if(sentence.contains("|")) {
                String[] disjunctiveClause = sentence.split("\\|");
                for(int j=0; j<disjunctiveClause.length; j++) {
                    disjunctiveClause[j] = disjunctiveClause[j].trim();
                    String key = disjunctiveClause[j].substring(0, disjunctiveClause[j].indexOf("("));
                    if(predicateMap.containsKey(key)) {
                        predicateMap.get(key).add(sentence);
                    } else {
                        List<String> values = new ArrayList<>();
                        values.add(sentence);
                        predicateMap.put(key, values);
                    }
                }
            } else {
                int index = sentence.indexOf("(");
                if(index != -1) {
                    // if the character after "(" is an uppercase letter, then it is a literal of the form "Predicate(Constant)"
                    // check if all the args are constants
                    String[] args = sentence.substring(index+1, sentence.indexOf(")")).split(",");
                    boolean areAllConstants = true;
                    for(int j=0; j<args.length; j++) {
                        if (!Character.isUpperCase((args[j].trim()).charAt(0))) { //one of the arguments is a variable
                            areAllConstants = false;
                            break;
                        }
                    }
                    if(areAllConstants == true) {
                        String key = sentence.substring(0, index);
                        if(KbLiterals.containsKey(key)) {
                            KbLiterals.get(key).add(sentence);
                        } else {
                            List<String> values = new ArrayList<>();
                            values.add(sentence);
                            KbLiterals.put(key, values);
                        }
                    } else {
                        List<String> values = new ArrayList<>();
                        values.add(sentence);
                        predicateMap.put(sentence.substring(0, index).trim(), values);
                    }
                }
            }
        }

        this.setKbSentences(standardizedKB);
    }

    // function to standardize the kb sentence
    // Eg: P(x,y) | P(a,b) | ~A(x) would be converted to P(x0,y0) | P(a0,b0) | ~A(x0)
    private String standardizeInputSentence(int num, String s) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] literals = s.split("\\|");
        for(int i=0; i<literals.length; i++) {
            String str = literals[i].trim();
            int index = str.indexOf("(");
            stringBuilder.append(str.substring(0, index+1));
            String argsStr = str.substring(index+1, str.indexOf(")"));
            String[] args = argsStr.split(",");

            for(int j=0; j<args.length; j++) {
                if(Character.isUpperCase((args[j].trim()).charAt(0))) { //argument is a constant
                    stringBuilder.append(args[j].trim());
                } else { //argument is a variable
                    stringBuilder.append(args[j].trim());
                    stringBuilder.append(num);
                }
                if(j+1 != args.length) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append(")");

            if(i+1 != literals.length) {
                stringBuilder.append("|");
            }
        }

        return stringBuilder.toString();
    }
}