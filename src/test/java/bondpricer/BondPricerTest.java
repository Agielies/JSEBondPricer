package bondpricer;

import java.time.LocalDate;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Unit test for bondpricer.
 */
public class BondPricerTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BondPricerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public void testNextCouponDate() {
        // Test how the next coupon date is generated

        BondType selectedBond = BondType.R186;

        // Date at which the settlement date falls between the first and second coupon
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        Map<String, Object> bondDetails = bondR186.getBondDetails(7.5);
        LocalDate nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");

        assertEquals(LocalDate.of(2005, 12, 21), nextCouponDate);

        // Date at which the settlement date falls on coupon
        bondR186.setSettlementDate(LocalDate.of(2005, 12, 21));
        bondDetails = bondR186.getBondDetails(7.5);
        nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");

        assertEquals(LocalDate.of(2006, 6, 21), nextCouponDate);

        // Date at which the settlement date falls after the second coupon
        bondR186.setSettlementDate(LocalDate.of(2005, 12, 25));
        bondDetails = bondR186.getBondDetails(7.5);
        nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");

        assertEquals(LocalDate.of(2006, 6, 21), nextCouponDate);

        // Date at which the settlement date falls before the first coupon date
        bondR186.setSettlementDate(LocalDate.of(2005, 5, 20));
        bondDetails = bondR186.getBondDetails(7.5);
        nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");

