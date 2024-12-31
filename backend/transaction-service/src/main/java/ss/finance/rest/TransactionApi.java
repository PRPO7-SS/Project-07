package ss.finance.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ss.finance.Transaction;
import ss.finance.TransactionDTO;
import ss.finance.TransactionZrno;
import ss.finance.security.JwtUtil;


@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionZrno transactionZrno;

    @Inject
    private JwtUtil jwtUtil;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTransaction(TransactionDTO transactionDTO, @Context HttpHeaders headers) {
        try {
            // Log prejem podatkov
            System.out.println("Received transactionDTO: " + transactionDTO);
    
            // Extract token
            String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Authorization header missing or invalid.");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Authorization header missing or invalid\"}")
                        .build();
            }
            token = token.substring(7);
            System.out.println("Extracted token: " + token);
    
            // Extract userId
            ObjectId userId = jwtUtil.extractUserId(token);
            System.out.println("Extracted userId: " + userId);
    
            // Create and add transaction
            Transaction transaction = new Transaction(userId, transactionDTO.getType(), transactionDTO.getAmount(), transactionDTO.getCategory());
            transactionZrno.addTransaction(transaction);
            System.out.println("Transaction added successfully: " + transaction);
    
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Transaction created successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error\"}")
                    .build();
        }
    }        

    @GET
    public Response getUserTransactions(@Context HttpHeaders headers) {
        //String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        Cookie authCookie = headers.getCookies().get("auth_token");
        if (authCookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Auth cookie missing\"}")
                    .build();
        }
        String token = authCookie.getValue();
        if (token == null || !token.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Authorization header missing or invalid\"}")
                    .build();
        }
        // Remove "Bearer " prefix from token
        token = token.substring(7);
        ObjectId userId = jwtUtil.extractUserId(token);

        List<TransactionDTO> transactions = transactionZrno.getAllTransactions(userId);

        return Response.ok(transactions).build();
    }
}
