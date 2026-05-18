package io.qzz.hoangvu.ticketpeak.api.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.AccountPermission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Permission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.PermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.service.PermissionConstants;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.MemberRequest;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationMemberControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired OrganizationMemberRepository memberRepository;
    @Autowired PermissionRepository permissionRepository;
    @Autowired AccountPermissionRepository accountPermissionRepository;
    @Autowired PasswordEncoder passwordEncoder;

    Account adminAccount;
    Account ownerAccount;
    Account memberAccount;
    Account authorizedAccount;
    Account otherAccount;
    Organization org;

    String adminToken;
    String ownerToken;
    String memberToken;
    String authorizedToken;
    String otherToken;

    @BeforeEach
    void setup() throws Exception {
        memberRepository.deleteAll();
        organizationRepository.deleteAll();
        accountPermissionRepository.deleteAll();
        permissionRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder().email("admin@tp.com").password(encoded).role(Role.ADMIN).status(AccountStatus.ACTIVE).build());
        ownerAccount = accountRepository.saveAndFlush(Account.builder().email("owner@tp.com").password(encoded).role(Role.ORGANIZER).status(AccountStatus.ACTIVE).build());
        memberAccount = accountRepository.saveAndFlush(Account.builder().email("member@tp.com").password(encoded).role(Role.ORGANIZER).status(AccountStatus.ACTIVE).build());
        authorizedAccount = accountRepository.saveAndFlush(Account.builder().email("auth@tp.com").password(encoded).role(Role.ORGANIZER).status(AccountStatus.ACTIVE).build());
        otherAccount = accountRepository.saveAndFlush(Account.builder().email("other@tp.com").password(encoded).role(Role.ORGANIZER).status(AccountStatus.ACTIVE).build());

        adminToken = login("admin@tp.com", rawPassword);
        ownerToken = login("owner@tp.com", rawPassword);
        memberToken = login("member@tp.com", rawPassword);
        authorizedToken = login("auth@tp.com", rawPassword);
        otherToken = login("other@tp.com", rawPassword);

        org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Org").slug("test-org").ownerAccountId(ownerAccount.getId()).status(OrganizationStatus.ACTIVE).build());

        memberRepository.saveAndFlush(OrganizationMember.builder()
                .organization(org).accountId(ownerAccount.getId()).status(OrganizationMemberStatus.ACTIVE).joinedAt(Instant.now()).build());
        memberRepository.saveAndFlush(OrganizationMember.builder()
                .organization(org).accountId(memberAccount.getId()).status(OrganizationMemberStatus.ACTIVE).joinedAt(Instant.now()).build());
        memberRepository.saveAndFlush(OrganizationMember.builder()
                .organization(org).accountId(authorizedAccount.getId()).status(OrganizationMemberStatus.ACTIVE).joinedAt(Instant.now()).build());

        Permission removePerm = permissionRepository.saveAndFlush(new Permission(PermissionConstants.ORG_MEMBER_REMOVE, "Remove Member", PermissionScope.ORGANIZATION, "Remove", "Users"));
        Permission invitePerm = permissionRepository.saveAndFlush(new Permission(PermissionConstants.ORG_MEMBER_INVITE, "Invite Member", PermissionScope.ORGANIZATION, "Invite", "Users"));

        accountPermissionRepository.saveAndFlush(AccountPermission.builder().accountId(authorizedAccount.getId()).organizationId(org.getId()).permission(removePerm).isActive(true).build());
        accountPermissionRepository.saveAndFlush(AccountPermission.builder().accountId(authorizedAccount.getId()).organizationId(org.getId()).permission(invitePerm).isActive(true).build());
    }

    @Test
    void list_members_success_for_owner_and_member() throws Exception {
        mockMvc.perform(get("/api/organizations/" + org.getId() + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].account.email").isNotEmpty());

        mockMvc.perform(get("/api/organizations/" + org.getId() + "/members")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void list_members_forbidden_for_non_member() throws Exception {
        mockMvc.perform(get("/api/organizations/" + org.getId() + "/members")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_member_status_success() throws Exception {
        mockMvc.perform(get("/api/organizations/" + org.getId() + "/members/" + memberAccount.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(memberAccount.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void owner_can_remove_member() throws Exception {
        mockMvc.perform(delete("/api/organizations/" + org.getId() + "/members/" + memberAccount.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        OrganizationMember m = memberRepository.findByOrganizationIdAndAccountId(org.getId(), memberAccount.getId()).orElseThrow();
        assertThat(m.getStatus()).isEqualTo(OrganizationMemberStatus.REMOVED);
    }

    @Test
    void authorized_member_can_remove_member() throws Exception {
        mockMvc.perform(post("/api/organizations/" + org.getId() + "/members/remove")
                        .header("Authorization", "Bearer " + authorizedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MemberRequest(memberAccount.getId()))))
                .andExpect(status().isOk());

        OrganizationMember m = memberRepository.findByOrganizationIdAndAccountId(org.getId(), memberAccount.getId()).orElseThrow();
        assertThat(m.getStatus()).isEqualTo(OrganizationMemberStatus.REMOVED);
    }

    @Test
    void unauthorized_member_cannot_remove() throws Exception {
        mockMvc.perform(delete("/api/organizations/" + org.getId() + "/members/" + authorizedAccount.getId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_remove_owner() throws Exception {
        mockMvc.perform(delete("/api/organizations/" + org.getId() + "/members/" + ownerAccount.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CANNOT_REMOVE_OWNER"));
    }

    @Test
    void member_can_leave() throws Exception {
        mockMvc.perform(post("/api/organizations/" + org.getId() + "/members/leave")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        OrganizationMember m = memberRepository.findByOrganizationIdAndAccountId(org.getId(), memberAccount.getId()).orElseThrow();
        assertThat(m.getStatus()).isEqualTo(OrganizationMemberStatus.LEFT);
    }

    @Test
    void owner_cannot_leave() throws Exception {
        mockMvc.perform(post("/api/organizations/" + org.getId() + "/members/leave")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CANNOT_LEAVE_AS_OWNER"));
    }

    @Test
    void restore_member_reuses_unique_row() throws Exception {
        OrganizationMember m = memberRepository.findByOrganizationIdAndAccountId(org.getId(), memberAccount.getId()).orElseThrow();
        m.setStatus(OrganizationMemberStatus.REMOVED);
        memberRepository.saveAndFlush(m);

        mockMvc.perform(post("/api/organizations/" + org.getId() + "/members/" + memberAccount.getId() + "/restore")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        OrganizationMember restored = memberRepository.findByOrganizationIdAndAccountId(org.getId(), memberAccount.getId()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(OrganizationMemberStatus.ACTIVE);
        assertThat(restored.getId()).isEqualTo(m.getId()); // Reused unique row
    }

    @Test
    void validation_failure_returns_validation_failed_shape() throws Exception {
        mockMvc.perform(post("/api/organizations/" + org.getId() + "/members/remove")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.accountId").exists());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private record LoginPayload(String email, String password) {}
}
