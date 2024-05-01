package today.tecktip.killbill.backend.routes.dms;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.backend.auth.Authenticator;
import today.tecktip.killbill.backend.db.chat.Dm;
import today.tecktip.killbill.backend.db.chat.Dms;
import today.tecktip.killbill.backend.db.chat.MessageState;
import today.tecktip.killbill.backend.db.friends.Friend;
import today.tecktip.killbill.backend.db.friends.FriendState;
import today.tecktip.killbill.backend.db.friends.Friends;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.UserRole;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.ServerError;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import today.tecktip.killbill.backend.routes.MessageBody;

/**
 * Routes in order to send and receive dms
 * 
 * @author cz
 */
@RestController
@RequestMapping("/dms")
public class DmRoutes {
    private record GetDmResponseBody(Dm dm) {}
    private record PostDmResponseBody(Dm dm) {}
    private record GetDmsResponseBody(List<Dm> dms) {}
    private record GetIntResponseBody(int count) {}

    private record SendMessageRequestBody(
        @NotNull String toId,
        @NotNull String message
        ){}

    /**
     * Searches and returns the message passed with messageId
     * @param authToken authentication token
     * @param messageId message to be searched
     * @return message
     * @throws SQLException query failed
     * @throws InvalidArgumentException unable to parse into a UUID
     * @throws AuthenticationFailure You do not have permission
     * @throws ServerError Server died :/
     */
    @GetMapping("")
    public MessageBody getDmById(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String messageId
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError 
    {
        
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        
        UUID id;
        try {
            id = UUID.fromString(messageId);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw new InvalidArgumentException("Unable to parse messageId as UUID");
        }

        Dm dm;
        try {
            dm = Dms.getDmById(id);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Dm does not exist");
        }

        if (!authenticatedUser.id().equals(dm.fromId()) && !authenticatedUser.id().equals(dm.toId())) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }

        return MessageBody.ofSuccess(new GetDmResponseBody(dm));
    }

