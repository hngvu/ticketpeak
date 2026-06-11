package io.qzz.hoangvu.ticketpeak.api.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.CreatePermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.GrantPermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.model.AccountPermission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Permission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.PermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.service.PermissionConstants;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PermissionControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    Account adminAccount;
    Account ownerAccount;
    Account memberAccount;
    Account otherAccount;
    Organization organization;
    String adminToken;
    String ownerToken;
    String memberToken;

    @BeforeEach
    void setup() throws Exception {
        accountPermissionRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        permissionRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ADMIN))
                .status(AccountStatus.ACTIVE)
                .build());

        ownerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("owner@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ORGANIZER))
                .status(AccountStatus.ACTIVE)
                .build());

        memberAccount = accountRepository.saveAndFlush(Account.builder()
                .email("member@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ORGANIZER))
                .status(AccountStatus.ACTIVE)
                .build());

        otherAccount = accountRepository.saveAndFlush(Account.builder()
                .email("stranger@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.BUYER))
                .status(AccountStatus.ACTIVE)
                .build());

        organization = organizationRepository.saveAndFlush(Organization.builder()
                .name("Peak Events")
                .slug("peak-events")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        organizationMemberRepository.saveAndFlush(OrganizationMember.builder()
                .organization(organization)
                .accountId(memberAccount.getId())
                .status(OrganizationMemberStatus.ACTIVE)
                .build());

        adminToken = login("admin@ticketpeak.com", rawPassword);
        ownerToken = login("owner@ticketpeak.com", rawPassword);
        memberToken = login("member@ticketpeak.com", rawPassword);

        // Seed some permissions
        permissionRepository.saveAndFlush(Permission.builder()
                .code(PermissionConstants.ORG_MANAGE_PERMISSIONS)
                .name("Manage Org Permissions")
                .scope(PermissionScope.ORGANIZATION)
                .build());

        permissionRepository.saveAndFlush(Permission.builder()
                .code("ORG_CREATE_EVENT")
                .name("Create Org Event")
                .scope(PermissionScope.ORGANIZATION)
                .build());

        permissionRepository.saveAndFlush(Permission.builder()
                .code("PLATFORM_MANAGE_USERS")
                .name("Manage Platform Users")
                .scope(PermissionScope.PLATFORM)
                .build());
    }

    @Test
    void createPermission_as_admin_succeeds() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "ORG_EDIT_EVENT", "Edit Org Event", PermissionScope.ORGANIZATION, "edit", "event"
        );

        mockMvc.perform(post("/api/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ORG_EDIT_EVENT"))
                .andExpect(jsonPath("$.data.scope").value("ORGANIZATION"));

        assertThat(permissionRepository.existsById("ORG_EDIT_EVENT")).isTrue();
    }

    @Test
    void createPermission_as_non_admin_is_forbidden() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "ORG_DELETE_EVENT", "Delete Org Event", PermissionScope.ORGANIZATION, "delete", "event"
        );

        mockMvc.perform(post("/api/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPermissions_returns_list_and_supports_filtering() throws Exception {
        mockMvc.perform(get("/api/permissions")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(get("/api/permissions?scope=ORGANIZATION")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void grantPermission_by_org_owner_to_active_member_succeeds() throws Exception {
        GrantPermissionRequest request = new GrantPermissionRequest(memberAccount.getId(), "ORG_CREATE_EVENT");

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accountId").value(memberAccount.getId().toString()))
                .andExpect(jsonPath("$.data.permission.code").value("ORG_CREATE_EVENT"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    void grantPermission_prevents_duplicate_active_grants() throws Exception {
        GrantPermissionRequest request = new GrantPermissionRequest(memberAccount.getId(), "ORG_CREATE_EVENT");

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PERMISSION_ALREADY_GRANTED"));
    }

    @Test
    void grantPermission_reactivates_previously_revoked_grant() throws Exception {
        Permission perm = permissionRepository.findById("ORG_CREATE_EVENT").orElseThrow();
        AccountPermission revoked = accountPermissionRepository.saveAndFlush(AccountPermission.builder()
                .accountId(memberAccount.getId())
                .organizationId(organization.getId())
                .permission(perm)
                .isActive(false)
                .grantedAt(Instant.now().minusSeconds(1000))
                .grantedBy(ownerAccount.getId())
                .revokedBy(ownerAccount.getId())
                .updatedAt(Instant.now())
                .build());

        GrantPermissionRequest request = new GrantPermissionRequest(memberAccount.getId(), "ORG_CREATE_EVENT");

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(revoked.getId()))
                .andExpect(jsonPath("$.data.isActive").value(true));

        AccountPermission active = accountPermissionRepository.findById(revoked.getId()).orElseThrow();
        assertThat(active.getIsActive()).isTrue();
        assertThat(active.getRevokedBy()).isNull();
    }

    @Test
    void grantPermission_to_non_member_is_bad_request() throws Exception {
        GrantPermissionRequest request = new GrantPermissionRequest(otherAccount.getId(), "ORG_CREATE_EVENT");

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_ORGANIZATION_MEMBER"));
    }

    @Test
    void grant_and_revoke_by_authorized_organizer_with_manage_perm_succeeds() throws Exception {
        Permission managePerm = permissionRepository.findById(PermissionConstants.ORG_MANAGE_PERMISSIONS).orElseThrow();
        accountPermissionRepository.saveAndFlush(AccountPermission.builder()
                .accountId(memberAccount.getId())
                .organizationId(organization.getId())
                .permission(managePerm)
                .isActive(true)
                .grantedAt(Instant.now())
                .grantedBy(ownerAccount.getId())
                .updatedAt(Instant.now())
                .build());

        // Member now has ORG_MANAGE_PERMISSIONS, so member can grant ORG_CREATE_EVENT to owner (or self)
        GrantPermissionRequest request = new GrantPermissionRequest(ownerAccount.getId(), "ORG_CREATE_EVENT");

        mockMvc.perform(post("/api/organizations/" + organization.getId() + "/permissions")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Now revoke it
        mockMvc.perform(delete("/api/organizations/" + organization.getId() + "/permissions")
                        .param("accountId", ownerAccount.getId().toString())
                        .param("permissionCode", "ORG_CREATE_EVENT")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        List<AccountPermission> active = accountPermissionRepository.findByAccountIdAndOrganizationIdAndIsActiveTrue(
                ownerAccount.getId(), organization.getId()
        );
        assertThat(active).isEmpty();
    }

    @Test
    void getAccountPermissions_lists_active_permissions() throws Exception {
        Permission perm = permissionRepository.findById("ORG_CREATE_EVENT").orElseThrow();
        accountPermissionRepository.saveAndFlush(AccountPermission.builder()
                .accountId(memberAccount.getId())
                .organizationId(organization.getId())
                .permission(perm)
                .isActive(true)
                .grantedAt(Instant.now())
                .grantedBy(ownerAccount.getId())
                .updatedAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/organizations/" + organization.getId() + "/permissions/" + memberAccount.getId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].permission.code").value("ORG_CREATE_EVENT"));
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private record LoginPayload(String email, String password) {
    }
}
