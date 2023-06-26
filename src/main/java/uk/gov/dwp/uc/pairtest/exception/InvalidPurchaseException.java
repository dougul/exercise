package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {

    public static String notEnoughAdults = "Not Enough Adults for Infants";
    public static String notAtLeastOneAdult = "At least one Adult required with Children/Infants";
    public static String tooManyTickets = "Maximum of 20 tickets per purchase";
    public static String invalidAccountId = "Account Id must be greater than zero";

    public InvalidPurchaseException(String message)
    {
        super(message);
    }
}
