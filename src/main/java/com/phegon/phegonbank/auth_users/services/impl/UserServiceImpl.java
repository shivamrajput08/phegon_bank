package com.phegon.phegonbank.auth_users.services.impl;

import com.phegon.phegonbank.auth_users.dtos.UpdatePasswordRequest;
import com.phegon.phegonbank.auth_users.dtos.UserDTO;
import com.phegon.phegonbank.auth_users.entity.User;
import com.phegon.phegonbank.auth_users.repo.UserRepo;
import com.phegon.phegonbank.auth_users.services.UserService;

import com.phegon.phegonbank.exceptions.BadRequestException;
import com.phegon.phegonbank.exceptions.NotFoundException;
import com.phegon.phegonbank.notification.dtos.NotificationDTO;
import com.phegon.phegonbank.notification.services.NotificationService;
import com.phegon.phegonbank.res.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
//

    // this is for backend
//    private final String uploadDir = "uploads/profile-pictures/";


    // this is for frontend
//    private final String uploadDir = "C:\\phegonDev\\phegon-bank-react/public/profile-picture/";
    private final String uploadDir = "uploads/profile-pictures/";
    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NotFoundException("user is not authenticated");
        }
        String email = authentication.getName();
        return userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

//    @Override
//    public Response<UserDTO> getMyProfile() {
//        User user = getCurrentLoggedInUser();
//        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
//
//        return Response.<UserDTO>builder()
//                .statusCode(HttpStatus.OK.value())
//                .message("User retrieved")
//                .data(userDTO)
//                .build();
//    }
@Override
public Response<UserDTO> getMyProfile() {
    User user = getCurrentLoggedInUser();

    // Agar user entity me accounts list hai, toh use yahan load karlein
    if (user.getAccounts() != null) {
        org.hibernate.Hibernate.initialize(user.getAccounts());
    }

    // Agar error me koi aur collection ka naam bhi tha (jaise transactions), toh use bhi initialize karlein:
    // if (user.getTransactions() != null) { org.hibernate.Hibernate.initialize(user.getTransactions()); }

    UserDTO userDTO = modelMapper.map(user, UserDTO.class);

    return Response.<UserDTO>builder()
            .statusCode(HttpStatus.OK.value())
            .message("User retrieved")
            .data(userDTO)
            .build();
}
//    @Override
//    public Response<Page<UserDTO>> getAllUsers(int page, int size) {
//        Page<User> users = userRepo.findAll(PageRequest.of(page, size));
//        Page<UserDTO> userDTOS = users.map(user -> modelMapper.map(user, UserDTO.class));
//
//        return Response.<Page<UserDTO>>builder()
//                .statusCode(HttpStatus.OK.value())
//                .message("Users retrived")
//                .data(userDTOS)
//                .build();
//    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(int page, int size) {
        Page<User> users = userRepo.findAll(PageRequest.of(page, size));

        Page<UserDTO> userDTOS = users.map(user -> {
            if (user.getAccounts() != null) {
                org.hibernate.Hibernate.initialize(user.getAccounts());
            }
            return modelMapper.map(user, UserDTO.class);
        });

        return Response.<Page<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrived")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentLoggedInUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        if (oldPassword == null || newPassword == null) {
            throw new BadRequestException("Old and new Password Required");

        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old Password not Correct");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);


        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Your Password was Successfullly Changed ")
                .templateName("password-change")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password Changed successfully")
                .build();
    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {
        User user = getCurrentLoggedInUser();

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Path oldFile = Paths.get(user.getProfilePictureUrl());
                if (Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            }

            // Generate a unique file name to avoid conflicts
            String originalFileName = file.getOriginalFilename();

            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + fileExtension;

            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(file.getInputStream(), filePath);

//            String fileUrl = uploadDir + newFileName;
            String fileUrl = "profile-picture/" + newFileName;  // this is for frontend


            user.setProfilePictureUrl(fileUrl);

            userRepo.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile picture uploaded successfully")
                    .data(fileUrl)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}