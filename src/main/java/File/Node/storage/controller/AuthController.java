package File.Node.storage.controller;

import File.Node.storage.dto.ApiResponse;
import File.Node.storage.dto.LoginRequest;
import File.Node.storage.dto.RegisterRequest;
import File.Node.storage.model.User;
import File.Node.storage.repository.UserRepository;
import File.Node.storage.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =============================
    // REGISTER
    // =============================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse("Email already exists"));
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(), hashedPassword);
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse("Registered successfully!"));
    }

    // =============================
    // LOGIN → issues JWT cookie
    // =============================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request,
                                             HttpServletResponse response) {

        return userRepository.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getEmail());

                    Cookie cookie = new Cookie("CLOUDBOX_TOKEN", token);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(false); // true if HTTPS
                    cookie.setPath("/");
                    cookie.setMaxAge(24 * 60 * 60); // 1 day
                    response.addCookie(cookie);

                    return ResponseEntity.ok(new ApiResponse("Login successful!"));
                })
                .orElse(ResponseEntity.status(401)
                        .body(new ApiResponse("Invalid credentials")));
    }

    // =============================
    // LOGOUT → removes cookie
    // =============================
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("CLOUDBOX_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(new ApiResponse("Logged out successfully"));
    }

    // =============================
    // GET API KEY → secure, only for authenticated users
    // =============================
    @GetMapping("/my-apikey")
    public ResponseEntity<ApiResponse> getApiKey(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(new ApiResponse("Unauthorized"));
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(new ApiResponse("User not found"));
        }

        // Return API key in JSON
        return ResponseEntity.ok(new ApiResponse(user.getApiKey()));
    }


    // =============================
    // GET CURRENT USER → auth/me
    // =============================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(new ApiResponse("Unauthorized"));
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(new ApiResponse("User not found"));
        }

        // Return user info (excluding password)
        return ResponseEntity.ok(new ApiResponse(
                "id: " + user.getId() +
                        ", name: " + user.getName() +
                        ", email: " + user.getEmail() +
                        ", apiKey: " + user.getApiKey()
        ));
    }
}