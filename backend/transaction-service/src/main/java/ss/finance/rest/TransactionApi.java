package ss.finance.rest;

import ss.finance.TransactionZrno;
import ss.finance.TransactionDTO;
import ss.finance.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.inject.Inject;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionZrno transactionZrno;

    @POST
    public Response addTransaction(TransactionDTO transactionDTO) {
        // Map DTO to entity
        Transaction transaction = new Transaction(
                transactionDTO.getType(),
                transactionDTO.getAmount(),
                transactionDTO.getCategory()
        );
        // Save the transaction
        transactionZrno.addTransaction(transaction);

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"Transaction created successfully\"}")
                .build();
    }

    @GET
    public Response getTransactions() {
        // Retrieve all transactions
        return Response.ok(transactionZrno.getAllTransactions()).build();
    }
}
