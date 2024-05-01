package today.tecktip.killbill.backend.db.users;

/**
 * Enum representing the possible roles a user can be.
 * @author cs
 */
public enum UserRole {
    /**
     * Regular user. Anyone can sign up as one of these.
     */
    USER,
    /**
     * Admin account. Can perform basically any action. 
     * <p>
     * Must be created by another admin user or the admin init key.
     */
    ADMIN
}
