class LiteralSubstitution {
    private int noOfLeftVaribles=0;
    private int noOfRightVariables=0;
    private int noOfLeftConstants=0;
    private int noOfRightConstants=0;
    private String[] leftArgs;
    private String[] rightArgs;
    private String[] substituteTo;
    private String[] substituteFrom;

    public LiteralSubstitution(int length) {
        this.noOfLeftVaribles=0;
        this.noOfRightVariables=0;
        this.noOfLeftConstants=0;
        this.noOfRightConstants=0;
        leftArgs = new String[length];
        rightArgs = new String[length];
    }

    public int getNoOfLeftVaribles() {
        return noOfLeftVaribles;
    }

    public void setNoOfLeftVaribles(int noOfLeftVaribles) {
        this.noOfLeftVaribles = noOfLeftVaribles;
    }

    public int getNoOfRightVariables() {
        return noOfRightVariables;
    }

    public void setNoOfRightVariables(int noOfRightVariables) {
        this.noOfRightVariables = noOfRightVariables;
    }

    public int getNoOfLeftConstants() {
        return noOfLeftConstants;
    }

    public void setNoOfLeftConstants(int noOfLeftConstants) {
        this.noOfLeftConstants = noOfLeftConstants;
    }

    public int getNoOfRightConstants() {
        return noOfRightConstants;
    }

    public void setNoOfRightConstants(int noOfRightConstants) {
        this.noOfRightConstants = noOfRightConstants;
    }

    public String[] getLeftArgs() {
        return leftArgs;
    }

    public void setLeftArgs(String[] leftArgs) {
        this.leftArgs = leftArgs;
    }

    public String[] getRightArgs() {
        return rightArgs;
    }

    public void setRightArgs(String[] rightArgs) {
        this.rightArgs = rightArgs;
    }

    public String[] getSubstituteTo() {
        return substituteTo;
    }

    public void setSubstituteTo(String[] substituteTo) {
        this.substituteTo = substituteTo;
    }

    public String[] getSubstituteFrom() {
        return substituteFrom;
    }

    public void setSubstituteFrom(String[] substituteFrom) {
        this.substituteFrom = substituteFrom;
    }
}