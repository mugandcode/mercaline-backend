package com.mercaline.users.controllers;

import com.mercaline.dto.ProductResponseSummaryDTO;
import com.mercaline.dto.converter.ProductoDTOConverter;
import com.mercaline.dto.converter.UserDTOConverter;
import com.mercaline.service.ProductService;
import com.mercaline.users.dto.ResponseUserCompleteDTO;
import com.mercaline.users.dto.ResponseUserSummaryDTO;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mercaline.users.Model.UserEntity;
import com.mercaline.users.services.UserEntityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private final UserEntityService userEntityService;
    private final ProductService productService;
    private final ProductoDTOConverter productoDTOConverter;
    private final UserDTOConverter userDTOConverter;

    @PostMapping("/registrar")
    public ResponseEntity<ResponseUserSummaryDTO> createUser(@Validated(UserEntity.OnCreate.class) @RequestBody UserEntity user) {
        return ResponseEntity.ok(userDTOConverter
                .convertToResponseUserSummaryDTO(this.userEntityService.newUser(user)));
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseUserCompleteDTO> me(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(userDTOConverter.convertToResponseUserCompleteDTO(user));
    }
    
    @GetMapping("/check-username")
public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
    boolean exists = userEntityService.findUserByUsername(username).isPresent();
    return ResponseEntity.ok(exists);
}
@GetMapping("/check-email")
public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
    boolean exists = userEntityService.findUserByEmail(email).isPresent();
    return ResponseEntity.ok(exists);
}


    @PutMapping("/update")
    public ResponseEntity<ResponseUserCompleteDTO> updateUser(@AuthenticationPrincipal UserEntity user, @Validated(UserEntity.OnUpdate.class) @RequestBody UserEntity updatedUser) {
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getLastname() != null) {
            user.setLastname(updatedUser.getLastname());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getTel() != null) {
            user.setTel(updatedUser.getTel());
        }

        UserEntity updated = userEntityService.updateUser(user);
        return ResponseEntity.ok(userDTOConverter.convertToResponseUserCompleteDTO(updated));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserEntity user) {
        this.userEntityService.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myproducts")
    public ResponseEntity<Page<ProductResponseSummaryDTO>> myProducts(@AuthenticationPrincipal UserEntity user, Pageable pageable) {
        Page<ProductResponseSummaryDTO> myProducts = (this.productService.findByUser(user, pageable))
                .map(product -> productoDTOConverter.convertToGetProduct(product, user));
        return ResponseEntity.ok().body(myProducts);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseSummaryDTO>> otherProducts(@AuthenticationPrincipal UserEntity user, Pageable pageable) {
        Page<ProductResponseSummaryDTO> products = (this.productService.findOthers(user, pageable))
                .map(product -> productoDTOConverter.convertToGetProduct(product, product.getUser()));
        return ResponseEntity.ok().body(products);
    }
}