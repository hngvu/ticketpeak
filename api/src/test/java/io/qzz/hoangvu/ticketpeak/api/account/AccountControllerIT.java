package io.qzz.hoangvu.ticketpeak.api.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
        seedLocations();
    }

    @Test
    void register_creates_account_with_hashed_password_and_defaults() throws Exception {
        String rawPassword = "Secret123!";
        String response = mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterAccountPayload(
                                "NEWUSER@EXAMPLE.COM",
                                rawPassword,
                                "New",
                                "User",
                                "https://cdn.example.com/avatar.png",
                                LocalDate.of(1995, 5, 20),
                                "FEMALE",
                                1,
                                "VN"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.role").value("BUYER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        Account account = accountRepository.findByEmailIgnoreCase("newuser@example.com").orElseThrow();

        assertThat(passwordEncoder.matches(rawPassword, account.getPassword())).isTrue();
        assertThat(body.path("data").path("password").isMissingNode()).isTrue();
        assertThat(account.getFirstName()).isEqualTo("New");
        assertThat(account.getLastName()).isEqualTo("User");
    }

    @Test
    void register_rejects_duplicate_email() throws Exception {
        createRegisteredAccount("duplicate@example.com", "Secret123!");

        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterAccountPayload(
                                "DUPLICATE@EXAMPLE.COM",
                                "Secret123!",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void me_returns_current_profile_and_update_persists_allowed_fields_only() throws Exception {
        createRegisteredAccount("profile@example.com", "Secret123!");
        String accessToken = login("profile@example.com", "Secret123!");

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/accounts/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfilePayload(
                                "Updated",
                                "Person",
                                "https://cdn.example.com/new-avatar.png",
                                LocalDate.of(1994, 4, 12),
                                "MALE",
                                1,
                                "VN"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.cityId").value(1))
                .andExpect(jsonPath("$.data.role").value("BUYER"));

        mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.gender").value("MALE"));

        Account account = accountRepository.findByEmailIgnoreCase("profile@example.com").orElseThrow();
        assertThat(account.getEmail()).isEqualTo("profile@example.com");
        assertThat(passwordEncoder.matches("Secret123!", account.getPassword())).isTrue();
        assertThat(account.getRole()).isEqualTo(Role.BUYER);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getFirstName()).isEqualTo("Updated");
        assertThat(account.getAvatarUrl()).isEqualTo("https://cdn.example.com/new-avatar.png");
        assertThat(account.getCityId()).isEqualTo(1);
        assertThat(account.getCountryCode()).isEqualTo("VN");

        String maliciousBody = """
                {
                  "firstName": "Second",
                  "email": "hacker@example.com",
                  "password": "NotAllowed123!",
                  "role": "ADMIN",
                  "status": "BANNED",
                  "metadata": {"isAdmin": true}
                }
                """;
        mockMvc.perform(patch("/api/accounts/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Second"))
                .andExpect(jsonPath("$.data.email").value("profile@example.com"));

        Account updated = accountRepository.findByEmailIgnoreCase("profile@example.com").orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("profile@example.com");
        assertThat(updated.getPassword()).isEqualTo(account.getPassword());
        assertThat(updated.getRole()).isEqualTo(Role.BUYER);
        assertThat(updated.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void validation_errors_return_standard_shape() throws Exception {
        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterAccountPayload(
                                "not-an-email",
                                "short",
                                null,
                                null,
                                null,
                                LocalDate.now().plusDays(1),
                                "alien",
                                0,
                                "VNM"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.email[0]").value("must be a valid email address"))
                .andExpect(jsonPath("$.errors.password[0]").value("must be between 8 and 72 characters"))
                .andExpect(jsonPath("$.errors.birthDate[0]").value("must be a past date"))
                .andExpect(jsonPath("$.errors.gender[0]").value("must be MALE, FEMALE, or UNKNOWN"))
                .andExpect(jsonPath("$.errors.cityId[0]").value("must be greater than 0"));
    }

    @Test
    void unauthorized_requests_are_rejected() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Nope"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private Account createRegisteredAccount(String email, String password) {
        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName("Ticket")
                .lastName("Peak")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private void seedLocations() {
        jdbcTemplate.update("""
                INSERT INTO country (code, name, slug, currency, is_active)
                VALUES ('VN', 'Vietnam', 'vietnam', 'VND', true)
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO city (id, name, slug, latitude, longitude, timezone, is_active, country_code)
                VALUES (1, 'Ho Chi Minh City', 'ho-chi-minh-city', '10.8231', '106.6297', 'Asia/Ho_Chi_Minh', true, 'VN')
                ON CONFLICT (id) DO NOTHING
                """);
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

    private record RegisterAccountPayload(
            String email,
            String password,
            String firstName,
            String lastName,
            String avatarUrl,
            LocalDate birthDate,
            String gender,
            Integer cityId,
            String countryCode
    ) {
    }

    private record UpdateProfilePayload(
            String firstName,
            String lastName,
            String avatarUrl,
            LocalDate birthDate,
            String gender,
            Integer cityId,
            String countryCode
    ) {
    }

    private record LoginPayload(String email, String password) {
    }
}
