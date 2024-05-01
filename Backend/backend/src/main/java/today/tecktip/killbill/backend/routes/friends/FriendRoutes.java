package today.tecktip.killbill.backend.routes.friends;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import today.tecktip.killbill.backend.auth.Authenticator;
import today.tecktip.killbill.backend.db.friends.Friend;
import today.tecktip.killbill.backend.db.friends.FriendState;
import today.tecktip.killbill.backend.db.friends.Friends;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.UserRole;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.ServerError;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import today.tecktip.killbill.backend.routes.MessageBody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Routes for managing friends.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/friends")
public class FriendRoutes {
    /**
     * A response body for listing friends.
     * @param friends List of friends retrieved
     */
    private record GetFriendsResponseBody(List<Friend> friends) {}

    /**
     * Gets a list of all friends.
     * <p>
     * When listing all or listing a user other than the authenticated one, this is an admin method.
     * Regular users can only list their own friends.
     * 
     * @param authToken Authorization header
     * @param userId If specified, gets the friend links matching only this user ID
     * @return List of all friend links registered
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException No friend links are registered
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("/list")
    public MessageBody listFriends(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(required = false) String userId
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID filterId = null;
        if (userId != null) {
            try {
                filterId = UUID.fromString(userId);
                if (filterId == null) throw new Exception();
            } catch (Exception e) {
                throw new InvalidArgumentException("Unable to parse ID as UUID.");
            }
        }

        // Can't list another user's friends if you're not an admin
        if (filterId == null || !authenticatedUser.id().equals(filterId)) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        List<Friend> friends = new ArrayList<>();
        try {
            if (filterId == null) {
                friends = Friends.getFriends();
            } else {
                friends = Friends.getFriends(filterId);
            }
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(new GetFriendsResponseBody(friends));
    }

    /**
     * A response body for a single friend link.
     * @param friend Friend retrieved
     */
    private record GetFriendResponseBody(Friend friend) {}

    /**
     * Gets a single friend link.
     * <p>
     * Authenticated method. User or admin. Users can only get links they're a part of.
     * 
     * @param authToken Authorization header
     * @param userId1 First user in friend link
     * @param userId2 Second user in this friend link
     * @return Friend link between these users
     * @throws TransientServerError SQL contact failed
     * @throws ServerError No such link
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("")
    public MessageBody getUser(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(required = true) String userId1,
            @RequestParam(required = true) String userId2
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID id1;
        UUID id2;
        try {
            id1 = UUID.fromString(userId1);
            id2 = UUID.fromString(userId2);
            if (id1 == null || id2 == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        // Confirm the user is one of the specified UUIDs. Otherwise, they must be an admin.
        if (!id1.equals(authenticatedUser.id()) && !id2.equals(authenticatedUser.id())) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        Friend friendLink;
        try {
            friendLink = Friends.getFriend(id1, id2);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Friend link does not exist.");
        }

        return MessageBody.ofSuccess(new GetFriendResponseBody(friendLink));
    }

    /**
     * Request body for friend add.
     * @param userId Target user's ID
     */
    private record CreateFriendRequestBody(@NotNull String userId) {}

    /**
     * Response body for friend add.
     * @param user Created/modified friend link
     */
    private record CreateFriendResponseBody(@NotNull Friend friend) {}

    /**
     * Invites a friend for the authenticated user account.
     * 
     * @param authToken Authorization header
     * @param request Friend add request body
     * @return Created friend link
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @PostMapping("")
    public MessageBody addFriend(
            @RequestHeader(value = "Authorization", required = false) String authToken, 
            @Valid @RequestBody(required = true) CreateFriendRequestBody request
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Get the specified user
        UUID targetId;
        try {
            targetId = UUID.fromString(request.userId());
            if (targetId == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        // Prevent friending yourself (I would NEVER accidentally do this, breaking the frontend)
        if (targetId.equals(authenticatedUser.id())) {
            throw new InvalidArgumentException("You cannot friend yourself.");
        }

        User user;
        try {
            user = Users.getUser(targetId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("User not found.");
        }

        // Check if such a friend link exists
        Friend friend;
        try {
            friend = Friends.getFriend(authenticatedUser.id(), user.id());

            // Exists. Throw an error
            if (FriendState.INVITED.equals(friend.state())) {
                if (friend.fromId().equals(authenticatedUser.id()))
                    throw new InvalidArgumentException("You have already sent a friend request to this user.");
                else
                    throw new InvalidArgumentException("This user has already invited you to be friends.");
            } else if (FriendState.INVITED.equals(friend.state())) {
                throw new InvalidArgumentException("You are already friends with this user.");
            } else throw new ServerError("Impossible state.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) { }
        

        // Generate the friend link
        friend = new Friend(
            authenticatedUser.id(),
            user.id(),
            Instant.now(),
            FriendState.INVITED
        );

        // Send out to database
        try {
            Friends.putFriend(friend);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to send friend link to database.", e);
        }

        return MessageBody.ofSuccess(new CreateFriendResponseBody(friend));
    }

    /**
     * Accepts a friend for the authenticated user account.
     * 
     * @param authToken Authorization header
     * @param request Friend add request body
     * @return Updated friend link
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @PutMapping("")
    public MessageBody acceptFriend(
            @RequestHeader(value = "Authorization", required = false) String authToken, 
            @Valid @RequestBody(required = true) CreateFriendRequestBody request
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Get the specified user
        UUID targetId;
        try {
            targetId = UUID.fromString(request.userId());
            if (targetId == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        User user;
        try {
            user = Users.getUser(targetId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("User not found.");
        }

        // Check if such a friend link exists
        Friend friend;
        try {
            friend = Friends.getFriend(authenticatedUser.id(), user.id());
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) { 
            throw new InvalidArgumentException("You have not been invited to be a friend with this user.");
        }

        // Ensure that the link is in the proper state
        if (FriendState.FRIENDS.equals(friend.state())) {
            throw new InvalidArgumentException("You are already friends with this user.");
        }

        // And make sure this is the invited user
        if (!authenticatedUser.id().equals(friend.toId())) {
            throw new InvalidArgumentException("You can't accept your own friend invitation.");
        }

        // Change the state
        friend = new Friend(
            friend.fromId(),
            friend.toId(),
            Instant.now(),
            FriendState.FRIENDS
        );

        // Send out to database
        try {
            Friends.putFriend(friend);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to send friend link to database.", e);
        }

        return MessageBody.ofSuccess(new CreateFriendResponseBody(friend));
    }

    /**
     * Response body for friend deletion.
     * @param friend Deleted friend
     */
    private record DeleteFriendResponseBody(@NotNull Friend friend) {}

    /**
     * Deletes a friend.
     * 
     * @param authToken Authorization header
     * @param userId ID of friend to delete
     * @return Deleted friend
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @DeleteMapping("")
    public MessageBody deleteFriend(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(required = true) String userId
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Get the specified user
        UUID targetId;
        try {
            targetId = UUID.fromString(userId);
            if (targetId == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        // Check if such a friend link exists
        Friend friend;
        try {
            friend = Friends.getFriend(authenticatedUser.id(), targetId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) { 
            throw new InvalidArgumentException("You are not friends with or have no outstanding invitation to be friends with this user.");
        }
        
        // Kill it
        try {
            Friends.deleteFriend(friend);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(new DeleteFriendResponseBody(friend));
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public FriendRoutes() { }
}
