package ss.finance.rest;

import ss.finance.entities.Report;
import ss.finance.services.ReportBean;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportApi {

    @Inject
    private ReportBean reportBean;

    private static final Logger logger = Logger.getLogger(ReportApi.class.getName());

    @GET
    @Path("/{reportId}")
    public Response getReportById(@PathParam("reportId") String reportId) {
        try {
            logger.info("Fetching report with ID: " + reportId);
            Report report = reportBean.getReportById(reportId);
            if (report == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Report not found\"}")
                        .build();
            }
            return Response.ok(report).build();
        } catch (Exception e) {
            logger.severe("Error fetching report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @GET
    @Path("/custom")
    public Response generateCustomReport(@QueryParam("userId") String userId,
                                          @QueryParam("startDate") String startDateStr,
                                          @QueryParam("endDate") String endDateStr) {
        try {
            logger.info("Generating custom report for user: " + userId);

            if (userId == null || startDateStr == null || endDateStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Both 'userId', 'startDate', and 'endDate' are required\"}")
                        .build();
            }

            Date startDate = reportBean.parseDate(startDateStr);
            Date endDate = reportBean.parseDate(endDateStr);

            if (startDate == null || endDate == null || startDate.after(endDate)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid date range\"}")
                        .build();
            }

            Report report = reportBean.generateCustomReport(userId, startDate, endDate);
            return Response.ok(report).build();
        } catch (Exception e) {
            logger.severe("Error generating custom report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @POST
    public Response createReport(Report report) {
        try {
            logger.info("Creating a new report for user: " + report.getUserId());
            Report createdReport = reportBean.createReport(report);
            return Response.status(Response.Status.CREATED).entity(createdReport).build();
        } catch (Exception e) {
            logger.severe("Error creating report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{reportId}")
    public Response deleteReport(@PathParam("reportId") String reportId) {
        try {
            logger.info("Deleting report with ID: " + reportId);
            boolean deleted = reportBean.deleteReport(reportId);
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Report not found\"}")
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }
}