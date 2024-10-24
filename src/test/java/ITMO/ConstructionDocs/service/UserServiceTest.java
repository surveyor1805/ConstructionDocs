package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.UserRepository;
import ITMO.ConstructionDocs.model.dto.request.UserReq;
import ITMO.ConstructionDocs.model.dto.request.UserToCompanyReq;
import ITMO.ConstructionDocs.model.dto.response.UserResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Spy
    ObjectMapper objectMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    CompanyService companyService;

    @Test
    void getCurrentUser() {
    }

    @Test
    void createUser() {
        UserReq userReq = new UserReq();
        userReq.setEmail("email@mail.ru");
        userReq.setPassword("<PASSWORD>");
        userReq.setPhoneNumber("+71230456789");

        User user = new User();
        user.setId(1L);
        user.setRole(Role.ADMIN);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResp result = userService.createUser(userReq);

        assertEquals(user.getId(), result.getId());
    }

    @Test
    void getUser() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        UserResp result = userService.getUser(user.getId());

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getRole(), result.getRole());
    }

    @Test
    void getUserFromDB() {
    }

    @Test
    void updateUser() {
        UserReq userReq = new UserReq();
        userReq.setEmail("email@mail.ru");
        userReq.setPassword("200000");
        userReq.setRole(Role.CONTRACTOR);

        User user = new User();
        user.setId(1L);
        user.setEmail("oldEmail@mail.ru");
        user.setRole(Role.DESIGNER);
        user.setPassword("100000");
        user.setPhoneNumber("+89006005040");

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.encode(any(String.class))).thenReturn(userReq.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResp userResp = userService.updateUser(user.getId(), userReq);

        assertEquals(user.getId(), userResp.getId());
        assertEquals(user.getEmail(), userReq.getEmail());
        assertEquals(user.getPassword(), userReq.getPassword());
        assertEquals(user.getPhoneNumber(), userResp.getPhoneNumber());
    }

    @Test
    void deleteUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email@mail.ru");
        user.setRole(Role.ADMIN);
        user.setPassword("100000");
        user.setPhoneNumber("+89006005040");

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        userService.deleteUser(user.getId());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(user.getStatus(), CommonStatus.DELETED);
    }

    @Test
    void getAllUsers() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "name";
        String filter = "man";
        Sort.Direction order = Sort.Direction.ASC;

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("e1mail@mail.ru");
        user1.setFirstName("ivan");
        user1.setLastName("ivanov");
        user1.setPosition("designer");
        user1.setRole(Role.CONTRACTOR);

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("e2mail@mail.ru");
        user2.setFirstName("man");
        user2.setLastName("manov");
        user2.setPosition("engineer");
        user2.setRole(Role.DEVELOPER);

        User user3 = new User();
        user3.setId(3L);
        user3.setEmail("e3mail@mail.ru");
        user3.setFirstName("petr");
        user3.setLastName("petrov");
        user3.setPosition("manager");
        user3.setRole(Role.DESIGNER);

        List<User> filteredUsers = new ArrayList<>();

        when(userRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(CommonStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<User> allUsers = List.of(user1, user2, user3);

                    filteredUsers.addAll(allUsers.stream()
                            .filter(user -> (user.getFirstName().toLowerCase().contains(filter)) ||
                                    (user.getLastName().toLowerCase().contains(filter)) ||
                                    (user.getPosition().toLowerCase().contains(filter)))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredUsers, invocation.getArgument(0), filteredUsers.size());
                });

        Page<UserResp> result = userService.getAllUsers(page, sizePerPage, sort, order, filter);
        assertEquals(filteredUsers.size(), result.getTotalElements());
        assertEquals(filteredUsers.get(0).getId(), user2.getId());
        assertEquals(filteredUsers.get(1).getId(), user3.getId());
    }

    @Test
    void getUserByEmail() {
    }

    @Test
    void addUserToCompany() {
        User user = new User();
        user.setId(1L);

        Company company = new Company();
        company.setId(1L);
        company.setUsers(new ArrayList<>());

        UserToCompanyReq userToCompanyReq = new UserToCompanyReq();
        userToCompanyReq.setCompanyId(company.getId());
        userToCompanyReq.setUserId(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(companyService.getCompanyFromDB(company.getId())).thenReturn(company);

        userService.addUserToCompany(userToCompanyReq);

        verify(companyService, times(1)).updateCompanyData(any(Company.class));
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(user.getCompany(), company);
    }

    @Test
    void getUsersByCompany() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "name";
        Sort.Direction order = Sort.Direction.ASC;

        Company company = new Company();
        company.setId(1L);
        company.setStatus(CommonStatus.CREATED);
        Company company2 = new Company();
        company2.setId(2L);

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("e1mail@mail.ru");
        user1.setFirstName("ivan");
        user1.setLastName("ivanov");
        user1.setPosition("designer");
        user1.setRole(Role.CONTRACTOR);
        user1.setCompany(company);

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("e2mail@mail.ru");
        user2.setFirstName("man");
        user2.setLastName("manov");
        user2.setPosition("engineer");
        user2.setRole(Role.DEVELOPER);
        user2.setCompany(company);

        User user3 = new User();
        user3.setId(3L);
        user3.setEmail("e3mail@mail.ru");
        user3.setFirstName("petr");
        user3.setLastName("petrov");
        user3.setPosition("manager");
        user3.setRole(Role.DESIGNER);
        user3.setCompany(company2);

        List<User> filteredUsers = new ArrayList<>();

        when(companyService.getCompanyFromDB(company.getId())).thenReturn(company);

        when(userRepository.findAllByCompany(any(Pageable.class), anyLong()))
                .thenAnswer(invocation -> {
                    List<User> allUsers = List.of(user1, user2, user3);

                    filteredUsers.addAll(allUsers.stream()
                            .filter(user -> Objects.equals(user.getCompany().getId(), company.getId()))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredUsers, invocation.getArgument(0), filteredUsers.size());
                });

        Page<UserResp> result = userService.getUsersByCompany(company.getId(), page, sizePerPage, sort, order);
        assertEquals(filteredUsers.size(), result.getTotalElements());
        assertEquals(filteredUsers.get(0).getId(), user1.getId());
        assertEquals(filteredUsers.get(1).getId(), user2.getId());
    }
}