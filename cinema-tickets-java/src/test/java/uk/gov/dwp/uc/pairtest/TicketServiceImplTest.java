/**
 * 
 */
package uk.gov.dwp.uc.pairtest;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * @author raghavendra.araveti
 *
 */
public class TicketServiceImplTest {
	
	
	private TicketPaymentService mockPaymentService;
    private SeatReservationService mockReservationService;
    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        mockPaymentService = mock(TicketPaymentService.class);
        mockReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(mockPaymentService, mockReservationService);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithNullTicketTypeRequests() throws InvalidPurchaseException {
    	TicketTypeRequest[] ticketTypeRequests = null;
    	
        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithEmptyTicketTypeRequests() throws InvalidPurchaseException {
    	TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{};
    	
        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithMoreThanMaxTicketsPerPurchase() throws InvalidPurchaseException {
    	TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketType, childTicketType, infantTicketType};
        
        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithChildAndInfantTicketWithoutAdultTicket() throws InvalidPurchaseException {
    	TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    	TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
    	TicketTypeRequest[] ticketTypeRequests = {childTicketType, infantTicketType};
        
    	ticketService.purchaseTickets(1L, ticketTypeRequests);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithChildTicketWithoutAdultTicket() throws InvalidPurchaseException {
    	TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    	TicketTypeRequest[] ticketTypeRequests = {childTicketType};
       
    	ticketService.purchaseTickets(1L, ticketTypeRequests);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithInfantTicketWithoutAdultTicket() throws InvalidPurchaseException {
    	TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
    	TicketTypeRequest[] ticketTypeRequests = {infantTicketType};
       
    	ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void testPurchaseTicketsWithValidAdultAndChildTicketTypeRequests() throws InvalidPurchaseException {
    	TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketType, childTicketType};
        
        ticketService.purchaseTickets(123L, ticketTypeRequests);
        
        verify(mockPaymentService).makePayment(123L, 50);
        verify(mockReservationService).reserveSeat(123L, 3);
    }
    
    @Test
    public void testPurchaseTicketsWithValidAdultAndInfantTicketTypeRequests() throws InvalidPurchaseException {
    	TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketType, infantTicketType};
        
        ticketService.purchaseTickets(123L, ticketTypeRequests);
        
        verify(mockPaymentService).makePayment(123L, 40);
        verify(mockReservationService).reserveSeat(123L, 2);
    }
    
    @Test
    public void testPurchaseTicketsWithValidAdultAndChildAndInfantTicketTypeRequests() throws InvalidPurchaseException {
    	TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
    	TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketType, childTicketType, infantTicketType};
        
        ticketService.purchaseTickets(123L, ticketTypeRequests);
        
        verify(mockPaymentService).makePayment(123L, 50);
        verify(mockReservationService).reserveSeat(123L, 3);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithInvalidAccountId() throws InvalidPurchaseException {
    	TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketType};
        
        ticketService.purchaseTickets(0L, ticketTypeRequests);
    }

}
