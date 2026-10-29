package com.fintech.customer.service;

import com.fintech.customer.dto.CustomerRequest;
import com.fintech.customer.dto.CustomerResponse;
import com.fintech.customer.entity.Customer;
import com.fintech.customer.mapper.CustomerMapper;
import com.fintech.customer.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request, Authentication auth) {
        if (!isAdmin(auth) && !auth.getName().equalsIgnoreCase(request.email())) {
            throw new SecurityException("Un client ne peut créer que son propre profil");
        }
        return CustomerMapper.toResponse(repository.save(CustomerMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(Authentication auth) {
        List<Customer> customers = isAdmin(auth) ? repository.findAll() : repository.findByEmailIgnoreCase(auth.getName());
        return customers.stream().map(CustomerMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id, Authentication auth) {
        Customer customer = findOwned(id, auth);
        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request, Authentication auth) {
        Customer customer = findOwned(id, auth);
        if (!isAdmin(auth) && !auth.getName().equalsIgnoreCase(request.email())) {
            throw new SecurityException("Un client ne peut pas transférer son profil à un autre compte");
        }
        CustomerMapper.update(customer, request);
        return CustomerMapper.toResponse(repository.save(customer));
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        repository.delete(findOwned(id, auth));
    }

    private Customer findOwned(Long id, Authentication auth) {
        Customer customer = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Profil client introuvable"));
        if (!isAdmin(auth) && !customer.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new SecurityException("Accès interdit à ce profil");
        }
        return customer;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}