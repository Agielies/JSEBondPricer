# JSE Bond Pricer

Program to calculate a South African Government bond’s dirty price, clean price and
accrued interest from its yield for a given settlement date.
The program follows the exact specifications as set out in:
https://clientportal.jse.co.za/Content/JSEValuations%20Methodologies/Bond%20Pricing%20Formula%20-%20Speciﬁcations.pdf

## Running the Project

To build and run the project, use Maven:

1. **Build the project:**

    ```sh
    mvn compile
    ```

2. **Run tests:**

    ```sh
    mvn test
    ```

3. **Generate code coverage report:**

    ```sh
    mvn jacoco:report
    ```
4. **Generate documentation:**

    ```sh
    javadoc -d docs src/main/java/bondpricer/*.java
    ```
## Usage

1. **Instantiate a BondPricer:**

    ```java
    BondType selectedBond = BondType.R186;
    LocalDate settlementDate = LocalDate.of(2017, 2, 7);
    BondPricer bondR186 = new BondPricer(selectedBond, settlementDate);
    ```

2. **Get Accrued Interest, Calculate Dirty and Clean Price:**

    ```java
    double accruedInterst = bondR186.getAccruedInterest();
    // Can also get prices together with getBondPrices()
    double dirtyPrice = bondR186.getAllInPrice(8.75);
    double cleanPrice = bondR186.getCleanPrice(8.75);

    System.out.println("Accrued Interest: " + accruedInterst);
    System.out.println("Dirt Price: " + dirtyPrice);
    System.out.println("Clean Price: " + cleanPrice);
    // Accrued Interest: 1.38082
    // Dirty Price: 112.77263
    // Clean Price: 111.39181
    ```
3. **Update Settlement Date and Recalculate:**

    ```java
    bondR186.setSettlementDate(LocalDate.of(2017, 2, 8));
    double newCleanPrice = bondR186.getCleanPrice(8.8);

    System.out.println("Clean Price " + newcleanPrice);
    // Clean Price: 111.03968 
    ```
## Efficiency Mechanisms

- **Real-Time Pricing:** The assumption was made that the bond needs to be priced in real-time, so the settlement date is set before the market opens. This way, the calculations are done before trading begins, enhancing performance and ensuring that the bond prices are readily available when needed. The settlement date was also set in the constructor to avoid using error handling when the settlement date is not set and prices are calculated.
- **Caching Intermediate Results:** Intermediate results such as discount factors and accrued interest are calculated once and reused, reducing redundant computations and improving efficiency.
- **Lazy Loading**: Calculations are performed only when necessary.

## Program design

- **Object-Oriented Design:**
Encapsulating bond attributes and behaviors within the BondPricer class promotes modularity and reusability. This follows the principles of encapsulation and the single responsibility principle.

- **Functional Programming**: The approach avoids storing intermediate results like clean and dirty prices in instance variables. This ensures that the code remains side-effect-free.

- **Immutable Fields:**
Using final for certain fields makes them immutable, enhancing thread safety and ensuring critical attributes remain constant throughout the object's lifecycle.

- **Constructor Initialization:**
The constructor initializes the object with essential bond details, ensuring the BondPricer object is always in a valid state, following the fail-fast principle.

- **Lazy Initialization and Caching:**
Dependent fields are recalculated only when the settlement date changes, minimizing redundant calculations and improving performance.

- **Method Decomposition:**
Complex calculations are broken into smaller, reusable methods, enhancing readability and maintainability.

- **Factory Method Pattern:**
The constructor acts as a factory method for creating BondPricer instances, abstracting bond details initialization and promoting flexibility.

- **Encapsulation of Logic:**
Core business logic is encapsulated within private methods, adhering to the information hiding principle and reducing the risk of accidental misuse.

## Solution Approach

The solution approach was to:

1. Develop a working solution that adheres to the exact specifications.
2. Refactor the code to improve its structure, readability, and maintainability.
3. Write comprehensive unit tests to ensure the correctness of the implementation.
4. Optimize the solution to improve performance and efficiency.

## Data Structures

- **Primitive Types:** Used wherever possbile for efficiency.
- **LocalDate:** Used for representing dates such as settlement date, next coupon date, etc.
- **MonthDay:** Used for representing dates that recur annually, such as coupon dates.
- **Map:** Used for storing and retrieving bond details efficiently, for debuggin and testing purposes.
- **Array** Used to return clean and dirty prices, when both are required.
- **Enums and Records:** Utilized for representing bond types and details in a type-safe manner.

## Tools Used

- **Maven:** For project management and build automation.
- **Jacoco:** For code coverage analysis.
- **Checkstyle:** For ensuring code adheres to a consistent style.