        assertEquals(LocalDate.of(2005, 6, 21), nextCouponDate);
    }

    public void testLastCouponDate() {

        BondType selectedBond = BondType.R186;

        // Date at which next coupon is the first coupon
        LocalDate settlementDate = LocalDate.of(2005, 6, 01);

        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        Map<String, Object> bondDetails = bondR186.getBondDetails(7.5);
        LocalDate lastCouponDate = (LocalDate) bondDetails.get("lastCouponDate");

        assertEquals(LocalDate.of(2004, 12, 21), lastCouponDate);

        // Date at which last coupon is the second coupon
        bondR186.setSettlementDate(LocalDate.of(2006, 1, 1));
        bondDetails = bondR186.getBondDetails(7.5);
        lastCouponDate = (LocalDate) bondDetails.get("lastCouponDate");

        assertEquals(LocalDate.of(2005, 12, 21), lastCouponDate);

    }

    public void testBooksCloseDate() {

        BondType selectedBond = BondType.R186;

        // Date at which next coupon is the first coupon
        LocalDate settlementDate = LocalDate.of(2005, 6, 01);

        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        Map<String, Object> bondDetails = bondR186.getBondDetails(7.5);
        LocalDate booksCloseDate = (LocalDate) bondDetails.get("booksCloseDate");

        assertEquals(LocalDate.of(2005, 6, 11), booksCloseDate);

        // Date at which last coupon is the second coupon
        bondR186.setSettlementDate(LocalDate.of(2005, 12, 20));
        bondDetails = bondR186.getBondDetails(7.5);
        booksCloseDate = (LocalDate) bondDetails.get("booksCloseDate");

        assertEquals(LocalDate.of(2005, 12, 11), booksCloseDate);

    }

    public void testCouponPayable() {

        BondType selectedBond = BondType.R186;

        // Date bewtween books close date and coupon date
        LocalDate settlementDate = LocalDate.of(2005, 6, 12);

        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        Map<String, Object> bondDetails = bondR186.getBondDetails(7.5);

        double couponPayable = (double) bondDetails.get("couponPayable");

        assertEquals(0.0, couponPayable);

    }

    public void testBrokenPeriod() {

        BondType selectedBond = BondType.R186;

        LocalDate settlementDate = LocalDate.of(2005, 8, 26);

        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        Map<String, Object> bondDetails = bondR186.getBondDetails(7.5);

        double discountFactor = (double) bondDetails.get("discountFactor");
        double brokenPeriod = (double) bondDetails.get("brokenPeriod");
        double brokenPeriodDiscountFactor = (double) bondDetails.get("brokenPeriodDiscountFactor");

        assertEquals(0.963855422, discountFactor, 9);
        assertEquals(0.639344262, brokenPeriod, 9);
        assertEquals(.976738028, brokenPeriodDiscountFactor, 9);

        // Update settlement so it's treated as a money market instrument
        bondR186.setSettlementDate(LocalDate.of(2026, 7, 28));

        bondDetails = bondR186.getBondDetails(7.5);
        brokenPeriod = (double) bondDetails.get("brokenPeriod");
        brokenPeriodDiscountFactor = (double) bondDetails.get("brokenPeriodDiscountFactor");

        assertEquals(0.800000000, brokenPeriod, 9);
        assertEquals(0.970873786, brokenPeriodDiscountFactor, 9);
    }

    public void testAccruedInterest() {

        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        double accruedInterest = bondR186.getAccruedInterest();
        assertEquals(1.89863, accruedInterest);
    }

    public void testToString() {

        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);

        String expected = "The bond type is: R186\n" +
                "The next coupon date is: 2005-12-21\n" +
                "The books close date is: 2005-12-11\n" +
                "Number of remaining coupons: 42\n" +
                "Cum-ex flag: true\n" +
                "Days accrued interest: 66\n" +
                "Accrued Interest: 1.8986301369863015\n" +
                "Coupon at Next Coupon Date: 5.25";

        String actual = bondR186.toString();
        assertEquals(expected, actual);
    }

    public void testCalculateCleanPrice() {

        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        double cleanPrice = bondR186.getCleanPrice(7.5);

        assertEquals(131.64846, cleanPrice); 
    }

    public void testCalculateAllInPrice() {

        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        double dirtyPrice = bondR186.getAllInPrice(7.5);
        assertEquals(133.54709, dirtyPrice);
    }

    public void testBondPrices() {

        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2005, 8, 26);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
        double[] prices = bondR186.getBondPrices(7.5);

        assertEquals(131.64846, prices[0]);
        assertEquals(133.54709, prices[1]);
    }

    public void testFirstExample() {

        // Example 1 from assignment
        BondType selectedBond = BondType.R186;
        LocalDate settlementDate = LocalDate.of(2017, 2, 7);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);

        Map<String, Object> bondDetails = bondR186.getBondDetails(8.75);

        LocalDate nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");
        LocalDate lastCouponDate = (LocalDate) bondDetails.get("lastCouponDate");
        LocalDate booksCloseDate = (LocalDate) bondDetails.get("booksCloseDate");
        double couponPayable = (double) bondDetails.get("couponPayable");
        double discountFactor = (double) bondDetails.get("discountFactor");
        double brokenPeriod = (double) bondDetails.get("brokenPeriod");
        double brokenPeriodDiscountFactor = (double) bondDetails.get("brokenPeriodDiscountFactor");
        double allInPrice = (double) bondDetails.get("allInPrice");
        double cleanPrice = (double) bondDetails.get("cleanPrice");

        assertEquals(LocalDate.of(2017, 6, 21), nextCouponDate);
        assertEquals(LocalDate.of(2016, 12, 21), lastCouponDate);
        assertEquals(LocalDate.of(2017, 6, 11), booksCloseDate);
        assertEquals(5.25, couponPayable);
        assertEquals(0.958083832, discountFactor, 9);
        assertEquals(0.736263736, brokenPeriod, 9);
        assertEquals(0.968964977, brokenPeriodDiscountFactor, 9);
        assertEquals(112.77263, allInPrice, 5);
        assertEquals(111.39181, cleanPrice, 5);
    }

    public void testSecondExample() {

        // Example 2 from assignment
        BondType selectedBond = BondType.R2032;
        LocalDate settlementDate = LocalDate.of(2024, 5, 16);
        BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);

        Map<String, Object> bondDetails = bondR186.getBondDetails(9.5);

        LocalDate nextCouponDate = (LocalDate) bondDetails.get("nextCouponDate");
        LocalDate lastCouponDate = (LocalDate) bondDetails.get("lastCouponDate");
        LocalDate booksCloseDate = (LocalDate) bondDetails.get("booksCloseDate");
        double couponPayable = (double) bondDetails.get("couponPayable");
        double discountFactor = (double) bondDetails.get("discountFactor");
        double brokenPeriod = (double) bondDetails.get("brokenPeriod");
        double brokenPeriodDiscountFactor = (double) bondDetails.get("brokenPeriodDiscountFactor");
        double allInPrice = (double) bondDetails.get("allInPrice");
        double cleanPrice = (double) bondDetails.get("cleanPrice");

        assertEquals(LocalDate.of(2024, 9, 30), nextCouponDate);
        assertEquals(LocalDate.of(2024, 3, 31), lastCouponDate);
        assertEquals(LocalDate.of(2024, 9, 20), booksCloseDate);
        assertEquals(4.125, couponPayable);
        assertEquals(0.954653938, discountFactor, 9);
        assertEquals(0.748633880, brokenPeriod, 9);
        assertEquals(0.965855171, brokenPeriodDiscountFactor, 9);
        assertEquals(94.19666, allInPrice, 5);
        assertEquals(93.15693, cleanPrice, 5);
    }
}




