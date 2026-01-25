package File.Node.utils.user;

import File.Node.entity.User;
import File.Node.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserResolverService {

    private final UserRepository userRepository;

    public UserResolverService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolves a User entity from either a JWT-provided Authentication or an API key.
     *
     * @param auth   the Spring Security Authentication object (from JWT)
     * @param apiKey optional API key (can be null)
     * @return User entity
     * @throws RuntimeException if no valid user is found
     */
    public User resolveUser(Authentication auth, String apiKey) {

        // First, try API key if provided
        if (apiKey != null && !apiKey.isBlank()) {
            return userRepository.findByApiKey(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid API key"));
        }

        // Then try authentication
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + auth.getName()));
        }

        // Neither API key nor authentication worked
        throw new RuntimeException("Authentication required");
    }
}
