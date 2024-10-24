package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.UserRepository;
import ITMO.ConstructionDocs.model.dto.request.UserReq;
import ITMO.ConstructionDocs.model.dto.request.UserToCompanyReq;
import ITMO.ConstructionDocs.model.dto.response.UserResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.utils.PaginationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;

    public User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public UserResp createUser(UserReq request) {
        validateEmail(request);

        if (request.getPhoneNumber() != null) {
            validatePhoneNumber(request);
        }

        userRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(user -> {
                    throw new CustomException(String.format("User with email: %s already exists", request.getEmail()), HttpStatus.BAD_REQUEST);
                });

        User user = objectMapper.convertValue(request, User.class);

        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(CommonStatus.CREATED);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User save = userRepository.save(user);

        return objectMapper.convertValue(save, UserResp.class);
    }

    private void validateEmail(UserReq request) {
        if (!EmailValidator.getInstance().isValid(request.getEmail())) {
            throw new CustomException("Invalid email format", HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePhoneNumber(UserReq request) {
        String phoneNumberPattern = "^\\+?[0-9]{10,15}$";
        if (!request.getPhoneNumber().matches(phoneNumberPattern)) {
            throw new CustomException("Invalid phone number format", HttpStatus.BAD_REQUEST);
        }
    }

    public UserResp getUser(Long id) {
        return objectMapper.convertValue(getUserFromDB(id), UserResp.class);
    }

    public User getUserFromDB(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }

    public UserResp updateUser(Long id, UserReq request) {
        User user = getUserFromDB(id);
        if (request.getEmail() != null) {
            validateEmail(request);
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPhoneNumber() != null) {
            validatePhoneNumber(request);
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(CommonStatus.UPDATED);

        User save = userRepository.save(user);

        return objectMapper.convertValue(save, UserResp.class);
    }

    public void deleteUser(Long id) {
        User user = getUserFromDB(id);
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(CommonStatus.DELETED);
        userRepository.save(user);
    }

    public Page<UserResp> getAllUsers(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<User> all;
        if (filter == null) {
            all = userRepository.findAllNotDeleted(pageRequest, CommonStatus.DELETED);
        } else {
            all = userRepository.findAllNotDeletedAndFiltered(pageRequest, CommonStatus.DELETED, filter.toLowerCase());
        }

        List<UserResp> content = all.getContent().stream()
                .map(user -> objectMapper.convertValue(user, UserResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Optional<User> getUserByEmail(String email) {

        return userRepository.findByEmailIgnoreCase(email);
    }

    public void addUserToCompany(@Valid UserToCompanyReq request) {
        User user = getUserFromDB(request.getUserId());
        Company company = companyService.getCompanyFromDB(request.getCompanyId());

        company.getUsers().add(user);
        companyService.updateCompanyData(company);

        user.setCompany(company);
        userRepository.save(user);
    }

    public Page<UserResp> getUsersByCompany(Long id, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Company company = companyService.getCompanyFromDB(id);

        if (company.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Company Info with id: %d is DELETED from DataBase", id), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<User> userPage = userRepository.findAllByCompany(pageRequest, id);

        List<UserResp> content = userPage.getContent().stream()
                .map(user -> objectMapper.convertValue(user, UserResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, userPage.getTotalElements());
    }
}
