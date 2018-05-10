import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FOLogicOperations {
    public static String SUCCESS = "SUCCESS";
    public static String FAILURE = "FAILURE";
    public static String EMPTY = "EMPTY";
    public static String QUERY_PROVED = "QUERY_PROVED";
    public static String STARTING_CLAUSE = "STARTING_CLAUSE";
    public static String NOT = "~";
    public static String OPENING_BRACES = "(";
    public static String COMMA = ",";
    public static String OR = "\\|";

    public String negateQuery(String query) {
        String negatedQuery = NOT;
        if(query.startsWith(NOT)) {
            negatedQuery = query.substring(1);
        } else {
            negatedQuery = negatedQuery.concat(query.substring(0));
        }
        return negatedQuery;
    }

    public String performRefutation(String negatedQuery, String leftClause, FOL fol) {
        Map<String, List<Unification>> unifiedClauses = new HashMap<>();
        LinkedList<Unification> queue = new LinkedList<>();
        boolean queryProved = false;

        String str = leftClause.substring(0);
        Unification unificationObj = new Unification(str, STARTING_CLAUSE);
        queue.add(unificationObj);

        int i=0;
        int counter = 100000;
        while(!queue.isEmpty()) {
            if(counter == 0) break;
            counter--;

            Unification obj = queue.removeFirst();

            //This is to be done only for the first object in the queue
            if(i==0) {
                findAllPossibleUnifyingSentencesInKB(obj.getLeftClause(), fol, queue);
                i++;
                // No unification possible as there are no predicates in KB to unify with
                if (queue.size() == 0) {
                    return FAILURE;
                }
                continue;
            }

//            System.out.println("left clause: " + obj.getLeftClause());
//            System.out.println("right clause: " + obj.getRightClause());
            String unifiedString = unify(obj, unifiedClauses);

            if(unifiedString != null) {
                unifiedString = simplifyLiterals(unifiedString);
            }
//            System.out.println("After unification: " + obj.getUnificationResult() + "\n");

            if(null != unifiedString) {
                if (unifiedString.equals(QUERY_PROVED)) {
                    queryProved = true;
                    break;
                } else {
                    //infinite loop detection
                    if(!unifiedClauses.isEmpty() && unifiedClauses.containsKey(obj.getLeftClause())) {
                        List<Unification> values = unifiedClauses.get(obj.getLeftClause());
                        boolean comboFound = false;
                        for(int j=0; j<values.size(); j++) {
                            Unification temp = values.get(j);
                            if(temp.getRightClause().equals(obj.getRightClause())) {
                                comboFound = true;
                                break;
                            }
                        }
                        if(comboFound) {
                            continue;
                        }
                    }
                    findAllPossibleUnifyingSentencesInKB(unifiedString, fol, queue);
                }
            }

            if(unifiedClauses.containsKey(obj.getLeftClause())) {
                unifiedClauses.get(obj.getLeftClause()).add(obj);
            } else {
                List<Unification> values = new ArrayList<>();
                values.add(obj);
                unifiedClauses.put(obj.getLeftClause(), values);
            }
        }

        if(queryProved)
            return SUCCESS;
        return FAILURE;
    }

    //function used to remove duplicate literals from a sentence
    //Eg: If unifiedString = A(x)|B(x)|C(x)|A(x) then the function returns A(x)|B(x)|C(x)
    private String simplifyLiterals(String unifiedString) {
        String[] literals = unifiedString.split(OR);
        String simplified = "";
        for(int i=0; i<literals.length; i++) {
            boolean matchFound = false;
            for(int j=i+1; j<literals.length; j++) {
                if (literals[i].equals(literals[j])) {
                    matchFound = true;
                    continue;
                }
            }
            if(!matchFound) {// means this is a unique literal
                simplified = simplified.concat(literals[i]);
                if(i+1 != literals.length) {
                    simplified = simplified.concat("|");
                }
            }
        }
        return simplified;
    }

    private String unify(Unification obj, Map<String, List<Unification>> unifiedClauses) {
        String resultOfUnification = null;
        Map<String, String> literalsToUnify = new HashMap<>();
        List<String> matches = new ArrayList<>();
        Map<String, String> substitutionMap = new HashMap<>();

        int y=0;
        int innerLoop=0, outerLoop=0;
        boolean validSubsFound = false;

        String leftSentence = obj.getLeftClause();
        String rightSentence = obj.getRightClause();
        String[] leftLiterals = leftSentence.split(OR);
        String[] rightLiterals = rightSentence.split(OR);
        outerLoop = leftLiterals.length;
        innerLoop = rightLiterals.length;

        //Find all the literals in both sentences that can be unified i.e. for every literal in left sentence, check against every literal in right sentence
        //to see if they can unify
        for(int i=0; i<outerLoop; i++) {
            for(int j=0; j<innerLoop; j++) {
                leftLiterals[i] = leftLiterals[i].trim();
                rightLiterals[j] = rightLiterals[j].trim();
                String match = findMatchingLiteralInRightSentence(leftLiterals[i], rightLiterals[j]);

                if (match != null) {
                    //check if the combination of left and right literal has already been considered before for unification.
                    //If yes, then skip this iteration and continue with next iteration.
                    //If no, then process this combination further to find the result of unification.
                    if(unifiedClauses.containsKey(leftSentence)) {
                        List<Unification> unificationObjs = unifiedClauses.get(leftSentence);
                        boolean comboDoneBefore = false;
                        for(int k=0; k<unificationObjs.size(); k++) {
                            if(leftLiterals[i].equals(unificationObjs.get(k).getLeftLiteral())) {
                                if (match.equals(unificationObjs.get(k).getRightLiteralToUnifyWith())) {
                                    comboDoneBefore = true;
                                    break;
                                }
                            }
                        }
                        if(comboDoneBefore) {
                            continue;
                        }
                    }

                    if (y == 0) matches.add(match);
                    if (y != 0 && matches.contains(match)) continue;
                    if (y != 0) matches.add(match);
                    y++;

                    if(!literalsToUnify.containsKey(leftLiterals[i])) {
                        literalsToUnify.put(leftLiterals[i], match);

                        //Find out the substitution list
                        validSubsFound = findSubstitutionList(substitutionMap, literalsToUnify);

                        if (!validSubsFound) {
                            literalsToUnify.remove(leftLiterals[i]);
                            continue;
                        }
                    }
                }
            }
        }

        if(!literalsToUnify.isEmpty()) {
            //Remove the corresponding literals from both the sentences
            Set<String> keys = literalsToUnify.keySet();
            for(String key: keys) {
                obj.setLeftLiteral(key);
                obj.setRightLiteralToUnifyWith(literalsToUnify.get(key));
                leftSentence = removeLiteralFromClause(key, leftSentence);
                rightSentence = removeLiteralFromClause(literalsToUnify.get(key), rightSentence);
            }

            //Apply the substitution list to the remaining literals of both sentences wherever applicable
            if (!leftSentence.equals(EMPTY))
                leftSentence = applySubstitution(substitutionMap, leftSentence);
            if (!rightSentence.equals(EMPTY))
                rightSentence = applySubstitution(substitutionMap, rightSentence);

            //unify the 2 sentences
            resultOfUnification = unifyTwoSentences(leftSentence, rightSentence);
            if (resultOfUnification != null) {
                obj.setUnificationResult(resultOfUnification);
            }
        }

        //return the resulting sentence as the result of unification step
        return resultOfUnification;
    }

    private boolean findSubstitutionList(Map<String, String> substitutionMap, Map<String, String> literalsToUnify) {
        Set<String> keys = literalsToUnify.keySet();
        String[] leftArgs;
        String[] rightArgs;
        String value;
        boolean validSubsFound = false;

        for(String key: keys) {
            value = literalsToUnify.get(key);
            leftArgs = (key.substring(key.indexOf(OPENING_BRACES) + 1, key.indexOf(")"))).split(COMMA);
            rightArgs = (value.substring(value.indexOf(OPENING_BRACES) + 1, value.indexOf(")"))).split(COMMA);
            Map<String, String> tempSubstitutionMap = new HashMap<>();

            if (leftArgs.length == rightArgs.length) {
                LiteralSubstitution literalSubstitution = new LiteralSubstitution(leftArgs.length);
                generateLiteralsInfo(leftArgs, rightArgs, literalSubstitution);
                determineWhichLiteralToSubstitute(literalSubstitution, leftArgs, rightArgs);
                tempSubstitutionMap = populateSubstitutionList(literalSubstitution);

                //Only if the entire literal can be unified, add the substitution values to the global map
                if (tempSubstitutionMap != null) {
                    Set<String> tempKeys = tempSubstitutionMap.keySet();
                    for (String tempKey : tempKeys) {
                        substitutionMap.put(tempKey, tempSubstitutionMap.get(tempKey));
                    }
                    validSubsFound = true;
                }
            } else {
                validSubsFound = false;
                break;
            }
        }

        return validSubsFound;
    }

    private Map<String, String> populateSubstitutionList(LiteralSubstitution litSubs) {
        String[] from = litSubs.getSubstituteFrom();
        String[] to = litSubs.getSubstituteTo();
        Map<String, String> tempSubstitutionMap = new HashMap<>();
        boolean isSubsfeasible = true;

        //      left_arg     right_arg       substitution
        // 1.   var          Const           YES
        // 2.   Const        var             YES
        // 3.   var          var             YES
        // 4.   Const        Const           YES only if both are equal
        for(int i=0; i<from.length; i++) {
            from[i] = from[i].trim();
            to[i] = to[i].trim();
            char left = from[i].charAt(0);
            char right = to[i].charAt(0);

            //case 1
            if(Character.isLowerCase(left) && Character.isUpperCase(right)) {
                if(!tempSubstitutionMap.containsKey(from[i])) {
                    tempSubstitutionMap.put(from[i], to[i]);
                } else {
                    //if a substitution already exists for this variable, check it is another variable.
                    //If yes, then replace that substitution with this constant
                    String existingSub = tempSubstitutionMap.get(from[i]);
                    if(Character.isLowerCase(existingSub.charAt(0))) {
                        tempSubstitutionMap.put(from[i], to[i]);
                    }
                    //If it is already a constant, then check if the 2 constants are exactly the same. If yes do nothing.
                    else if(Character.isUpperCase(existingSub.charAt(0)) && existingSub.equals(to[i])) {
                        continue;
                    }
                    //If it is constant and they are not same, then conclude no feasible substitution.
                    else if(Character.isUpperCase(existingSub.charAt(0)) && !existingSub.equals(to[i])) {
                        isSubsfeasible = false;
                        break;
                    }
                }
            }

            //case 2
            else if(Character.isUpperCase(left) && Character.isLowerCase(right)) {
                if(!tempSubstitutionMap.containsKey(to[i])) {
                    tempSubstitutionMap.put(to[i], from[i]);
                } else {
                    String existingSub = tempSubstitutionMap.get(to[i]);
                    if(Character.isLowerCase(existingSub.charAt(0))) {
                        tempSubstitutionMap.put(to[i], from[i]);
                    } else if(Character.isUpperCase(existingSub.charAt(0)) && existingSub.equals(from[i])) {
                        continue;
                    } else if(Character.isUpperCase(existingSub.charAt(0)) && !existingSub.equals(from[i])) {
                        isSubsfeasible = false;
                        break;
                    }
                }
            }

            //case 3
            else if(Character.isLowerCase(left) && Character.isLowerCase(right)) {
                if(!tempSubstitutionMap.containsKey(to[i])) {
                    tempSubstitutionMap.put(to[i], from[i]);
                } else {
                    String existingSub = tempSubstitutionMap.get(to[i]);
                    //if what we are trying to substitute with in the current iteration is same as existing one, then do nothing
                    //Eg: If x0->a0 exists and in this iteration we are trying to replace x0 with a0, then continue
                    if(existingSub.equals(from[i])) {
                        continue;
                    }
                    //Eg: If x0->a0 exists and in this iteration we are trying to replace x0 with b0, then terminate
                    else {
                        isSubsfeasible = false;
                        break;
                    }
                }
            }

            //case 4
            else if(Character.isUpperCase(left) && Character.isUpperCase(right)){
                if(!from[i].equals(to[i])) {
                    isSubsfeasible = false;
                    break;
                }
            }
        }

        if(isSubsfeasible)
            return tempSubstitutionMap;
        return null;
    }

    //function to decide if the arguments of left literal should be substituted with arguments of right literal or vice-versa
    private void determineWhichLiteralToSubstitute(LiteralSubstitution literalSubstitution, String[] leftLiteral, String[] rightLiteral) {
        if(literalSubstitution.getNoOfLeftConstants() >= literalSubstitution.getNoOfRightConstants()) {
            literalSubstitution.setSubstituteFrom(leftLiteral);
            literalSubstitution.setSubstituteTo(rightLiteral);
            return;
        }

        literalSubstitution.setSubstituteFrom(rightLiteral);
        literalSubstitution.setSubstituteTo(leftLiteral);
    }

    //function to determine the count of constants and variables in left and right literals
    private void generateLiteralsInfo(String[] leftArgs, String[] rightArgs, LiteralSubstitution literalSubstitution) {
        int noOfLeftVaribles=0, noOfRightVariables=0, noOfLeftConstants=0, noOfRightConstants=0;
        String[] left = new String[leftArgs.length];
        String[] right = new String[leftArgs.length];;
        for(int i=0; i<leftArgs.length; i++) {
            if(Character.isLowerCase(leftArgs[i].trim().charAt(0))) {
                noOfLeftVaribles++;
            } else {
                noOfLeftConstants++;
            }

            if(Character.isLowerCase(rightArgs[i].trim().charAt(0))) {
                noOfRightVariables++;
            } else {
                noOfRightConstants++;
            }

            left[i] = leftArgs[i].trim().substring(0);
            right[i] = rightArgs[i].trim().substring(0);
        }

        literalSubstitution.setNoOfLeftVaribles(noOfLeftVaribles);
        literalSubstitution.setNoOfLeftConstants(noOfLeftConstants);
        literalSubstitution.setNoOfRightVariables(noOfRightVariables);
        literalSubstitution.setNoOfRightConstants(noOfRightConstants);
        literalSubstitution.setLeftArgs(left);
        literalSubstitution.setRightArgs(right);
    }

    //If leftLiteral= A(x) then return rightLiteral iff it is ~A(x), else return NULL
    //If leftLiteral= ~A(x) then return rightLiteral iff it is A(x), else return NULL
    private String findMatchingLiteralInRightSentence(String leftLiteral, String rightLiteral) {
        if (leftLiteral.startsWith(NOT)) {
            if (leftLiteral.substring(0, leftLiteral.indexOf(OPENING_BRACES)).equals(NOT + rightLiteral.substring(0, rightLiteral.indexOf(OPENING_BRACES)))) {
                return rightLiteral;
            }
        } else {
            if (leftLiteral.substring(0, leftLiteral.indexOf(OPENING_BRACES)).equals(rightLiteral.substring(1, rightLiteral.indexOf(OPENING_BRACES)))) {
                return rightLiteral;
            }
        }
        return null;
    }

    private String unifyTwoSentences(String leftSentence, String rightSentence) {
        String result = null;
        if(leftSentence.equals(EMPTY) && !rightSentence.equals(EMPTY)) {
            result = rightSentence;
        } else if(!leftSentence.equals(EMPTY) && rightSentence.equals(EMPTY)) {
            result = leftSentence;
        } else if(leftSentence.equals(EMPTY) && rightSentence.equals(EMPTY)) {
            result = QUERY_PROVED;
        } else {
            result = leftSentence.concat("|").concat(rightSentence);
        }
        return result;
    }

    private String applySubstitution(Map<String, String> substitutionMap, String sentence) {
        String[] parts = sentence.split(OR);
        sentence = null;
        for(int i=0; i<parts.length; i++) {
            parts[i] = parts[i].trim();
            String substitutedStr = parts[i].substring(0, parts[i].indexOf(OPENING_BRACES)+1);
            String args = parts[i].substring(parts[i].indexOf(OPENING_BRACES)+1, parts[i].indexOf(")"));
            String[] argsList = args.split(COMMA);
            for(int j=0; j<argsList.length; j++) {
                argsList[j] = argsList[j].trim();
                if(substitutionMap.containsKey(argsList[j])) {
                    argsList[j] = substitutionMap.get(argsList[j]);
                }
                substitutedStr = substitutedStr.concat(argsList[j]);
                if(j+1 != argsList.length) {
                    substitutedStr = substitutedStr.concat(COMMA);
                }
                if(j+1 == argsList.length) {
                    substitutedStr = substitutedStr.concat(")");
                }
            }

            if(sentence == null) {
                sentence = substitutedStr.substring(0);
            } else {
                sentence = sentence.concat(substitutedStr);
            }
            if(i+1 != parts.length) {
                sentence = sentence.concat("|");
            }
        }
        return sentence;
    }

    private String removeLiteralFromClause(String literalToRemove, String clauseToRemoveLiteralFrom) {
        String[] literals = null;
        String resultingSentence = null;
        if(literalToRemove.equals(clauseToRemoveLiteralFrom)) {
            return "EMPTY";
        }

        if(clauseToRemoveLiteralFrom.contains("|")) {
            literals = clauseToRemoveLiteralFrom.split(OR);
            for (int i = 0; i < literals.length; i++) {
                literals[i] = literals[i].trim();
                if (literals[i].equals(literalToRemove)) {
                    continue;
                }

                // this is the first literal in the resulting sentence, so don't append "|" character
                if (resultingSentence == null) {
                    resultingSentence = literals[i].substring(0);
                } else {
                    resultingSentence = resultingSentence.concat("|");
                    resultingSentence = resultingSentence.concat(literals[i]);
                }
            }
        }

        return resultingSentence;
    }

    //function the returns the name to be used to search in the predicateMap and KbLiterals map
    //Eg: (a) If str= Mother(x,y) return ~Mother
    //    (b) If str= ~Mother(x,y) return Mother
    private String getLiteralName(String str) {
        if(str.startsWith(NOT)) {
            return str.substring(1, str.indexOf(OPENING_BRACES));
        } else {
            return NOT + str.substring(0, str.indexOf(OPENING_BRACES));
        }
    }

    //function to find all the KB sentences that can be used to unify the literal with. Also add them to the queue- BFS implementation
    private void findAllPossibleUnifyingSentencesInKB(String leftClause, FOL fol, LinkedList<Unification> queue) {
        Map<String, List<String>> predicateMap = fol.getPredicateMap();
        Map<String, List<String>> kbLiterals = fol.getKbLiterals();
        String predicateName = null;

        String[] eachLiteralInClause = leftClause.split(OR);
        for(int j=0; j<eachLiteralInClause.length; j++) {
            predicateName = getLiteralName(eachLiteralInClause[j].trim());

            if(kbLiterals.containsKey(predicateName)) {
                List<String> groundedSentences = kbLiterals.get(predicateName);
                for(String rightClause: groundedSentences) {
                    Unification unificationObj = new Unification(leftClause, rightClause);
                    queue.addLast(unificationObj);
                }
            }

            if (predicateMap.containsKey(predicateName)) {
                List<String> matches = predicateMap.get(predicateName);
                for (int i = 0; i < matches.size(); i++) {
                    Unification unificationObj = new Unification(leftClause, matches.get(i));
                    queue.addLast(unificationObj);
                }
            }
        }
    }
}
