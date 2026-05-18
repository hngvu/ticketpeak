package io.qzz.hoangvu.ticketpeak.api.organization.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationSearchParams;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AccountRepository accountRepository;
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            AccountRepository accountRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, AuthenticatedAccount principal) {
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only platform admins can create organizations");
        }

        Account owner = accountRepository.findById(request.ownerAccountId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Owner account not found"));

        if (owner.getRole() != Role.ORGANIZER && owner.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OWNER_ROLE", "Owner account must be an organizer or admin");
        }

        String slug = generateUniqueSlug(request.name());

        Organization org = Organization.builder()
                .name(request.name())
                .slug(slug)
                .bio(request.bio())
                .logoUrl(request.logoUrl())
                .websiteUrl(request.websiteUrl())
                .email(request.email())
                .phone(request.phone())
                .cityId(request.cityId())
                .countryCode(request.countryCode())
                .ownerAccountId(owner.getId())
                .status(OrganizationStatus.ACTIVE)
                .build();

        Organization savedOrg = organizationRepository.save(org);

        OrganizationMember member = OrganizationMember.builder()
                .organization(savedOrg)
                .accountId(owner.getId())
                .status(OrganizationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        organizationMemberRepository.save(member);

        return OrganizationResponse.from(savedOrg);
    }

    @Transactional
    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request, AuthenticatedAccount principal) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        if (!org.getOwnerAccountId().equals(principal.accountId())) {
            throw new AccessDeniedException("Only the organization owner can update organization details");
        }

        return updateFieldsAndSave(org, request);
    }

    @Transactional
    public OrganizationResponse adminUpdateOrganization(UUID id, UpdateOrganizationRequest request, AuthenticatedAccount principal) {
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only platform admins can perform administrative updates");
        }

        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        return updateFieldsAndSave(org, request);
    }

    private OrganizationResponse updateFieldsAndSave(Organization org, UpdateOrganizationRequest request) {
        org.setName(request.name());
        org.setBio(request.bio());
        org.setLogoUrl(request.logoUrl());
        org.setWebsiteUrl(request.websiteUrl());
        org.setEmail(request.email());
        org.setPhone(request.phone());
        org.setCityId(request.cityId());
        org.setCountryCode(request.countryCode());

        return OrganizationResponse.from(organizationRepository.save(org));
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));
        return OrganizationResponse.from(org);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id, AuthenticatedAccount principal) {
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only platform admins can access this endpoint");
        }
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));
        return OrganizationResponse.from(org);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationResponse> searchOrganizations(OrganizationSearchParams params, Pageable pageable, AuthenticatedAccount principal) {
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only platform admins can search organizations");
        }
        Page<Organization> orgs = organizationRepository.searchOrganizations(params.name(), params.status(), pageable);
        return orgs.map(OrganizationResponse::from);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = slugify(name);
        if (baseSlug.isBlank()) {
            baseSlug = "org"; // fallback
        }
        String slug = baseSlug;
        int counter = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }
}
