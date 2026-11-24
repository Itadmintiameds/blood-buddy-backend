package bloodbuddy.backend.enums;

/** How a donor's consent was captured. */
public enum ConsentSource {
    /** The donor ticked the consent checkbox on the public registration form. */
    SELF,
    /** Collected offline (paper form / camp) and attested by a superadmin during a bulk import. */
    OFFLINE_IMPORT
}
