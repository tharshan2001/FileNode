package File.Node.security;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import File.Node.repository.UserRepository;
import io.micrometer.common.lang.NonNullApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@NonNullApi
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final CubeRepository cubeRepository;
    private final CubeApiSecretUtil secretUtil; // inject your secret utility

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");
        String apiSecret = request.getHeader("X-API-SECRET");
        String username = request.getHeader("X-USERNAME");

        System.out.println("=== API KEY AUTH FILTER DEBUG ===");
        System.out.println("X-API-KEY: " + apiKey);
        System.out.println("X-API-SECRET: " + apiSecret);
        System.out.println("X-USERNAME: " + username);

        if (apiKey == null || apiSecret == null || username == null) {
            System.out.println("Missing API key or username");
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Lookup user by username or email
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);

            if (user == null) {
                System.out.println("User not found!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            System.out.println("User found: ID=" + user.getId() + ", username=" + user.getUsername());

            // Lookup cube by API key + owner only
            Cube cube = cubeRepository.findByApiKey(apiKey)
                    .filter(c -> c.getOwner().getId().equals(user.getId()))
                    .orElse(null);

            if (cube == null) {
                System.out.println("Cube not found for this user or API key!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Verify API secret matches encoded secret in DB
            if (!secretUtil.matches(apiSecret, cube.getApiSecret())) {
                System.out.println("API secret does not match!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            System.out.println("Cube authenticated: " + cube.getName());

            // Set Spring Security authentication
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
