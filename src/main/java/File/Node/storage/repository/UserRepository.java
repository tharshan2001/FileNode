package File.Node.storage.repository;

import File.Node.storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (for login & registration checks)
    Optional<User> findByEmail(String email);

    // Find user by API key
    Optional<User> findByApiKey(String apiKey);
}