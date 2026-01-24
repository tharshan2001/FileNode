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

    public User resolveUser(Authentication auth, String apiKey) {
        if (apiKey != null) {
            return userRepository.findByApiKey(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid API Key"));
        }
        if (auth != null) {
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("No authentication provided");
    }
}
