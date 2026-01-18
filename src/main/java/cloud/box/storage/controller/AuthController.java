package cloud.box.storage.controller;

import cloud.box.storage.model.User;
import cloud.box.storage.repository.UserRepository;
import cloud.box.storage.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    // REGISTER → returns API key
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password){
        if(userRepository.findByUsername(username).isPresent())
            return ResponseEntity.badRequest().body("Username exists");

        User user = new User(username, password);
        userRepository.save(user);

        return ResponseEntity.ok("Registered successfully! API Key: " + user.getApiKey());
    }

    // LOGIN → issues JWT cookie
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {

        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getUsername());
                    Cookie cookie = new Cookie("CLOUDBOX_TOKEN", token);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(false); // true if HTTPS
                    cookie.setPath("/");
                    cookie.setMaxAge(24*60*60); // 1 day
                    response.addCookie(cookie);

                    return ResponseEntity.ok("Login successful!");
                })
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response){
        Cookie cookie = new Cookie("CLOUDBOX_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully");
    }
}