package ecommerce.address.service;

import ecommerce.address.dto.request.AddressRequest;
import ecommerce.address.dto.response.AddressResponse;
import ecommerce.core.dto.response.MessageResponse;

import java.util.List;

public interface AddressService {

    AddressResponse createAddress(AddressRequest request, Long userId);

    List<AddressResponse> getUserAddresses(Long userId);

    AddressResponse getUserAddressById(Long addressId, Long userId);

    AddressResponse updateAddress(Long addressId, Long userId, AddressRequest request);

    MessageResponse deleteAddress(Long addressId, Long userId);
}