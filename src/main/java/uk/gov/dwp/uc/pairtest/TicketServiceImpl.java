package uk.gov.dwp.uc.pairtest;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketOrder;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    private TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    private SeatReservationService seatReservationService = new SeatReservationServiceImpl(); {
        
    };
    private final int adultPrice = 20;
    private final int childPrice = 10;
    //private final int infantPrice = 0;

    @Override
    public void purchaseTickets(long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        // populate TicketTypeRequest List.
        List<TicketTypeRequest> ticketRequestList = new LinkedList<TicketTypeRequest>();
        for(int i = 0; i < ticketTypeRequests.length; i++){
            ticketRequestList.add(ticketTypeRequests[i]);
        }
        
        //createTicketOrder 
        TicketOrder order = new TicketOrder(accountId, ticketRequestList);

        //validateOrder
        validateTicketOrder(order);

        //calculate seats 
        int totalSeats = calculateNumberOfSeats(order);
        //calculate price
        int totalPrice = calculatePrice(order);

        //call service seat and payment service.
        ticketPaymentService.makePayment(order.getAccountId(), totalPrice); //if payments fails seatReservationService should not be called
        seatReservationService.reserveSeat(order.getAccountId(), totalSeats);
    }

    private int calculateNumberOfSeats(TicketOrder order) {
        int seats = 0;
        int adults = 0;
        int children = 0;

        adults = numberOfTicketType(order, Type.ADULT);
        children = numberOfTicketType(order, Type.CHILD);

        seats = adults + children;
        System.out.println("Number of seats: " + seats); //should be logged or removed
        order.setNumberOfSeats(seats);
        return seats;
    }

    private int calculatePrice(TicketOrder order) {
        int adults = 0;
        int children = 0;

        adults = numberOfTicketType(order, Type.ADULT);
        children = numberOfTicketType(order, Type.CHILD);

        int price =  adults * adultPrice + children * childPrice;
        System.out.println("Price: " + price); //should be logged or removed
        order.setPrice(price);
        return price;
    }

    private int numberOfTicketType(TicketOrder order, TicketTypeRequest.Type type) {
        int count = 0;
        TicketTypeRequest ttr;
        Iterator<TicketTypeRequest> it = order.getTickets().iterator();
        while(it.hasNext()){
            ttr = it.next();
             if (ttr.getTicketType() == type) count += ttr.getNoOfTickets();
        }
        return count;
    }

    private boolean validateTicketOrder(TicketOrder order) throws InvalidPurchaseException{
        int adults = 0;
        int children = 0;
        int infants = 0;

        adults = numberOfTicketType(order, Type.ADULT);
        children = numberOfTicketType(order, Type.CHILD);
        infants = numberOfTicketType(order, Type.INFANT);

        // 20 or less tickets max allowed
        if (adults + children + infants > 20) throw new InvalidPurchaseException(InvalidPurchaseException.tooManyTickets);

        //at least one adult if order contains child or infant tickets
        if((children > 0 || infants > 0) && adults == 0) throw new InvalidPurchaseException(InvalidPurchaseException.notAtLeastOneAdult);

        // at least one adult for each infant. (They are sharing a seat).
        if(adults < infants) throw new InvalidPurchaseException(InvalidPurchaseException.notEnoughAdults);

        //account Id must be > 1.
        if(order.getAccountId() < 1) throw new InvalidPurchaseException(InvalidPurchaseException.invalidAccountId);

        //returns true if no validation failures
        return true;
    }
}
