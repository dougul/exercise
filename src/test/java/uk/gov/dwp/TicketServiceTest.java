package uk.gov.dwp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

    @InjectMocks
    TicketService ts = new TicketServiceImpl();

    @Mock
    TicketPaymentService tps;

    @Mock
    SeatReservationService srs;

    @Rule 
    public ExpectedException exceptionRule = ExpectedException.none();

    private TicketTypeRequest createTicketTypeRequest(int numberOfTickets, TicketTypeRequest.Type type){
        return new TicketTypeRequest(type, numberOfTickets);
    }
    
    @Test     //purchaseTickets - 1 adult - pass - 1 seat
    public void oneAdultTicketOnly() {
        //setup (Given)
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        long accountId = 1001;
        int price = 20;

        //make the call (When)
        ts.purchaseTickets(accountId, adultTickets);
        
        //results (Then)
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, 1); //SeatReservationService
    }

    @Test //purchase - 1 child - fail
    public void oneChild() {
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        long accountId = 1001;
        int price = 10;
        
        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notAtLeastOneAdult);

        ts.purchaseTickets(accountId, childTickets);
        
        verify(tps, never()).makePayment( any(),  any()); //paymentService
        verify(srs, never()).reserveSeat( any(),  any()); //SeatReservationService
    }

    @Test // 1 infant. - fail
    public void oneInfant() {
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 0;
        
        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notAtLeastOneAdult);

        ts.purchaseTickets(accountId, infantTickets);
        
        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, 1); //SeatReservationService
    }

    @Test // 1 child and 1 infant. - fail
    public void oneChildAndOneInfant() {
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
         TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        long accountId = 1001;
        int price = 0;
        
        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notAtLeastOneAdult);

        ts.purchaseTickets(accountId, childTickets, infantTickets);
        
        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, 1); //SeatReservationService
    }

    @Test // invalid account id. - fail
    public void invalidAccountId() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        long accountId = 0;
        int price = 20;
        
        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.invalidAccountId);

        ts.purchaseTickets(accountId, adultTickets);
        
        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, 1); //SeatReservationService
    }

    @Test // 1 adult , 1 child - pass 2 seats
    public void oneAdultOneChild() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        long accountId = 1001;
        int price = 30;
        int seats = 2;

        ts.purchaseTickets(accountId, adultTickets, childTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 1 adult , 1 infant - 1 seat
    public void oneAdultOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 20;
        int seats = 1;

        ts.purchaseTickets(accountId, adultTickets, infantTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 1 adult, 1 child, 1 infant -  2 seat seats
    public void oneAdultOneChildOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 30;
        int seats = 2;

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 1 adult, 2 infants - fail
    public void oneAdultTwoInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        TicketTypeRequest infantTickets = createTicketTypeRequest(2, Type.INFANT);
        long accountId = 1001;
        int price = 20;
        int seats = 1;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notEnoughAdults);

        ts.purchaseTickets(accountId, adultTickets, infantTickets);
        
        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 2 adults, 2 infants - pass 2 infants
    public void twoAdultsTwoInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(2, Type.ADULT);
        TicketTypeRequest infantTickets = createTicketTypeRequest(2, Type.INFANT);
        long accountId = 1001;
        int price = 40;
        int seats = 2;

        ts.purchaseTickets(accountId, adultTickets, infantTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 2 adults, 3 infants - fail
    public void twoAdultsThreeInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(2, Type.ADULT);
        TicketTypeRequest infantTickets = createTicketTypeRequest(3, Type.INFANT);
        long accountId = 1001;
        int price = 40;
        int seats = 2;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notEnoughAdults);

        ts.purchaseTickets(accountId, adultTickets, infantTickets);

        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 1 adult, 2 children - pass 3 seats
    public void oneAdultTwoChildrenThreeInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(1, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(3, Type.INFANT);
        long accountId = 1001;
        int price = 40;
        int seats = 3;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notEnoughAdults);

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);

        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 2 adults, 2 children, 1 infant - 4 seats
    public void twoAdultsTwoChildrenOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(2, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(2, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 60;
        int seats = 4;

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);

        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 2 adults, 2 children, 2 infant - 4 seats
    public void twoAdultsTwoChildrenTwoInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(2, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(2, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(2, Type.INFANT);
        long accountId = 1001;
        int price = 60;
        int seats = 4;

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);

        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 2 adults, 1 child, 3 infants - fail
    public void twoAdultOneChildrenThreeInfants() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(2, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(3, Type.INFANT);
        long accountId = 1001;
        int price = 50;
        int seats = 3;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.notEnoughAdults);

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);

        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, seats); //SeatReservationService
    }
    
    @Test // 20 adults - 20 seats - price 400
    public void twentyAdults() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(20, Type.ADULT);
        long accountId = 1001;
        int price = 400;
        int seats = 20;

        ts.purchaseTickets(accountId, adultTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 21 adults - 21 seats - fail
    public void twentyOneAdults() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(21, Type.ADULT);
        long accountId = 1001;
        int price = 420;
        int seats = 21;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.tooManyTickets);

        ts.purchaseTickets(accountId, adultTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    } 

    @Test // 20 Adults 1 Child - 21 tickets and 21 seats - fail
    public void twentyAdultsOneChild() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(20, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        long accountId = 1001;
        int price = 410;
        int seats = 21;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.tooManyTickets);

        ts.purchaseTickets(accountId, adultTickets, childTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }

    @Test // 20 Adults 1 infant - 21 tickets and 20 seats - fail
    public void twentyAdultsOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(20, Type.ADULT);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 400;
        int seats = 20;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.tooManyTickets);

        ts.purchaseTickets(accountId, adultTickets, infantTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }  

    
    @Test // 19 Adults, 1 Child, 1 infant - 21 tickets and 20 seats - fail
    public void nineteenAdultsOneChildOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(19, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 390;
        int seats = 20;

        exceptionRule.expect(InvalidPurchaseException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.tooManyTickets);

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);
        
        verify(tps, never()).makePayment(accountId, price); //paymentService
        verify(srs, never()).reserveSeat(accountId, seats); //SeatReservationService
    } 

    @Test // 18 Adults, 1 Child, 1 infant - 20 tickets and 19 seats
    public void eighteenAdultsOneChildOneInfant() {
        TicketTypeRequest adultTickets = createTicketTypeRequest(18, Type.ADULT);
        TicketTypeRequest childTickets = createTicketTypeRequest(1, Type.CHILD);
        TicketTypeRequest infantTickets = createTicketTypeRequest(1, Type.INFANT);
        long accountId = 1001;
        int price = 370;
        int seats = 19;

        ts.purchaseTickets(accountId, adultTickets, childTickets, infantTickets);
        
        verify(tps, times(1)).makePayment(accountId, price); //paymentService
        verify(srs, times(1)).reserveSeat(accountId, seats); //SeatReservationService
    }
}
