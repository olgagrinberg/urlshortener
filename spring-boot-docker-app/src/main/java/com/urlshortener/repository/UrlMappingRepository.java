package com.urlshortener.repository;
import com.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    // Find by full URL
    Optional<UrlMapping> findByFullUrl(String fullUrl);
    boolean existsByFullUrl(String fullUrl);

    // Find by short URL
    Optional<UrlMapping> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
}