package ss.finance.rest;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import ss.finance.entities.Investment;
import ss.finance.security.JwtUtil;
import ss.finance.services.InvestmentBean;


@Tag(name = "Investments", description = "Endpoints for managing investments")
@Path("/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentApi {

    @Inject
    private InvestmentBean investmentBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(InvestmentApi.class.getName());

    private static final String MONGO_URI = System.getenv("MONGO_URL");
    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");

    @GET
    @Path("/health")
    public Response healthCheck() {
        boolean isMongoUp = checkMongoDB();
        boolean isServiceUp = true;

        JsonObject healthJson = Json.createObjectBuilder()
                .add("status", isMongoUp ? "UP" : "DOWN")
                .add("details", Json.createObjectBuilder()
                        .add("MongoDB", isMongoUp ? "UP" : "DOWN")
                        .add("Investment Service", isServiceUp ? "UP" : "DOWN")
                        .build())
                .build();

        int statusCode = isMongoUp ? Response.Status.OK.getStatusCode() : Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
        return Response.status(statusCode).entity(healthJson.toString()).build();
    }

    private boolean checkMongoDB() {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            database.listCollectionNames().first();
            return true;
        } catch (Exception e) {
            logger.severe("MongoDB health check failed: " + e.getMessage());
            return false;
        }
    }

    @Operation(summary = "Get all investments",
            description = "Returns a list of all investments for the authenticated user")
    @APIResponse(
            responseCode = "200",
            description = "List of investments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"id\": \"<investment_id>\", \"type\": \"crypto\", \"name\": \"Bitcoin\", \"amount\": 500.0, \"quantity\": 0.01, \"purchaseDate\": \"2023-12-01\"}]"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @GET
    public Response getInvestments(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            List<Investment> investments = investmentBean.getAllInvestments(userId);
            return Response.ok(investments).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid token."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }

    @Operation(summary = "Get investment by ID",
            description = "Fetches details of a specific investment by ID")
    @APIResponse(
            responseCode = "200",
            description = "Investment details",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"id\": \"<investment_id>\", \"type\": \"crypto\", \"name\": \"Bitcoin\", \"amount\": 500.0, \"quantity\": 0.01, \"purchaseDate\": \"2023-12-01\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Investment not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Investment not found or access denied.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @GET
    @Path("/{id}")
    public Response getInvestmentById(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            Investment investment = investmentBean.getInvestment(investmentId);

            if (investment != null && investment.getUserId().equals(userId)) {
                return Response.ok(investment).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Investment not found or access denied."))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid investment ID format."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }

    @Operation(summary = "Create a new investment",
            description = "Adds a new investment for the authenticated user")
    @APIResponse(
            responseCode = "201",
            description = "Investment created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Investment created successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing body parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Body parameters 'type', 'name', 'amount', 'quantity', and 'purchaseDate' are required.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @POST
    public Response addInvestment(@CookieParam("auth_token") String token, @RequestBody(
            description = "Investment details including type, name, amount, quantity, and purchase date",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            example = "{ \"type\": \"stock\", \"name\": \"NVDA\", \"amount\": 1000.50, \"quantity\": 10, \"purchaseDate\": \"2025-01-01\" }"
                    )
            )
    )Investment investment) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            investment.setUserId(userId);

            if (investment.getType() == null || investment.getType().isEmpty() ||
                investment.getName() == null || investment.getName().isEmpty() ||
                investment.getAmount() == null || investment.getAmount() <= 0 ||
                investment.getQuantity() == null || investment.getQuantity() <= 0 ||
                investment.getPurchaseDate() == null) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Body parameters 'type', 'name', 'amount', 'quantity', and 'purchaseDate' are required."))
                        .build();
            }

            investmentBean.addInvestment(investment);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Investment created successfully")).build();
        } catch (IllegalArgumentException e) {
            logger.warning("Validation failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.severe("Unexpected error occurred: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error."))
                    .build();
        }
    }


    @Operation(summary = "Delete investment",
            description = "Deletes an investment by ID")
    @APIResponse(
            responseCode = "204",
            description = "Investment deleted successfully"
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid investment ID",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid investment ID format.\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Investment not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Investment not found.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @DELETE
    @Path("/{id}")
    public Response deleteInvestment(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            boolean deleted = investmentBean.deleteInvestment(investmentId);

            if (deleted) {
                return Response.noContent().build(); // 204 No Content
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Investment not found."))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid investment ID format."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }

        @Operation(summary = "Get last transaction",
        description = "Fetches the last transaction for the authenticated user")
        @APIResponse(
                responseCode = "200",
                description = "Last transaction details",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"userId\": \"<user_id>\", \"lastTransactionAmount\": 55.0, \"lastTransactionType\": \"income\", \"timestamp\": \"2025-01-11T12:00:00Z\"}"))
        )
        @APIResponse(
                responseCode = "401",
                description = "Unauthorized access",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
        )
        @APIResponse(
                responseCode = "404",
                description = "No transactions found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"No transactions found for the user.\"}"))
        )
        @APIResponse(
                responseCode = "500",
                description = "Server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
        )

        @GET
        @Path("/transactions/last")
        public Response getLastTransaction(@CookieParam("auth_token") String token) {
            try {
                if (token == null || token.isEmpty()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                            .build();
                }
        
                // Pridobitev userId kot String
                String userId = jwtUtil.extractUserId(token).toHexString();
        
                // Pridobitev zadnje transakcije
                Document lastTransaction = investmentBean.getLastTransaction(userId);
        
                if (lastTransaction != null) {
                    return Response.ok(lastTransaction).build(); // Vrne zadnjo transakcijo
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "No transactions found for the user."))
                            .build();
                }
            } catch (Exception e) {
                logger.severe("Unexpected error occurred while fetching last transaction: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Internal server error. Please try again later."))
                        .build();
            }
        }

        @Operation(summary = "Get all transactions",
        description = "Fetches all transactions for the authenticated user")
        @APIResponse(
                responseCode = "200",
                description = "List of transactions",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"userId\": \"<user_id>\", \"lastTransactionAmount\": 55.0, \"lastTransactionType\": \"income\", \"timestamp\": \"2025-01-11T12:00:00Z\"}]"))
        )
        @APIResponse(
                responseCode = "401",
                description = "Unauthorized access",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
        )
        @APIResponse(
                responseCode = "500",
                description = "Server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
        )
        
        @GET
        @Path("/transactions")
        public Response getAllTransactions(@CookieParam("auth_token") String token) {
            try {
                if (token == null || token.isEmpty()) {
                        return Response.status(Response.Status.UNAUTHORIZED)
                                .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                                .build();
                }
        
                String userId = jwtUtil.extractUserId(token).toHexString(); // Pretvorite ObjectId v String
                List<Document> transactions = investmentBean.getAllTransactions(userId); // Posredujte userId kot String
        
                if (transactions.isEmpty()) {
                        return Response.status(Response.Status.NO_CONTENT).build(); // 204, če ni transakcij
                }
        
                return Response.ok(transactions).build(); // Vrnemo seznam transakcij
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Invalid token."))
                        .build();
            } catch (Exception e) {
                logger.severe("Unexpected error occurred: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Internal server error. Please try again later."))
                        .build();
            }
        }

        @Operation(summary = "Delete all transactions",
        description = "Deletes all transactions for the authenticated user")
        @APIResponse(
                responseCode = "204",
                description = "All transactions deleted successfully"
        )
        @APIResponse(
                responseCode = "401",
                description = "Unauthorized access",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
        )
        @APIResponse(
                responseCode = "500",
                description = "Server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
        )
        @DELETE
        @Path("/transactions")
        public Response deleteAllTransactions(@CookieParam("auth_token") String token) {
        try {
                if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
                }

                // Pridobitev userId iz tokena
                String userId = jwtUtil.extractUserId(token).toHexString();

                // Klic metode za brisanje vseh transakcij
                boolean deleted = investmentBean.deleteAllTransactions(userId);

                if (deleted) {
                return Response.noContent().build(); // 204 No Content, če so transakcije uspešno izbrisane
                } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Failed to delete transactions."))
                        .build();
                }
        } catch (Exception e) {
                logger.severe("Unexpected error occurred while deleting transactions: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Internal server error. Please try again later."))
                        .build();
        }
        }

}
