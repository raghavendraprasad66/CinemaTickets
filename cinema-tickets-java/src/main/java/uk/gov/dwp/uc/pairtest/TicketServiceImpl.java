package uk.gov.dwp.uc.pairtest;

import java.util.Map;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

	private final TicketPaymentService paymentService;
	private final SeatReservationService reservationService;

	public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
		this.paymentService = paymentService;
		this.reservationService = reservationService;
	}

	private static final int MAX_TICKETS_PER_PURCHASE = 20;
	private static final Map<TicketTypeRequest.Type, Integer> TICKET_PRICES = Map.of(
            TicketTypeRequest.Type.INFANT, 0,
            TicketTypeRequest.Type.CHILD, 10,
            TicketTypeRequest.Type.ADULT, 20
    );

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {
		
		var hasAdultTicket = false;
		var hasChildOrInfantTicket = false;
		var totalPrice = 0;
		var numSeats = 0;
		
		if (!(accountId > 0)) {
			throw new InvalidPurchaseException("Invalid AccountId. An AccountId should be grater than zero");
		}

		if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
			throw new InvalidPurchaseException("At least one ticket type request is required");
		}

		// Calculate the total number of tickets requested, total price of the tickets and validate the purchase
		for (var ticket : ticketTypeRequests) {
			var numTickets = ticket.getNoOfTickets();
			var type = ticket.getTicketType();

			if (numSeats + numTickets > MAX_TICKETS_PER_PURCHASE) {
				throw new InvalidPurchaseException("Maximum " + MAX_TICKETS_PER_PURCHASE + " tickets can be purchased at a time");
			}

			totalPrice += numTickets * TICKET_PRICES.getOrDefault(type, 0);
			numSeats += switch (type) {
			  case ADULT -> {
				  hasAdultTicket = true;
				  yield numTickets;
			  }
			  case CHILD -> {
				  hasChildOrInfantTicket = true;
				  yield numTickets;
			  }
			  case INFANT -> {
				  hasChildOrInfantTicket = true;
				  yield 0;
			  }
			};
		}

		//allow child or infant tickets only with adult ticket's
		if (hasChildOrInfantTicket && !hasAdultTicket) {
			throw new InvalidPurchaseException("Child or infant tickets cannot be purchased without an adult ticket");
		}

		// Make payment to the payment service
		paymentService.makePayment(accountId, totalPrice);
		// Reserve seats using the seat reservation service
		reservationService.reserveSeat(accountId, numSeats);
	}

}
