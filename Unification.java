class Unification {
    private String leftClause;
    private String rightClause;
    private String leftLiteral;
    private String rightLiteralToUnifyWith;
    private String unificationResult;

    public Unification(String leftStr, String rightStr) {
        this.leftClause = leftStr;
        this.rightClause = rightStr;
    }

    public String getLeftClause() {
        return leftClause;
    }

    public void setLeftClause(String leftClause) {
        this.leftClause = leftClause;
    }

    public String getRightClause() {
        return rightClause;
    }

    public void setRightClause(String rightClause) {
        this.rightClause = rightClause;
    }

    public String getLeftLiteral() {
        return leftLiteral;
    }

    public void setLeftLiteral(String leftLiteral) {
        this.leftLiteral = leftLiteral;
    }

    public String getRightLiteralToUnifyWith() {
        return rightLiteralToUnifyWith;
    }

    public void setRightLiteralToUnifyWith(String rightLiteralToUnifyWith) {
        this.rightLiteralToUnifyWith = rightLiteralToUnifyWith;
    }

    public String getUnificationResult() {
        return unificationResult;
    }

    public void setUnificationResult(String unificationResult) {
        this.unificationResult = unificationResult;
    }
}
