package mage.api.config;

import mage.api.dto.ErrorResponse;
import mage.api.service.AuthenticationService;
import mage.api.service.JwtService;
import mage.server.Session;
import mage.server.managers.ManagerFactory;
import org.apache.log4j.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtAuthFilter.class);
    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final String SESSION_ATTRIBUTE = "session";
    private static final String USERNAME_ATTRIBUTE = "username";

    @Inject
    private JwtService jwtService;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private ManagerFactory managerFactory;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Allow access to authentication endpoints without JWT
        if (path.startsWith("api/auth")) {
            return;
        }

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        // Validate the token
        if (!jwtService.isTokenValid(token)) {
            logger.debug("Invalid JWT token");
            abortWithUnauthorized(requestContext);
            return;
        }

        // Get session from token
        Optional<Session> sessionOpt = authenticationService.getSessionFromToken(token);
        if (!sessionOpt.isPresent()) {
            logger.debug("Session not found for token");
            abortWithUnauthorized(requestContext);
            return;
        }

        Session session = sessionOpt.get();
        String username = jwtService.getUsernameFromToken(token);

        // Set session and username in request context for use in resources
        requestContext.setProperty(SESSION_ATTRIBUTE, session);
        requestContext.setProperty(USERNAME_ATTRIBUTE, username);
        requestContext.setProperty("sessionId", session.getId());
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "Invalid or missing JWT token"))
                        .build()
        );
    }

    public static Session getSession(ContainerRequestContext requestContext) {
        return (Session) requestContext.getProperty(SESSION_ATTRIBUTE);
    }

    public static String getUsername(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(USERNAME_ATTRIBUTE);
    }

    public static String getSessionId(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("sessionId");
    }
}

