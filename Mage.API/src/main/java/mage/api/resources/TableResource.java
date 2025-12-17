package mage.api.resources;

import mage.MageException;
import mage.api.config.JwtAuthFilter;
import mage.api.dto.ErrorResponse;
import mage.api.dto.JoinTableRequest;
import mage.cards.decks.DeckCardLists;
import mage.constants.TableState;
import mage.interfaces.MageServer;
import mage.server.MageServerImpl;
import mage.view.TableView;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@Path("/api/tables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TableResource {

    private static final Logger logger = Logger.getLogger(TableResource.class);

    @Inject
    private MageServerImpl mageServer;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/{tableId}")
    public Response getTable(@PathParam("tableId") String tableIdStr,
                            @QueryParam("roomId") String roomIdStr) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            
            if (roomId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId query parameter is required"))
                        .build();
            }

            TableView table = mageServer.roomGetTableById(roomId, tableId);
            
            if (table != null) {
                return Response.ok().entity(table).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("NOT_FOUND", "Table not found"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error getting table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to get table: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{tableId}/join")
    public Response joinTable(@PathParam("tableId") String tableIdStr,
                             @QueryParam("roomId") String roomIdStr,
                             JoinTableRequest request,
                             @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (roomId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId query parameter is required"))
                        .build();
            }

            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Join request is required"))
                        .build();
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Name is required"))
                        .build();
            }

            if (request.getPlayerType() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "playerType is required. Valid values: HUMAN, COMPUTER_MAD, COMPUTER_MONTE_CARLO, COMPUTER_DRAFT_BOT"))
                        .build();
            }

            // Intentar obtener información de la mesa para validaciones previas
            TableView tableInfo = null;
            try {
                tableInfo = mageServer.roomGetTableById(roomId, tableId);
                if (tableInfo == null) {
                    logger.warn("Table not found: tableId=" + tableId + ", roomId=" + roomId);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorResponse("TABLE_NOT_FOUND", "Table not found"))
                            .build();
                }
                
                // Validar estado de la mesa
                TableState tableState = tableInfo.getTableState();
                if (tableState != TableState.WAITING) {
                    logger.warn("Table is not in WAITING state: state=" + tableState + ", tableId=" + tableId);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse("TABLE_NOT_JOINABLE", "Table is not accepting new players. Current state: " + tableState))
                            .build();
                }
                
                // Contar asientos ocupados
                long occupiedSeats = tableInfo.getSeats().stream()
                    .filter(seat -> seat.getPlayerName() != null && !seat.getPlayerName().isEmpty())
                    .count();
                
                logger.info("Table info: state=" + tableInfo.getTableState() + ", totalSeats=" + tableInfo.getSeats().size() + ", occupiedSeats=" + occupiedSeats + ", gameType=" + tableInfo.getGameType() + ", deckType=" + tableInfo.getDeckType());
                
                // Validar que deckList no sea null para mesas no-limited
                if (request.getDeckList() == null && !tableInfo.isLimited()) {
                    String deckType = tableInfo.getDeckType();
                    logger.warn("Deck required for non-limited table: deckType=" + deckType);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse("DECK_REQUIRED", "Deck is required for this table. Deck type: " + deckType))
                            .build();
                }
            } catch (MageException e) {
                logger.error("Error retrieving table info", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to retrieve table information: " + e.getMessage()))
                        .build();
            } catch (Exception e) {
                logger.warn("Could not retrieve table info before join attempt", e);
            }

            logger.info("Attempting to join table: tableId=" + tableId + ", roomId=" + roomId + ", name=" + request.getName() + ", playerType=" + request.getPlayerType() + ", deckList=" + (request.getDeckList() != null ? "provided" : "null"));

            // Intentar cargar el deck antes de unirse para capturar errores de formato
            if (request.getDeckList() != null) {
                try {
                    mage.cards.decks.Deck testDeck = mage.cards.decks.Deck.load(request.getDeckList(), true, true);
                    logger.debug("Deck loaded successfully, card count: " + (testDeck != null ? testDeck.getCards().size() : 0));
                } catch (Exception e) {
                    logger.error("Error loading deck before join attempt", e);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse("DECK_LOAD_ERROR", "Failed to load deck: " + e.getMessage()))
                            .build();
                }
            }

            boolean success = mageServer.roomJoinTable(
                sessionId,
                roomId,
                tableId,
                request.getName(),
                request.getPlayerType(),
                request.getSkill(),
                request.getDeckList(),
                request.getPassword() != null ? request.getPassword() : ""
            );

            if (success) {
                logger.info("Successfully joined table: tableId=" + tableId + ", name=" + request.getName());
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Successfully joined table")).build();
            } else {
                // Intentar obtener más información sobre el error
                String errorDetails = "Failed to join table.";
                if (tableInfo != null) {
                    errorDetails += " Table state: " + tableInfo.getState() + ", Seats: " + tableInfo.getSeats().size();
                }
                logger.warn("Failed to join table: tableId=" + tableId + ", roomId=" + roomId + ", name=" + request.getName() + ", playerType=" + request.getPlayerType() + ". " + errorDetails);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("JOIN_FAILED", errorDetails + " Possible reasons: table is full, table already started, invalid deck format, deck validation failed, wrong password, quit ratio too high, or rating too low. Check server logs for more details."))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in join table", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format or parameter: " + e.getMessage()))
                    .build();
        } catch (MageException e) {
            logger.error("Error joining table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to join table: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error joining table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Unexpected error: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{tableId}/watch")
    public Response watchTable(@PathParam("tableId") String tableIdStr,
                              @QueryParam("roomId") String roomIdStr,
                              @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (roomId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId query parameter is required"))
                        .build();
            }

            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            boolean success = mageServer.roomWatchTable(sessionId, roomId, tableId);

            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Successfully watching table")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("WATCH_FAILED", "Failed to watch table"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error watching table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to watch table: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{tableId}")
    public Response removeTable(@PathParam("tableId") String tableIdStr,
                               @QueryParam("roomId") String roomIdStr,
                               @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (roomId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId query parameter is required"))
                        .build();
            }

            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            mageServer.tableRemove(sessionId, roomId, tableId);
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Table removed successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error removing table", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to remove table: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{tableId}/deck")
    public Response submitDeck(@PathParam("tableId") String tableIdStr,
                              DeckCardLists deckList,
                              @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (deckList == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Deck list is required"))
                        .build();
            }

            boolean success = mageServer.deckSubmit(sessionId, tableId, deckList);

            if (success) {
                return Response.ok().entity(new ErrorResponse("SUCCESS", "Deck submitted successfully")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("SUBMIT_FAILED", "Failed to submit deck"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error submitting deck", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to submit deck: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{tableId}/deck")
    public Response saveDeck(@PathParam("tableId") String tableIdStr,
                            DeckCardLists deckList,
                            @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            if (deckList == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "Deck list is required"))
                        .build();
            }

            mageServer.deckSave(sessionId, tableId, deckList);
            return Response.ok().entity(new ErrorResponse("SUCCESS", "Deck saved successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error saving deck", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to save deck: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{tableId}/is-owner")
    public Response isOwner(@PathParam("tableId") String tableIdStr,
                           @QueryParam("roomId") String roomIdStr,
                           @Context javax.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            UUID tableId = UUID.fromString(tableIdStr);
            UUID roomId = roomIdStr != null ? UUID.fromString(roomIdStr) : null;
            String sessionId = JwtAuthFilter.getSessionId(requestContext);
            
            if (roomId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("BAD_REQUEST", "roomId query parameter is required"))
                        .build();
            }

            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No session found"))
                        .build();
            }

            boolean isOwner = mageServer.tableIsOwner(sessionId, roomId, tableId);
            return Response.ok().entity(isOwner).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("BAD_REQUEST", "Invalid UUID format"))
                    .build();
        } catch (MageException e) {
            logger.error("Error checking table ownership", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to check ownership: " + e.getMessage()))
                    .build();
        }
    }
}

