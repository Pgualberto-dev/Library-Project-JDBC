package model;

public enum LoanStatus {

    ACTIVE("active"),
    RETURNED("returned"),
    OVERDUE("overdue");

    private final String dbValue;

    LoanStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static LoanStatus fromDb(String value) {
        for (LoanStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown loan status: " + value);
    }
}