import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class AccessControlResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", "http://localhost:4200");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.add("Access-Control-Allow-Credentials", "true");

        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
        if (requestedHeaders != null) {
            headers.add("Access-Control-Allow-Headers", requestedHeaders);
        } else {
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }

    }
}
