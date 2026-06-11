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
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateInvitationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.TokenRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitation;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationInvitationRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationInvitationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationInvitationRepository invitationRepository;

    @Autowired
    OrganizationMemberRepository memberRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    Account adminAccount;
    Account ownerAccount;
    Account inviteeAccount;
    Account authorizedMemberAccount;
    Organization org;

    String adminToken;
    String ownerToken;
    String inviteeToken;
    String authorizedMemberToken;

    @BeforeEach
    void setup() throws Exception {
        invitationRepository.deleteAll();
        memberRepository.deleteAll();
        organizationRepository.deleteAll();
        accountPermissionRepository.deleteAll();
        permissionRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder().email("admin@tp.com").password(encoded).roles(java.util.Set.of(Role.ADMIN)).status(AccountStatus.ACTIVE).build());
        ownerAccount = accountRepository.saveAndFlush(Account.builder().email("owner@tp.com").password(encoded).roles(java.util.Set.of(Role.ORGANIZER)).status(AccountStatus.ACTIVE).build());
        inviteeAccount = accountRepository.saveAndFlush(Account.builder().email("invitee@tp.com").password(encoded).roles(java.util.Set.of(Role.ORGANIZER)).status(AccountStatus.ACTIVE).build());
        authorizedMemberAccount = accountRepository.saveAndFlush(Account.builder().email("auth@tp.com").password(encoded).roles(java.util.Set.of(Role.ORGANIZER)).status(AccountStatus.ACTIVE).build());

        adminToken = login("admin@tp.com", rawPassword);
        ownerToken = login("owner@tp.com", rawPassword);
        inviteeToken = login("invitee@tp.com", rawPassword);
        authorizedMemberToken = login("auth@tp.com", rawPassword);

        org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Org").slug("test-org").ownerAccountId(ownerAccount.getId()).status(OrganizationStatus.ACTIVE).build());

        Permission invitePerm = permissionRepository.saveAndFlush(new Permission(PermissionConstants.ORG_MEMBER_INVITE, "Invite Member", PermissionScope.ORGANIZATION, "Invite", "Users"));
        accountPermissionRepository.saveAndFlush(AccountPermission.builder().accountId(authorizedMemberAccount.getId()).organizationId(org.getId()).permission(invitePerm).isActive(true).build());
    }

    @Test
    void owner_can_create_invitation() throws Exception {
        CreateInvitationRequest req = new CreateInvitationRequest(inviteeAccount.getId());

        mockMvc.perform(post("/api/partner/organizations/" + org.getId() + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inviteeAccountId").value(inviteeAccount.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void authorized_member_can_create_invitation() throws Exception {
        CreateInvitationRequest req = new CreateInvitationRequest(inviteeAccount.getId());

        mockMvc.perform(post("/api/partner/organizations/" + org.getId() + "/invitations")
                        .header("Authorization", "Bearer " + authorizedMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void cannot_invite_buyer() throws Exception {
        Account buyer = accountRepository.saveAndFlush(Account.builder().email("buyer@tp.com").password(passwordEncoder.encode("Password123!")).roles(java.util.Set.of(Role.BUYER)).status(AccountStatus.ACTIVE).build());
        CreateInvitationRequest req = new CreateInvitationRequest(buyer.getId());

        mockMvc.perform(post("/api/partner/organizations/" + org.getId() + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INVITEE_ROLE"));
    }

    @Test
    void validate_token_returns_details() throws Exception {
        OrganizationInvitation invite = invitationRepository.saveAndFlush(OrganizationInvitation.builder()
                .organization(org).inviteeAccountId(inviteeAccount.getId()).invitedBy(ownerAccount.getId())
                .token("test-token").status(OrganizationInvitationStatus.PENDING).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).build());

        mockMvc.perform(get("/api/partner/organizations/invitations/validate?token=test-token")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgName").value("Test Org"));
    }

    @Test
    void accept_invitation_creates_member() throws Exception {
        OrganizationInvitation invite = invitationRepository.saveAndFlush(OrganizationInvitation.builder()
                .organization(org).inviteeAccountId(inviteeAccount.getId()).invitedBy(ownerAccount.getId())
                .token("test-token").status(OrganizationInvitationStatus.PENDING).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).build());

        mockMvc.perform(post("/api/partner/organizations/invitations/accept?token=test-token")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk());

        OrganizationInvitation updatedInvite = invitationRepository.findById(invite.getId()).orElseThrow();
        assertThat(updatedInvite.getStatus()).isEqualTo(OrganizationInvitationStatus.ACCEPTED);

        boolean isMember = memberRepository.existsByOrganizationIdAndAccountIdAndStatus(org.getId(), inviteeAccount.getId(), OrganizationMemberStatus.ACTIVE);
        assertThat(isMember).isTrue();
    }

    @Test
    void reject_invitation_updates_status() throws Exception {
        OrganizationInvitation invite = invitationRepository.saveAndFlush(OrganizationInvitation.builder()
                .organization(org).inviteeAccountId(inviteeAccount.getId()).invitedBy(ownerAccount.getId())
                .token("test-token").status(OrganizationInvitationStatus.PENDING).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).build());

        mockMvc.perform(post("/api/partner/organizations/invitations/reject?token=test-token")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk());

        OrganizationInvitation updatedInvite = invitationRepository.findById(invite.getId()).orElseThrow();
        assertThat(updatedInvite.getStatus()).isEqualTo(OrganizationInvitationStatus.REJECTED);
    }

    @Test
    void list_org_invitations_and_my_invitations() throws Exception {
        invitationRepository.saveAndFlush(OrganizationInvitation.builder()
                .organization(org).inviteeAccountId(inviteeAccount.getId()).invitedBy(ownerAccount.getId())
                .token("token1").status(OrganizationInvitationStatus.PENDING).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).build());

        mockMvc.perform(get("/api/partner/organizations/" + org.getId() + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/partner/organizations/invitations/my")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
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
