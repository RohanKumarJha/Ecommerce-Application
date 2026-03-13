package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.model.User;
import com.ecommerce.Ecommerce_Application.payload.request.AddressRequest;
import com.ecommerce.Ecommerce_Application.payload.response.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(AddressRequest addressDTO, User user);
    List<AddressResponse> getAddresses();
    AddressResponse getAddressesById(Long addressId);
    List<AddressResponse> getUserAddresses(User user);
    AddressResponse updateAddress(Long addressId, AddressRequest addressDTO);
    String deleteAddress(Long addressId);
}