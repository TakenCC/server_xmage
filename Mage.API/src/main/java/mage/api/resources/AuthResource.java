package mage.api.resources;

import mage.MageException;
import mage.api.dto.ErrorResponse;
import mage.api.dto.LoginRequest;
import mage.api.dto.LoginResponse;
import mage.api.dto.RegisterRequest;
import mage.api.service.AuthenticationService;
import mage.server.AuthorizedUser;
import mage.server.AuthorizedUserRepository;
import mage.server.MageServerImpl;
import mage.server.User;
import mage.server.managers.ConfigSettings;
import mage.server.managers.ManagerFactory;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (!managerFactory.configSettings().isAuthenticationActivated()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("REGISTRATION_DISABLED", "Authentication is not activated on this server"))
                    .build();
        }

        try {
            String username = request.getUsername();
            String password = request.getPassword();
            String email = request.getEmail();

            // Validate username
            String validationError = validateUsername(username);
            if (validationError != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_ERROR", validationError))
                        .build();
            }

            // Validate password
            validationError = validatePassword(password, username);
            if (validationError != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_ERROR", validationError))
                        .build();
            }

            // Validate email
            validationError = validateEmail(email);
            if (validationError != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_ERROR", validationError))
                        .build();
            }

            // Create user directly (bypass email sending requirement)
            synchronized (AuthorizedUserRepository.getInstance()) {
                AuthorizedUser existingUser = AuthorizedUserRepository.getInstance().getByName(username);
                if (existingUser != null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse("USER_EXISTS", "Username already in use"))
                            .build();
                }

                AuthorizedUserRepository.getInstance().add(username, password, email);
                logger.info("User registered via REST API: " + username);
            }

            // Auto-login after registration
            AuthenticationService.AuthenticationResult loginResult = authenticationService.authenticate(
                username, 
                password
            );
            
            if (loginResult.isSuccess()) {
                LoginResponse response = new LoginResponse(
                    loginResult.getToken(),
                    username,
                    loginResult.getSessionId()
                );
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.CREATED)
                        .entity(new ErrorResponse("REGISTERED", "User registered but login failed: " + loginResult.getErrorMessage()))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Registration failed: " + e.getMessage()))
                    .build();
        }
    }

    private String validateUsername(String username) {
        if (username.equals(User.ADMIN_NAME)) {
            return "User name already in use";
        }

        ConfigSettings config = managerFactory.configSettings();
        if (username.length() < config.getMinUserNameLength()) {
            return "User name may not be shorter than " + config.getMinUserNameLength() + " characters";
        }
        if (username.length() > config.getMaxUserNameLength()) {
            return "User name may not be longer than " + config.getMaxUserNameLength() + " characters";
        }

        Pattern invalidUserNamePattern = Pattern.compile(config.getInvalidUserNamePattern(), Pattern.CASE_INSENSITIVE);
        Matcher m = invalidUserNamePattern.matcher(username);
        if (m.find()) {
            return "User name '" + username + "' includes not allowed characters: use a-z, A-Z and 0-9";
        }

        return null;
    }

    private String validatePassword(String password, String username) {
        ConfigSettings config = managerFactory.configSettings();
        if (password.length() < config.getMinPasswordLength()) {
            return "Password may not be shorter than " + config.getMinPasswordLength() + " characters";
        }
        if (password.length() > config.getMaxPasswordLength()) {
            return "Password may not be longer than " + config.getMaxPasswordLength() + " characters";
        }
        if (password.equals(username)) {
            return "Password may not be the same as your username";
        }
        
        Pattern alphabetsPattern = Pattern.compile(".*[a-zA-Z].*");
        Pattern digitsPattern = Pattern.compile(".*[0-9].*");
        Matcher alphabetsMatcher = alphabetsPattern.matcher(password);
        Matcher digitsMatcher = digitsPattern.matcher(password);
        if (!alphabetsMatcher.find() || !digitsMatcher.find()) {
            return "Password has to include at least one alphabet (a-zA-Z) and also at least one digit (0-9)";
        }
        return null;
    }

    private String validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Email address cannot be blank";
        }
        
        AuthorizedUser existingUser = AuthorizedUserRepository.getInstance().getByEmail(email);
        if (existingUser != null) {
            return "Email address '" + email + "' is associated with another user";
        }

        // Basic email format validation
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.matches()) {
            return "Invalid email address format";
        }

        return null;
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

