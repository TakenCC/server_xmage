package mage.api.resources;

import mage.MageException;
import mage.api.config.JwtAuthFilter;
import mage.api.dto.ErrorResponse;
import mage.api.dto.GameActionRequest;
import mage.constants.ManaType;
import mage.constants.PlayerAction;
import mage.game.GameException;
import mage.server.MageServerImpl;
import mage.view.GameView;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    private static final Logger logger = Logger.getLogger(GameResource.class);

    @Inject
    private MageServerImpl mageServer;

    @GET
    @Path("/{gameId}")
    public Response getGame(@PathParam("gameId") String gameIdStr,
                           @QueryParam("playerId") String playerIdStr,
                           @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            UUID playerId = playerIdStr != null ? UUID.fromString(playerIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            GameView gameView = mageServer.gameGetView(gameId, sessionId, playerId);
            
            if (gameView != null) {
                return Response.ok().entity(gameView).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("NOT_FOUND", "Game not found"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error getting game", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get game: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{gameId}/join")
    public Response joinGame(@PathParam("gameId") String gameIdStr,
                            @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            mageServer.gameJoin(gameId, sessionId);
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Successfully joined game")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error joining game", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to join game: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{gameId}/watch")
    public Response watchGame(@PathParam("gameId") String gameIdStr,
                             @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            boolean success = mageServer.gameWatchStart(gameId, sessionId);
            
            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Successfully watching game")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("WATCH_FAILED", "Failed to watch game"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error watching game", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to watch game: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{gameId}/watch")
    public Response stopWatchingGame(@PathParam("gameId") String gameIdStr,
                                    @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            mageServer.gameWatchStop(gameId, sessionId);
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Stopped watching game")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error stopping to watch game", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to stop watching game: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{gameId}/actions")
    public Response sendAction(@PathParam("gameId") String gameIdStr,
                              GameActionRequest request,
                              @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (request == null || request.getPlayerAction() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Player action is required"))
                        .build();
            }

            mageServer.sendPlayerAction(
                request.getPlayerAction(),
                gameId,
                sessionId,
                request.getData()
            );
            
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Action sent successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error sending game action", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to send action: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{gameId}/quit")
    public Response quitGame(@PathParam("gameId") String gameIdStr,
                            @QueryParam("roomId") String roomIdStr,
                            @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID gameId = UUID.fromString(gameIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            mageServer.matchQuit(gameId, sessionId);
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Quit game successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error quitting game", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to quit game: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{gameId}/start")
    public Response startMatch(@PathParam("gameId") String gameIdStr,
                              @QueryParam("roomId") String roomIdStr,
                              @QueryParam("tableId") String tableIdStr,
                              @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            UUID tableId = tableIdStr != null ? UUID.fromString(tableIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (roomId == null || tableId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId and tableId are required"))
                        .build();
            }

            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            boolean success = mageServer.matchStart(sessionId, roomId, tableId);
            
            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Match started successfully")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("START_FAILED", "Failed to start match"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error starting match", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to start match: " + e.getMessage()))
                    .build();
        }
    }
}

