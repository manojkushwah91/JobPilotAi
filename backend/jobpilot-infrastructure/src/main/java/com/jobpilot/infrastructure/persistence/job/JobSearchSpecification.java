package com.jobpilot.infrastructure.persistence.job;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JobSearchSpecification {

    public static Specification<JobListingEntity> withFilters(
            String query, List<String> skills, String employmentType,
            String experienceLevel, String industry, String location,
            Integer salaryMin, Integer salaryMax, String postedWithin) {
        return (root, cq, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.isTrue(root.get("isActive")));

            if (query != null && !query.isBlank()) {
                var pattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("industry")), pattern)
                ));
            }

            if (skills != null && !skills.isEmpty()) {
                for (String skill : skills) {
                    predicates.add(cb.like(cb.lower(root.get("skills")), "%\"" + skill.toLowerCase() + "\"%"));
                }
            }

            if (employmentType != null && !employmentType.isBlank()) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            if (experienceLevel != null && !experienceLevel.isBlank()) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("industry")), "%" + industry.toLowerCase() + "%"));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (postedWithin != null && !postedWithin.isBlank()) {
                var cutoff = switch (postedWithin) {
                    case "24h" -> Instant.now().minusSeconds(86400);
                    case "3d" -> Instant.now().minusSeconds(259200);
                    case "7d" -> Instant.now().minusSeconds(604800);
                    case "14d" -> Instant.now().minusSeconds(1209600);
                    case "30d" -> Instant.now().minusSeconds(2592000);
                    default -> null;
                };
                if (cutoff != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("postedAt"), cutoff));
                }
            }

            cq.orderBy(cb.desc(root.get("postedAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
