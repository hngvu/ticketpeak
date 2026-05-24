package io.qzz.hoangvu.ticketpeak.api.organization.service;

import io.qzz.hoangvu.ticketpeak.api.account.exception.AccountException;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationSearchParams;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.exception.OrganizationException;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        Account owner = accountRepository.findById(request.ownerAccountId())
                .orElseThrow(AccountException::notFound);

        if (owner.getRole() != Role.ORGANIZER && owner.getRole() != Role.ADMIN) {
            throw OrganizationException.invalidOwnerRole();
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
    @PreAuthorize("@orgSecurity.isOwner(#id)")
    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(OrganizationException::notFound);

        return updateFieldsAndSave(org, request);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse adminUpdateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(OrganizationException::notFound);

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
    @PostAuthorize("returnObject.status() == T(io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus).ACTIVE or hasRole('ADMIN') or @orgSecurity.isOwnerOrMember(#id)")
    public OrganizationResponse getOrganization(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(OrganizationException::notFound);
        return OrganizationResponse.from(org);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrganizationResponse> searchOrganizations(OrganizationSearchParams params, Pageable pageable) {
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
