package com.ecommerce.Ecommerce_Application.controller;

import com.ecommerce.Ecommerce_Application.model.User;
import com.ecommerce.Ecommerce_Application.payload.request.AddressRequest;
import com.ecommerce.Ecommerce_Application.payload.response.AddressResponse;
import com.ecommerce.Ecommerce_Application.service.AddressService;
import com.ecommerce.Ecommerce_Application.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest addressDTO){
        User user = authUtil.loggedInUser();
        AddressResponse savedAddressDTO = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(){
        List<AddressResponse> addressList = addressService.getAddresses();
        return new ResponseEntity<>(addressList, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long addressId){
        AddressResponse addressDTO = addressService.getAddressesById(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }


    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressResponse>> getUserAddresses(){
        User user = authUtil.loggedInUser();
        List<AddressResponse> addressList = addressService.getUserAddresses(user);
        return new ResponseEntity<>(addressList, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long addressId
            , @RequestBody AddressRequest addressDTO){
        AddressResponse updatedAddress = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> updateAddress(@PathVariable Long addressId){
        String status = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
