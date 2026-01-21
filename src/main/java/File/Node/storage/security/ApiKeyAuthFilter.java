package File.Node.storage.security;

import File.Node.storage.model.User;
import File.Node.storage.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public ApiKeyAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Check API key in query parameter or header
        String apiKey = request.getParameter("apiKey");
        if (apiKey == null) {
            apiKey = request.getHeader("X-API-KEY");
        }

        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByApiKey(apiKey).orElse(null);
            if (user != null) {
                // Authenticate user with email as principal
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(), // use email instead of username
                                null,
                                null // no roles for now
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}