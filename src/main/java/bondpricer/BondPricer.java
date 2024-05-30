package bondpricer;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;

/**
 * The {@code BondPricer} class calculates various bond prices and details.
 * The class implements the JSE's Bond Pricing Formula.
 * Specifications can be found at:
 * https://clientportal.jse.co.za/Content/JSEValuations%20Methodologies/Bond%20Pricing%20Formula%20-%20Speciﬁcations.pdf
 * It uses bond-related parameters to perform calculations such as clean price,
 * all-in price, accrued interest, and more.
 */
public class BondPricer {

    private String bondName;
    private final LocalDate maturityDate;
    private final MonthDay firstCouponDate;
    private final MonthDay secondCouponDate;
    private final MonthDay firstBooksCloseDate;
    private final MonthDay secondBooksCloseDate;
    private final double couponRate;
    private final double basicCouponAmount;

    private LocalDate settlementDate;
    private LocalDate nextCouponDate;
    private LocalDate booksCloseDate;
    private LocalDate lastCouponDate;
    private int numberRemainingCoupons;
    private long daysAccrued;
    private boolean cumexFlag;
    private double couponPayable;
    private double accruedInterest;

    private static final int FREQUENCY = 2;
    private static final int DAYSINYEAR = 365;
    private static final double AVERAGEDAYSINYEAR = 365.25;
    private static final int PROUND = 5;
    private static final double SCALE = Math.pow(10, PROUND);

    /**
     * Constructs a {@code BondPricer} with the specified bond type.
     *
     * @param bondType       the bond type
     * @param settlementDate the settlement date
     */
    public BondPricer(final BondType bondType, final LocalDate settlementDate) {
        bondName = bondType.name();
        BondDetails details = bondType.getBondDetails();
        maturityDate = details.maturityDate();
        couponRate = details.couponRate();
        firstCouponDate = details.firstCouponDate();
        secondCouponDate = details.secondCouponDate();
        firstBooksCloseDate = details.firstBooksCloseDate();
        secondBooksCloseDate = details.secondBooksCloseDate();
        basicCouponAmount = couponRate / FREQUENCY;
        setSettlementDate(settlementDate);
    }

    /**
     * Sets the settlement date and calculates date related values.
     *
     * @param settlementDate the settlement date
     */
    public void setSettlementDate(final LocalDate settlementDate) {
        this.settlementDate = settlementDate;
        nextCouponDate = getNextCouponDate();
        booksCloseDate = getBooksCloseDate();
        lastCouponDate = getLastCouponDate();
        numberRemainingCoupons = calculateRemainingCoupons();
        cumexFlag = isCumEx();
        daysAccrued = calculateDaysAccrued();
        couponPayable = calculateCouponPayable();
        accruedInterest = calculateAccruedInterest();
    }

    /**
     * Gets the accrued interest.
     *
     * @return the accrued interest
     */
    public double getAccruedInterest() {
        return Math.round(accruedInterest * SCALE) / SCALE;
    }

    /**
     * Calculates the all-in price (dirty price) of the bond for the specified
     * yield.
     *
     * @param yield the yield
     * @return the all-in price
     */
    public double getAllInPrice(final double yield) {
        double discountFactor = calculateDiscountFactor(yield);
        double brokenPeriod = calculateBrokenPeriod(settlementDate,
                nextCouponDate,
                discountFactor);
        double brokenPeriodDiscountFactor = calculateBrokenPeriodDiscountFactor(
                nextCouponDate,
                discountFactor,
                brokenPeriod);
        return Math.round(calculateAllInPrice(discountFactor,
                brokenPeriod,
                brokenPeriodDiscountFactor,
                yield) * SCALE) / SCALE;
    }

    /**
     * Calculates the clean price of the bond for the specified yield.
     *
     * @param yield the yield
     * @return the clean price
     */
    public double getCleanPrice(final double yield) {
        return Math.round(calculateCleanPrice(getAllInPrice(yield))
                * SCALE) / SCALE;
    }

    /**
     * Gets the clean and dirty prices of the bond for the specified yield.
     *
     * @param yield the yield
     * @return an array containing the clean price at index 0 and the
     *         dirty price at index 1
     */
    public double[] getBondPrices(final double yield) {
        double discountFactor = calculateDiscountFactor(yield);
        double brokenPeriod = calculateBrokenPeriod(settlementDate,
                nextCouponDate,
                discountFactor);
        double brokenPeriodDiscountFactor = calculateBrokenPeriodDiscountFactor(
                nextCouponDate,
                discountFactor,
                brokenPeriod);
        double allInPrice = Math.round(
                calculateAllInPrice(discountFactor,
                        brokenPeriod,
                        brokenPeriodDiscountFactor, yield) * SCALE)
                / SCALE;
        double cleanPrice = Math.round(calculateCleanPrice(allInPrice)
                * SCALE) / SCALE;
        return new double[] { cleanPrice, allInPrice };
    }

    private double calculateCleanPrice(final double allInPrice) {
        return allInPrice - accruedInterest;
    }

    private double calculateAllInPrice(final double discountFactor,
            final double brokenPeriod, final double brokenPeriodDiscountFactor,
            final double yield) {
        return brokenPeriodDiscountFactor * (couponPayable
                + basicCouponAmount * (discountFactor
                        * (1 - Math.pow(discountFactor, numberRemainingCoupons))
                        / (1 - discountFactor))
                + 100 * Math.pow(discountFactor, numberRemainingCoupons));
    }

