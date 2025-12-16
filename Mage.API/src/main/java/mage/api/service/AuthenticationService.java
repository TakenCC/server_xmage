package mage.api.service;

import mage.MageException;
import mage.server.AuthorizedUser;
import mage.server.AuthorizedUserRepository;
import mage.server.Session;
import mage.server.managers.ManagerFactory;
import mage.utils.MageVersion;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationService.class);
    private final ManagerFactory managerFactory;
    private final JwtService jwtService;

    public AuthenticationService(ManagerFactory managerFactory, JwtService jwtService) {
        this.managerFactory = managerFactory;
        this.jwtService = jwtService;
    }

    public AuthenticationResult authenticate(String username, String password) {
        try {
            if (!managerFactory.configSettings().isAuthenticationActivated()) {
                return new AuthenticationResult(false, "Authentication is not activated on this server", null, null);
            }

            AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByName(username);
            if (authorizedUser == null) {
                logger.debug("Authentication failed: user not found - " + username);
                return new AuthenticationResult(false, "Invalid username or password", null, null);
            }

            if (!authorizedUser.doCredentialsMatch(username, password)) {
                logger.debug("Authentication failed: invalid password - " + username);
                return new AuthenticationResult(false, "Invalid username or password", null, null);
            }

            // Check if user is active
            if (!authorizedUser.isActive()) {
                return new AuthenticationResult(false, "User account is disabled", null, null);
            }

            // Check if user is locked
            if (authorizedUser.getLockedUntil() != null && authorizedUser.getLockedUntil().after(new java.util.Date())) {
                return new AuthenticationResult(false, "User account is locked", null, null);
            }

            // Create or get session
            String sessionId = UUID.randomUUID().toString();
            
            // Create session in SessionManager
            try {
                // For REST API, we use a dummy callback handler
                // The session will be properly initialized when the user connects
                mage.api.util.RestCallbackHandler restCallback = new mage.api.util.RestCallbackHandler();
                managerFactory.sessionManager().createSession(sessionId, restCallback);
                
                // Connect user to session
                String userInfo = "REST API client";
                boolean connected = managerFactory.sessionManager().connectUser(
                    sessionId, 
                    "", 
                    username, 
                    password, 
                    userInfo, 
                    false
                );

                if (!connected) {
                    return new AuthenticationResult(false, "Failed to connect user to session", null, null);
                }

                // Generate JWT token
                String token = jwtService.generateToken(username, sessionId);

                logger.info("User authenticated via REST API: " + username);
                return new AuthenticationResult(true, null, token, sessionId);
            } catch (MageException e) {
                logger.error("Error creating session for user: " + username, e);
                return new AuthenticationResult(false, "Failed to create session: " + e.getMessage(), null, null);
            }
        } catch (Exception e) {
            logger.error("Error during authentication", e);
            return new AuthenticationResult(false, "Authentication error: " + e.getMessage(), null, null);
        }
    }

    public boolean register(String username, String password, String email) {
        try {
            String sessionId = UUID.randomUUID().toString();
            mage.api.util.RestCallbackHandler restCallback = new mage.api.util.RestCallbackHandler();
            managerFactory.sessionManager().createSession(sessionId, restCallback);
            return managerFactory.sessionManager().registerUser(sessionId, username, password, email);
        } catch (MageException e) {
            logger.error("Error registering user", e);
            return false;
        }
    }

    public Optional<Session> getSessionFromToken(String token) {
        if (!jwtService.isTokenValid(token)) {
            return Optional.empty();
        }

        String sessionId = jwtService.getSessionIdFromToken(token);
        if (sessionId == null) {
            return Optional.empty();
        }

        return managerFactory.sessionManager().getSession(sessionId);
    }

    public static class AuthenticationResult {
        private final boolean success;
        private final String errorMessage;
        private final String token;
        private final String sessionId;

        public AuthenticationResult(boolean success, String errorMessage, String token, String sessionId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.token = token;
            this.sessionId = sessionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getToken() {
            return token;
        }

        public String getSessionId() {
            return sessionId;
        }
    }
}

