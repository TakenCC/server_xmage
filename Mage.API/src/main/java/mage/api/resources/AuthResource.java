package mage.api.resources;

import mage.MageException;
import mage.api.dto.ErrorResponse;
import mage.api.dto.LoginRequest;
import mage.api.dto.LoginResponse;
import mage.api.dto.RegisterRequest;
import mage.api.service.AuthenticationService;
import mage.server.MageServerImpl;
import mage.server.managers.ManagerFactory;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger logger = Logger.getLogger(AuthResource.class);
    
    @Inject
    private AuthenticationService authenticationService;
    
    @Inject
    private MageServerImpl mageServer;
    
    @Inject
    private ManagerFactory managerFactory;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Username and password are required"))
                    .build();
        }

        AuthenticationService.AuthenticationResult result = authenticationService.authenticate(
            request.getUsername(), 
            request.getPassword()
        );

        if (result.isSuccess()) {
            LoginResponse response = new LoginResponse(
                result.getToken(),
                request.getUsername(),
                result.getSessionId()
            );
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("UNAUTHORIZED", result.getErrorMessage()))
                    .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        if (request == null || request.getUsername() == null || 
            request.getPassword() == null || request.getEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Username, password and email are required"))
                    .build();
        }

        try {
            String sessionId = UUID.randomUUID().toString();
            mage.api.util.RestCallbackHandler restCallback = new mage.api.util.RestCallbackHandler();
            managerFactory.sessionManager().createSession(sessionId, restCallback);
            
            boolean success = mageServer.authRegister(sessionId, request.getUsername(), 
                request.getPassword(), request.getEmail());
            
            if (success) {
                // Auto-login after registration
                AuthenticationService.AuthenticationResult loginResult = authenticationService.authenticate(
                    request.getUsername(), 
                    request.getPassword()
                );
                
                if (loginResult.isSuccess()) {
                    LoginResponse response = new LoginResponse(
                        loginResult.getToken(),
                        request.getUsername(),
                        loginResult.getSessionId()
                    );
                    return Response.ok(response).build();
                } else {
                    return Response.status(Response.Status.CREATED)
                            .entity(new ErrorResponse("REGISTERED", "User registered but login failed: " + loginResult.getErrorMessage()))
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("REGISTRATION_FAILED", "User registration failed"))
                        .build();
            }
        } catch (MageException e) {
            logger.error("Error during registration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Registration failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@FormParam("email") String email, 
                                  @FormParam("authToken") String authToken,
                                  @FormParam("newPassword") String newPassword) {
        if (email == null || authToken == null || newPassword == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Email, auth token and new password are required"))
                    .build();
        }

        try {
            String sessionId = UUID.randomUUID().toString();
            mage.api.util.RestCallbackHandler restCallback = new mage.api.util.RestCallbackHandler();
            managerFactory.sessionManager().createSession(sessionId, restCallback);
            
            boolean success = mageServer.authResetPassword(sessionId, email, authToken, newPassword);
            
            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Password reset successfully")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("RESET_FAILED", "Password reset failed"))
                        .build();
            }
        } catch (MageException e) {
            logger.error("Error during password reset", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Password reset failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/send-reset-token")
    public Response sendResetToken(@FormParam("email") String email) {
        if (email == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Email is required"))
                    .build();
        }

        try {
            String sessionId = UUID.randomUUID().toString();
            mage.api.util.RestCallbackHandler restCallback = new mage.api.util.RestCallbackHandler();
            managerFactory.sessionManager().createSession(sessionId, restCallback);
            
            boolean success = mageServer.authSendTokenToEmail(sessionId, email);
            
            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Reset token sent to email")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("SEND_FAILED", "Failed to send reset token"))
                        .build();
            }
        } catch (MageException e) {
            logger.error("Error sending reset token", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to send reset token: " + e.getMessage()))
                    .build();
        }
    }
}

