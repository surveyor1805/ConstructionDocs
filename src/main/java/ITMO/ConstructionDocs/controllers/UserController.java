package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.UserReq;
import ITMO.ConstructionDocs.model.dto.request.UserToCompanyReq;
import ITMO.ConstructionDocs.model.dto.response.UserResp;
import ITMO.ConstructionDocs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static ITMO.ConstructionDocs.constants.Constants.USER;

@Tag(name = "Users")
@RestController
@RequestMapping(USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    @Operation(summary = "Create user")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResp createUser(@RequestBody UserReq request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResp getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by id")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResp updateUser(@PathVariable Long id, @RequestBody UserReq request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResp> getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer sizePerPage,
                                      @RequestParam(defaultValue = "lastName") String sort,
                                      @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                      @RequestParam(required = false) String filter) {
        return userService.getAllUsers(page, sizePerPage, sort, order, filter);
    }

    @PostMapping("/setUserToCompany")
    @Operation(summary = "set User to Company")
    @PreAuthorize("hasRole('ADMIN')")
    public void addUserToCompany(@RequestBody @Valid UserToCompanyReq request) {
        userService.addUserToCompany(request);
    }

    @GetMapping("/allByCompanyId")
    @Operation(summary = "Get list of users by Company Id")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResp> getUsersByCompany(@RequestParam Long companyId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer sizePerPage,
                                            @RequestParam(defaultValue = "lastName") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return userService.getUsersByCompany(companyId, page, sizePerPage, sort, order);
    }

}
