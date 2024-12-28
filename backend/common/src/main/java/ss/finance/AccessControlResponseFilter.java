import javax.annotation.Priority; // For @Priority annotation
import javax.ws.rs.Priorities; // For Priorities.HEADER_DECORATOR constant
import javax.ws.rs.container.ContainerRequestContext; // For the request context
import javax.ws.rs.container.ContainerResponseContext; // For the response context
import javax.ws.rs.container.ContainerResponseFilter; // For implementing response filters
import javax.ws.rs.ext.Provider; // For @Provider annotation
import javax.ws.rs.core.MultivaluedMap; // For response headers
import java.io.IOException; // For the filter method exception

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class AccessControlResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String,Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.add("Access-Control-Allow-Origin", "*");
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            headers.add("Access-Control-Allow-Headers", requestContext.getHeaderString("Access-Control-Request-Headers"));
        }
    }
}