    /**
     * Creates and sends a new DM
     * @param authToken authentication token
     * @param request is a request body that contains the toId and the message to be sent
     * @return successful if we upserted the dm
     * @throws SQLException query failed
     * @throws InvalidArgumentException unable to parse
     * @throws AuthenticationFailure you do not have permission
     * @throws ServerError server died :/
     */
    @PostMapping("")
    public MessageBody sendNewMessage(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @Valid @RequestBody(required = true) SendMessageRequestBody request
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError 
    {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        Dm dm;

        UUID messageId = UUID.randomUUID();

        UUID fromId = authenticatedUser.id();

        UUID toId;
        try {
            toId = UUID.fromString(request.toId());
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse id as UUID");
        }

        String message;
        try {
            message = request.message();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse message");
        }

        Friend friend;
        try {
        friend = Friends.getFriend(fromId, toId);
        } catch (SQLException e) {
        throw new TransientServerError("Failed to contact database");
        } catch (NotFoundException e) {
        throw new InvalidArgumentException("You are not friends with this user");
        }

        if (!friend.state().equals(FriendState.FRIENDS)) {
        throw new InvalidArgumentException("You are not friends with this user");
        }

        try {
            dm = new Dm(messageId, fromId, toId, Instant.now(), message, MessageState.UNREAD);
            Dms.putDm(dm);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database", e);
        } catch (InvalidArgumentException e) {
            throw new InvalidArgumentException("You passed something that does not exist");
        }

        return MessageBody.ofSuccess(new PostDmResponseBody(dm));
    }

    /**
     * Gets every dm registered in the database. Requires Admin
     * @param authToken authentication token
     * @return all dms ever
     * @throws SQLException query failed
     * @throws AuthenticationFailure You are not admin
     * @throws ServerError server died :/
     */
    @GetMapping("/admin/list")
    public MessageBody getAllDmsEver(
        @RequestHeader(value = "Authorization", required = true) String authToken
    ) throws SQLException, AuthenticationFailure, ServerError 
    {
        
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        List<Dm> dms;

        try  {
            dms = Dms.getAllDmsEver();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact the database", e);
        }
        
        Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        return MessageBody.ofSuccess(new GetDmsResponseBody(dms));
    }

    /**
     * Deletes all dms between 2 users.
     * @param authToken authentication token
     * @param userId1 user 1
     * @param userId2 user 2
     * @return successful if deleted
     * @throws SQLException query failed
     * @throws InvalidArgumentException unable to parse userId as a UUID
     * @throws AuthenticationFailure you don't have permission
     * @throws ServerError server died :/
     */
    @DeleteMapping("/all")
    public MessageBody deleteAll(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String userId1, 
        @RequestParam(required = true) String userId2
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError 
    {
        
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        
        UUID id1;
        UUID id2;

        try {
            id1 = UUID.fromString(userId1);
            if (id1 == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId1 as UUID");
        }

        try {
            id2 = UUID.fromString(userId2);
            if (id2 == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId2 as UUID");
        }

        
        if (!authenticatedUser.id().equals(id1) && !authenticatedUser.id().equals(id2)) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }

        try {
            Dms.deleteAll(id1, id2);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Messages between the 2 users does not exist");
        }

        return MessageBody.ofSuccess(null);
    }

    /**
     * gets all Dms between 2 users
     * @param authToken authentication token
     * @param userId1 user 1
     * @param userId2 user 2
     * @return all dms between 2 users
     * @throws SQLException query failed
     * @throws InvalidArgumentException unbale to parse as UUID
     * @throws AuthenticationFailure you do not have permission
     * @throws ServerError server died :/
     */
    @GetMapping("/list")
    public MessageBody getAllFromTo(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String userId1, 
        @RequestParam(required = true) String userId2
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError 
    {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        List<Dm> dms;
        UUID id1;
        UUID id2;

        try {
            id1 = UUID.fromString(userId1);
            if (id1 == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId1 as UUID");
        }

        try {
            id2 = UUID.fromString(userId2);
            if (id2 == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId2 as UUID");
        }

        try {
            dms = Dms.getAllFromTo(id1, id2);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to connect to database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Dms between these 2 people do not exist");
        }

        if (!authenticatedUser.id().equals(id1) && !authenticatedUser.id().equals(id2)) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }
    
        return MessageBody.ofSuccess(new GetDmsResponseBody(dms));
    }

    /**
     * gets all dms that user has sent.
     * @param authToken authentication token
     * @param userId user to query
     * @return all dms sent by user
     * @throws SQLException query failed
     * @throws AuthenticationFailure you do not have permission
     * @throws ServerError server died :/
     */
    @GetMapping("/list_from")
    public MessageBody getAllFrom(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String userId
    ) throws SQLException, AuthenticationFailure, ServerError 
    {
            
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        List<Dm> dms;
        UUID id;

        try {
            id = UUID.fromString(userId);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse user as UUID");
        }

        try {
            dms = Dms.getAllFrom(id);
        } catch (SQLException e){
            throw new TransientServerError("Failed to connect to the database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Dms do not exist");
        }

        if (!authenticatedUser.id().equals(id)) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }
        
        return MessageBody.ofSuccess(new GetDmsResponseBody(dms));
    }

    /**
     * deletes one message by querying it with the UUID. Requires Admin or to be the user who sent the message
     * @param authToken authentication token
     * @param messageId message to delete
     * @return successful if deleted message
     * @throws SQLException query failed
     * @throws InvalidArgumentException
     * @throws AuthenticationFailure not admin or not relevent user
     * @throws ServerError server died :/
     */
    @DeleteMapping("")
    public MessageBody deleteMessage(
        @RequestHeader(value = "authorization", required = true) String authToken,
        @RequestParam(required = true) String messageId
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError
    {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
    
        UUID id;
        try {
            id = UUID.fromString(messageId);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse messageId as UUID");
        }
    
        Dm dm = Dms.getDmById(id);

        if (!authenticatedUser.id().equals(dm.fromId())) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }

        try {
            Dms.deleteOne(id);
        } catch (SQLException e) {
            throw new TransientServerError("Unable to contact database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Message does not exist");
        }

        return MessageBody.ofSuccess(null);
    }

    /**
     * Gets all unread dms to passed user
     * @param authToken authentication token
     * @param userId user
     * @return list of dms
     * @throws SQLException query failed
     * @throws InvalidArgumentException unable to parse as UUID
     * @throws AuthenticationFailure you do not have permission
     * @throws ServerError server died :/
     */
    @GetMapping("/list_unread")
    public MessageBody getUnreadTo(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String userId
    ) throws SQLException, InvalidArgumentException, AuthenticationFailure, ServerError
    {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        
        List<Dm> dms;

        UUID id;
        try {
            id = UUID.fromString(userId);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId as UUID");
        }
        
        try {
            dms = Dms.getUnreadTo(id);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Unread messages do not exist");
        }
        
        if (!authenticatedUser.id().equals(id)) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }

        return MessageBody.ofSuccess(new GetDmsResponseBody(dms));
    }

    /**
     * Gives every unread dm in the database. Requires Admin
     * @param authToken authentication token
     * @return every unread dm in the database
     * @throws SQLException query failed
     * @throws AuthenticationFailure you aren't admin
     * @throws ServerError server died :/
     */
    @GetMapping("/admin/list_unread")
    public MessageBody getUnreadAll(
        @RequestHeader(value = "Authorization", required = true) String authToken
    ) throws SQLException, AuthenticationFailure, ServerError 
    {
        
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        List<Dm> dms;

        try {
            dms = Dms.getAllUnreadDmsEver();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Somehow there are absolutely no unread messages");
        }

        Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        return MessageBody.ofSuccess(new GetDmsResponseBody(dms));
    }

    /**
     * Counts number of unread messages to user
     * @param authToken authentication token
     * @param userId user
     * @return number of unread messages
     * @throws SQLException query failed
     */
    @GetMapping("/count_unread")
    public MessageBody getNumUnread(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String userId
    ) throws SQLException 
    {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        int numUnread;
        UUID id;
        try {
            id = UUID.fromString(userId);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse userId as UUID");
        }
        
        try {
            numUnread = Dms.countUnreadUser(id);
        } catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
            throw new TransientServerError("Failed to contact database", e);
        }

        if (!authenticatedUser.id().equals(id)) {
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }
        
        return MessageBody.ofSuccess(new GetIntResponseBody(numUnread));
    }

}
