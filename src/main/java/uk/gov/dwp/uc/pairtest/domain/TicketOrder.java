package uk.gov.dwp.uc.pairtest.domain;

import java.util.LinkedList;
import java.util.List;

public class TicketOrder {

    /**
     *  Represents a customers order of multiple cinema tickets and types. Number of seats and the order price is calculated by the TicketService.
     */
    private List<TicketTypeRequest> tickets = new LinkedList<TicketTypeRequest>(); 
    private long accountId;
    private int numberOfSeats;
    private int price; 
  
    public TicketOrder(long accountId, List<TicketTypeRequest> tickets) {
        this.tickets = tickets;
        this.accountId = accountId;
    }

    public List<TicketTypeRequest> getTickets() {
        return this.tickets;
    }

    public long getAccountId() {
        return this.accountId;
    }

    public int getNumberOfSeats() {
        return this.numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
