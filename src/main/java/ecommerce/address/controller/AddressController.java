package ecommerce.address.controller;

import ecommerce.address.dto.request.AddressRequest;
import ecommerce.address.dto.response.AddressResponse;
import ecommerce.address.service.AddressService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.UnauthorizedException;
import ecommerce.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {

        validateUser(user);

        log.info("User {} creating address", user.getUserId());

        AddressResponse response =
                addressService.createAddress(request, user.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAll(
            @AuthenticationPrincipal UserDetailsImpl user) {

        validateUser(user);

        log.debug("User {} fetching all addresses", user.getUserId());

        return ResponseEntity.ok(
                addressService.getUserAddresses(user.getUserId())
        );
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {

        validateUser(user);

        log.debug("User {} fetching address {}", user.getUserId(), id);

        return ResponseEntity.ok(
                addressService.getUserAddressById(id, user.getUserId())
        );
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {

        validateUser(user);

        log.info("User {} updating address {}", user.getUserId(), id);

        return ResponseEntity.ok(
                addressService.updateAddress(id, user.getUserId(), request)
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {

        validateUser(user);

        log.warn("User {} deleting address {}", user.getUserId(), id);

        return new ResponseEntity<>(addressService.deleteAddress(id, user.getUserId()), HttpStatus.OK);

    }

    // ================= COMMON VALIDATION =================
    private void validateUser(UserDetailsImpl user) {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized access");
        }
    }
}