package com.urlshortener.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "url_mapping",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"full_url"}),
                @UniqueConstraint(columnNames = {"short_url"}),
                @UniqueConstraint(columnNames = {"full_url", "short_url"})
        })
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_url", unique = true)
    @Size(min = 10, max = 2048, message = "Full Url must be between 10 and 2048 characters")
    @URL
    private String fullUrl;

    @Column(name = "short_url", unique = true)
    @Size(max = 10, message = "Short Url must be max 10 characters")
    private String shortUrl;

    @Transient
    private String error;

}
