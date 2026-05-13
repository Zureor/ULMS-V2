package com.ulms.service.impl;

import com.ulms.dto.request.RegisterRequest;
import com.ulms.model.AuditLog;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import com.ulms.repository.AuditLogRepository;
import com.ulms.repository.UserRepository;
import com.ulms.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User createUser(RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required for new users");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.valueOf(request.getRole() != null ? request.getRole().toUpperCase() : "STUDENT"));
        user.setStudentId(request.getStudentId());
        user.setDepartment(request.getDepartment());
        user.setActive(true);

        User saved = userRepository.save(user);
        audit(saved, "CREATE", "User", saved.getId(), "Created user: " + saved.getUsername());
        return saved;
    }

    @Override
    @Transactional
    public User updateUser(Long id, RegisterRequest request) {
        User existing = getUserById(id);

        // Check if email is already taken by ANOTHER user
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(id)) {
                throw new RuntimeException("Email already taken by another user: " + request.getEmail());
            }
        });

        existing.setFullName(request.getFullName());
        existing.setEmail(request.getEmail());
        existing.setDepartment(request.getDepartment());
        existing.setStudentId(request.getStudentId());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            existing.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        }

        User saved = userRepository.save(existing);
        audit(saved, "UPDATE", "User", id, "Updated user: " + saved.getUsername());
        return saved;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        audit(user, "DELETE", "User", id, "Deleted user: " + user.getUsername());
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Page<User> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    private void audit(User user, String action, String entityType, Long entityId, String details) {
        if (user == null) return;
        AuditLog logEntry = new AuditLog();
        logEntry.setAction(action);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setPerformedBy(user);
        logEntry.setDetails(details);
        auditLogRepository.save(logEntry);
    }
}