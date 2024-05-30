package bondpricer;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * The {@code BondType} enum represents different types
 * of bonds along with their details.
 */
public enum BondType {

    /**
     * Represents the R186 bond type.
     */
    R186(new BondDetails(LocalDate.of(2026, 12, 21), 10.5, MonthDay.of(6, 21),
        MonthDay.of(12, 21), MonthDay.of(6, 11), MonthDay.of(12, 11))),

    /**
     * Represents the R2032 bond type.
     */
    R2032(new BondDetails(LocalDate.of(2032, 3, 31), 8.25, MonthDay.of(3, 31),
        MonthDay.of(9, 30), MonthDay.of(3, 21), MonthDay.of(9, 20)));

     /**
     * The bond details associated with the bond type.
     */

    private final BondDetails bondDetails;

    /**
     * Constructs a {@code BondType} enum with the specified bond details.
     *
     * @param bondDetails the bond details
     */
    BondType(final BondDetails bondDetails) {
        this.bondDetails = bondDetails;
    }

    /**
     * Gets the bond details associated with this bond type.
     *
     * @return the bond details
     */
    public BondDetails getBondDetails() {
        return bondDetails;
    }
}



