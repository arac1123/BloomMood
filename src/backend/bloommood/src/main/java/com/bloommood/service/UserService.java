package com.bloommood.service;

import com.bloommood.dto.UpdateRequest;
import com.bloommood.dto.UserViewDto;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserViewDto getInfo(Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return new UserViewDto(user.getUid(), user.getEmail(), user.getUname(), user.getRole());
    }

    @Transactional
    public boolean updateMe(Long uid, UpdateRequest req) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        boolean changed = false;

        if (req.getUname() != null) {
            String newName = req.getUname().trim();
            if (newName.isBlank()) {
                throw new IllegalArgumentException("uname cannot be blank");
            }
            user.setUname(newName);
            changed = true;
        }

        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (newEmail.isBlank()) {
                throw new IllegalArgumentException("email cannot be blank");
            }

            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("email already exists");
                }
                user.setEmail(newEmail);
                changed = true;
            }
        }

        if (req.getNewPassword() != null) {
            String newPw = req.getNewPassword();
            if (newPw.isBlank()) {
                throw new IllegalArgumentException("newPassword cannot be blank");
            }
            if (req.getOldPassword() == null || req.getOldPassword().isBlank()) {
                throw new IllegalArgumentException("oldPassword required to change password");
            }
            if (user.getPassword() == null || !passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
                throw new SecurityException("invalid old password");
            }

            user.setPassword(passwordEncoder.encode(newPw));
            changed = true;
        } else {
            if (req.getOldPassword() != null && !req.getOldPassword().isBlank()) {
                throw new IllegalArgumentException("newPassword required when oldPassword is provided");
            }
        }

        if (!changed) {
            return false;
        }

        userRepository.save(user);
        return true;
    }
}

