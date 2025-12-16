package mage.api.resources;

import mage.MageException;
import mage.api.config.JwtAuthFilter;
import mage.api.dto.ErrorResponse;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.MageServer;
import mage.server.MageServerImpl;
import mage.server.Session;
import mage.view.MatchView;
import mage.view.RoomUsersView;
import mage.view.TableView;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Path("/api/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger logger = Logger.getLogger(RoomResource.class);

    @Inject
    private MageServerImpl mageServer;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/main")
    public Response getMainRoomId() {
        try {
            UUID mainRoomId = mageServer.serverGetMainRoomId();
            return Response.ok().entity(mainRoomId.toString()).build();
        } catch (MageException e) {
            logger.error("Error getting main room ID", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get main room ID: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{roomId}/users")
    public Response getRoomUsers(@PathParam("roomId") String roomIdStr) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            List<RoomUsersView> users = mageServer.roomGetUsers(roomId);
            return Response.ok().entity(users).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid room ID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error getting room users", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get room users: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{roomId}/finished-matches")
    public Response getFinishedMatches(@PathParam("roomId") String roomIdStr) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            List<MatchView> matches = mageServer.roomGetFinishedMatches(roomId);
            return Response.ok().entity(matches).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid room ID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error getting finished matches", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get finished matches: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{roomId}/tables")
    public Response createTable(@PathParam("roomId") String roomIdStr, 
                                MatchOptions matchOptions,
                                @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (matchOptions == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Match options are required"))
                        .build();
            }

            TableView table = mageServer.roomCreateTable(sessionId, roomId, matchOptions);
            
            if (table != null) {
                return Response.status(Response.Status.CREATED).entity(table).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("CREATE_FAILED", "Failed to create table"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid room ID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error creating table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to create table: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{roomId}/tournaments")
    public Response createTournament(@PathParam("roomId") String roomIdStr,
                                     TournamentOptions tournamentOptions,
                                     @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (tournamentOptions == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Tournament options are required"))
                        .build();
            }

            TableView table = mageServer.roomCreateTournament(sessionId, roomId, tournamentOptions);
            
            if (table != null) {
                return Response.status(Response.Status.CREATED).entity(table).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("CREATE_FAILED", "Failed to create tournament"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid room ID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error creating tournament", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to create tournament: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{roomId}/tables")
    public Response getAllTables(@PathParam("roomId") String roomIdStr) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            List<TableView> tables = mageServer.roomGetAllTables(roomId);
            return Response.ok().entity(tables).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid room ID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error getting tables", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get tables: " + e.getMessage()))
                    .build();
        }
    }
}

