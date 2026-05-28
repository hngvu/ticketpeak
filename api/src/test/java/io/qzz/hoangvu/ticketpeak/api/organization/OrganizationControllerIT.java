package io.qzz.hoangvu.ticketpeak.api.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    Account adminAccount;
    Account ownerAccount;
    Account otherAccount;
    String adminToken;
    String ownerToken;
    String otherToken;

    @BeforeEach
    void setup() throws Exception {
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@ticketpeak.com")
                .password(encoded)
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());

        ownerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("owner@ticketpeak.com")
                .password(encoded)
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        otherAccount = accountRepository.saveAndFlush(Account.builder()
                .email("other@ticketpeak.com")
                .password(encoded)
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        adminToken = login("admin@ticketpeak.com", rawPassword);
        ownerToken = login("owner@ticketpeak.com", rawPassword);
        otherToken = login("other@ticketpeak.com", rawPassword);
    }

    @Test
    void admin_can_create_organization() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Peak Events", ownerAccount.getId(), "Bio", null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/ops/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Peak Events"))
                .andExpect(jsonPath("$.data.slug").value("peak-events"));

        assertThat(organizationRepository.existsBySlug("peak-events")).isTrue();
    }

    @Test
    void non_admin_cannot_create_organization() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Peak Events", ownerAccount.getId(), null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/ops/organizations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void slug_collision_handled() throws Exception {
        CreateOrganizationRequest request1 = new CreateOrganizationRequest(
                "Test Org", ownerAccount.getId(), null, null, null, null, null, null, null
        );
        CreateOrganizationRequest request2 = new CreateOrganizationRequest(
                "Test Org", ownerAccount.getId(), null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/ops/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").value("test-org"));

        mockMvc.perform(post("/api/ops/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").value("test-org-1"));
    }

    @Test
    void admin_can_search_organizations() throws Exception {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("FindMe")
                .slug("findme")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/ops/organizations?name=find")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("FindMe"));
    }

    @Test
    void admin_can_get_by_id() throws Exception {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("GetMe")
                .slug("getme")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/ops/organizations/" + org.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("GetMe"));
    }

    @Test
    void anyone_can_get_by_id() throws Exception {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Public Org")
                .slug("public-org")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/organizations/" + org.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Public Org"));
    }

    @Test
    void owner_can_update_organization() throws Exception {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Old Name")
                .slug("old-name")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "New Name", "New Bio", null, null, null, null, null, null
        );

        mockMvc.perform(put("/api/partner/organizations/" + org.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.bio").value("New Bio"));
    }

    @Test
    void non_owner_cannot_update() throws Exception {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Old Name")
                .slug("old-name")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Hacked Name", null, null, null, null, null, null, null
        );

        mockMvc.perform(put("/api/partner/organizations/" + org.getId())
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
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
