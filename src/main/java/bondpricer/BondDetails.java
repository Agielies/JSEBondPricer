package bondpricer;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * The {@code BondDetails} record represents the details of a bond,
 * including its maturity date, coupon rate, coupon dates,
 * and books close dates.
 *
 * @param maturityDate       the date when the bond matures
 * @param couponRate         the coupon rate paid by the bond
 * @param firstCouponDate    the date of the first coupon payment
 * @param secondCouponDate   the date of the second coupon payment
 * @param firstBooksCloseDate the date when the books close for the first time
 * @param secondBooksCloseDate the date when the books close for the second time
 */
public record BondDetails(
        LocalDate maturityDate,
        double couponRate,
        MonthDay firstCouponDate,
        MonthDay secondCouponDate,
        MonthDay firstBooksCloseDate,
        MonthDay secondBooksCloseDate
) {

}
