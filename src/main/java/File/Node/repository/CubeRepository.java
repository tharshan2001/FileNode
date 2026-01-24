package File.Node.repository;

import File.Node.entity.Cube;
import File.Node.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CubeRepository extends JpaRepository<Cube, Long> {
    List<Cube> findByOwner(User owner);
    Optional<Cube> findByApiKey(String apiKey);
}
