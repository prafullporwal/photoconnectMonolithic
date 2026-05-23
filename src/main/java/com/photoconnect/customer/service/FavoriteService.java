package com.photoconnect.customer.service;

import com.photoconnect.customer.domain.Favorite;
import com.photoconnect.customer.dto.FavoriteResponse;
import com.photoconnect.customer.dto.FavoriteStatusResponse;
import com.photoconnect.customer.exception.CustomerExceptions.PhotographerNotFoundException;
import com.photoconnect.customer.repository.FavoriteRepository;
import com.photoconnect.photographer.dto.FeedItemResponse;
import com.photoconnect.photographer.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer favorites — bookmarks on portfolio items.
 *
 * <p>Verification + enrichment were Feign calls in the microservices version;
 * here they're direct calls into {@link PortfolioService}.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository repository;
    private final PortfolioService portfolioService;

    public FavoriteResponse save(UUID customerId, UUID portfolioItemId) {
        var existing = repository.findByCustomerIdAndPortfolioItemId(customerId, portfolioItemId);
        if (existing.isPresent()) {
            Favorite f = existing.get();
            return FavoriteResponse.withoutItem(f.getId(), f.getPortfolioItemId(), f.getCreatedAt());
        }

        // Verify the portfolio item exists before persisting the favorite row.
        // Feign-NotFound → PhotographerNotFoundException in the microservices
        // version; here we just check the Optional from the in-process service.
        if (portfolioService.findAsFeedItem(portfolioItemId).isEmpty()) {
            throw new PhotographerNotFoundException(portfolioItemId);
        }

        Favorite favorite = new Favorite();
        favorite.setCustomerId(customerId);
        favorite.setPortfolioItemId(portfolioItemId);

        try {
            Favorite saved = repository.save(favorite);
            log.info("Customer {} saved portfolio item {}", customerId, portfolioItemId);
            return FavoriteResponse.withoutItem(saved.getId(), saved.getPortfolioItemId(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException e) {
            Favorite winner = repository
                    .findByCustomerIdAndPortfolioItemId(customerId, portfolioItemId)
                    .orElseThrow(() -> e);
            return FavoriteResponse.withoutItem(winner.getId(), winner.getPortfolioItemId(), winner.getCreatedAt());
        }
    }

    public void remove(UUID customerId, UUID portfolioItemId) {
        int removed = repository.deleteByCustomerAndPortfolioItem(customerId, portfolioItemId);
        if (removed > 0) {
            log.info("Customer {} unsaved portfolio item {}", customerId, portfolioItemId);
        }
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(UUID customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::enrich)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(UUID customerId, UUID portfolioItemId) {
        return new FavoriteStatusResponse(
                repository.existsByCustomerIdAndPortfolioItemId(customerId, portfolioItemId));
    }

    private FavoriteResponse enrich(Favorite f) {
        Optional<FeedItemResponse> item = portfolioService.findAsFeedItem(f.getPortfolioItemId());
        return new FavoriteResponse(f.getId(), f.getPortfolioItemId(), item.orElse(null), f.getCreatedAt());
    }
}