    private LocalDate getNextCouponDate() {
        return (settlementDate.isBefore(
                firstCouponDate.atYear(settlementDate.getYear())))
                        ? firstCouponDate.atYear(settlementDate.getYear())
                        : (settlementDate.isBefore(secondCouponDate.atYear(
                                settlementDate.getYear())))
                                        ? secondCouponDate.atYear(settlementDate.getYear())
                                        : firstCouponDate.atYear(settlementDate.getYear() + 1);
    }

    private LocalDate getLastCouponDate() {
        return (nextCouponDate.equals(firstCouponDate.atYear(nextCouponDate.getYear())))
                ? secondCouponDate.atYear(nextCouponDate.getYear() - 1)
                : firstCouponDate.atYear(nextCouponDate.getYear());
    }

    private LocalDate getBooksCloseDate() {
        return (nextCouponDate.equals(firstCouponDate.atYear(nextCouponDate.getYear()))
                ? firstBooksCloseDate.atYear(nextCouponDate.getYear())
                : secondBooksCloseDate.atYear(nextCouponDate.getYear()));
    }

    private int calculateRemainingCoupons() {
        long daysBetween = ChronoUnit.DAYS.between(nextCouponDate, maturityDate);
        return (int) Math.round(daysBetween / (AVERAGEDAYSINYEAR / FREQUENCY));
    }

    private boolean isCumEx() {
        return settlementDate.isBefore(booksCloseDate);
    }

    private long calculateDaysAccrued() {
        return cumexFlag ? ChronoUnit.DAYS.between(lastCouponDate, settlementDate)
                : ChronoUnit.DAYS.between(nextCouponDate, settlementDate);
    }

    private double calculateDiscountFactor(final double yield) {
        return 1 / (1 + yield / (100 * FREQUENCY));
    }

    private double calculateCouponPayable() {
        return cumexFlag ? basicCouponAmount : 0;
    }

    private double calculateAccruedInterest() {
        return daysAccrued * couponRate / DAYSINYEAR;
    }

    private double calculateBrokenPeriod(final LocalDate settlementDate,
            final LocalDate nextCouponDate, final double discountFactor) {
        return (!nextCouponDate.isEqual(maturityDate))
                ? (double) ChronoUnit.DAYS.between(settlementDate, nextCouponDate)
                        / ChronoUnit.DAYS.between(lastCouponDate, nextCouponDate)
                : ChronoUnit.DAYS.between(settlementDate, nextCouponDate) / (DAYSINYEAR / FREQUENCY);
    }

    private double calculateBrokenPeriodDiscountFactor(final LocalDate nextCouponDate, final double discountFactor,
            final double brokenPeriod) {
        return (!nextCouponDate.isEqual(maturityDate)) ? Math.pow(discountFactor, brokenPeriod)
                : discountFactor / (discountFactor + brokenPeriod * (1 - discountFactor));
    }

    /**
     * Gets the bond details in a map.
     *
     * @param yield the yield
     * @return a map containing the bond details
     */
    public Map<String, Object> getBondDetails(final double yield) {
        Map<String, Object> bondDetails = new HashMap<>();
        double discountFactor = calculateDiscountFactor(yield);
        double brokenPeriod = calculateBrokenPeriod(settlementDate, nextCouponDate, accruedInterest);
        double brokenPeriodDiscountFactor = calculateBrokenPeriodDiscountFactor(nextCouponDate, discountFactor,
                brokenPeriod);
        double allInPrice = calculateAllInPrice(discountFactor, brokenPeriod, brokenPeriodDiscountFactor, yield);
        double cleanPrice = calculateCleanPrice(allInPrice);

        bondDetails.put("nextCouponDate", nextCouponDate);
        bondDetails.put("lastCouponDate", lastCouponDate);
        bondDetails.put("booksCloseDate", booksCloseDate);
        bondDetails.put("numberRemainingCoupons", numberRemainingCoupons);
        bondDetails.put("cumexFlag", cumexFlag);
        bondDetails.put("daysAccrued", daysAccrued);
        bondDetails.put("accruedInterest", accruedInterest);
        bondDetails.put("brokenPeriod", brokenPeriod);
        bondDetails.put("brokenPeriodDiscountFactor", brokenPeriodDiscountFactor);
        bondDetails.put("discountFactor", discountFactor);
        bondDetails.put("couponPayable", couponPayable);
        bondDetails.put("allInPrice", allInPrice);
        bondDetails.put("cleanPrice", cleanPrice);

        return bondDetails;
    }

    /**
     * Returns a string representation of the bond details.
     *
     * @return a string containing the bond details
     */
    @Override
    public String toString() {
        return "The bond type is: " + bondName + "\n" +
                "The next coupon date is: " + nextCouponDate + "\n" +
                "The books close date is: " + booksCloseDate + "\n" +
                "Number of remaining coupons: " + numberRemainingCoupons + "\n" +
                "Cum-ex flag: " + cumexFlag + "\n" +
                "Days accrued interest: " + daysAccrued + "\n" +
                "Accrued Interest: " + accruedInterest + "\n" +
                "Coupon at Next Coupon Date: " + couponPayable;
    }
}
