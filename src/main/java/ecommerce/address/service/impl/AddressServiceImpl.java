package ecommerce.address.service.impl;

import ecommerce.address.dto.request.AddressRequest;
import ecommerce.address.dto.response.AddressResponse;
import ecommerce.address.model.Address;
import ecommerce.address.repository.AddressRepository;
import ecommerce.address.service.AddressService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    // ================= CREATE =================
    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request, Long userId) {

        log.debug("Creating address for userId={}", userId);

        Address address = modelMapper.map(request, Address.class);
        address.setUserId(userId);

        Address saved = addressRepository.save(address);

        log.info("Address created: id={} for userId={}", saved.getAddressId(), userId);

        return modelMapper.map(saved, AddressResponse.class);
    }

    // ================= GET ALL =================
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {

        log.debug("Fetching addresses for userId={}", userId);

        List<Address> addresses = addressRepository.findByUserId(userId);

        log.info("Found {} addresses for userId={}", addresses.size(), userId);

        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressResponse.class))
                .toList();
    }

    // ================= GET BY ID =================
    @Override
    @Transactional(readOnly = true)
    public AddressResponse getUserAddressById(Long addressId, Long userId) {

        log.debug("Fetching addressId={} for userId={}", addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        validateOwnership(address, userId);

        return modelMapper.map(address, AddressResponse.class);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, Long userId, AddressRequest request) {

        log.debug("Updating addressId={} for userId={}", addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        validateOwnership(address, userId);

        // 🔥 SAFE UPDATE (no overwrite nulls)
        modelMapper.map(request, address);

        Address updated = addressRepository.save(address);

        log.info("Address updated: id={}", addressId);

        return modelMapper.map(updated, AddressResponse.class);
    }

    // ================= DELETE =================
    @Override
    @Transactional
    public MessageResponse deleteAddress(Long addressId, Long userId) {

        log.debug("Deleting addressId={} for userId={}", addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        validateOwnership(address, userId);

        addressRepository.delete(address);

        log.info("Address deleted: id={}", addressId);

        return new MessageResponse("Address deleted successfully");
    }

    // ================= HELPER =================
    private void validateOwnership(Address address, Long userId) {
        if (!address.getUserId().equals(userId)) {
            log.error("Unauthorized access: userId={} addressId={}",
                    userId, address.getAddressId());

            throw new APIException("Access denied: address does not belong to user");
        }
    }
}