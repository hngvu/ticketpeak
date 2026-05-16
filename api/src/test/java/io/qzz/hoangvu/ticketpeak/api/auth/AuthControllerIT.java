package io.qzz.hoangvu.ticketpeak.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.security.JwtService;
import io.qzz.hoangvu.ticketpeak.api.security.RefreshTokenStore;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Autowired
    RefreshTokenStore refreshTokenStore;

    @BeforeEach
    void clearAccounts() {
        accountRepository.deleteAll();
    }

    @Test
    void login_returns_tokens_and_whitelists_refresh_token() throws Exception {
        Account account = createAccount("buyer@example.com", "Secret123!");

        JsonNode response = performLogin("buyer@example.com", "Secret123!");
        String refreshToken = response.path("data").path("refreshToken").asText();
        JwtService.ParsedRefreshToken parsedRefreshToken = jwtService.parseRefreshToken(refreshToken);

        assertThat(refreshTokenStore.matches(account.getId(), parsedRefreshToken.refreshTokenId())).isTrue();
        assertThat(response.path("success").asBoolean()).isTrue();
        assertThat(response.path("data").path("accessToken").asText()).isNotBlank();
    }

    @Test
    void new_login_overwrites_previous_refresh_token() throws Exception {
        Account account = createAccount("device-switch@example.com", "Secret123!");

        JsonNode firstLogin = performLogin("device-switch@example.com", "Secret123!");
        String firstRefreshToken = firstLogin.path("data").path("refreshToken").asText();
        JwtService.ParsedRefreshToken firstParsed = jwtService.parseRefreshToken(firstRefreshToken);

        JsonNode secondLogin = performLogin("device-switch@example.com", "Secret123!");
        String secondRefreshToken = secondLogin.path("data").path("refreshToken").asText();
        JwtService.ParsedRefreshToken secondParsed = jwtService.parseRefreshToken(secondRefreshToken);

        assertThat(refreshTokenStore.matches(account.getId(), firstParsed.refreshTokenId())).isFalse();
        assertThat(refreshTokenStore.matches(account.getId(), secondParsed.refreshTokenId())).isTrue();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequestPayload(firstRefreshToken))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequestPayload(secondRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void logout_deletes_refresh_token_from_redis() throws Exception {
        createAccount("logout@example.com", "Secret123!");
        performLogin("logout@example.com", "Secret123!");
        JsonNode login = performLogin("logout@example.com", "Secret123!");
        String refreshToken = login.path("data").path("refreshToken").asText();
        JwtService.ParsedRefreshToken parsedRefreshToken = jwtService.parseRefreshToken(refreshToken);
        UUID accountId = parsedRefreshToken.accountId();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequestPayload(refreshToken))))
                .andExpect(status().isOk());

        assertThat(refreshTokenStore.matches(accountId, parsedRefreshToken.refreshTokenId())).isFalse();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequestPayload(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protected_endpoint_requires_access_token() throws Exception {
        createAccount("protected@example.com", "Secret123!");
        JsonNode login = performLogin("protected@example.com", "Secret123!");
        String accessToken = login.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("protected@example.com"))
                .andExpect(jsonPath("$.data.role").value("BUYER"));
    }

    private JsonNode performLogin(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequestPayload(email, password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private Account createAccount(String email, String rawPassword) {
        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Ticket")
                .lastName("Peak")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private record LoginRequestPayload(String email, String password) {
    }

    private record RefreshRequestPayload(String refreshToken) {
    }
}
