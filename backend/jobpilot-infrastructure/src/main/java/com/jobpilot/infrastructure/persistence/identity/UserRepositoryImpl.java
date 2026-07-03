package com.jobpilot.infrastructure.persistence.identity;

import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.User;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.infrastructure.event.DomainEventPublishingRepositoryDecorator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final DomainEventPublishingRepositoryDecorator eventDecorator;

    public UserRepositoryImpl(UserJpaRepository jpaRepository,
                              DomainEventPublishingRepositoryDecorator eventDecorator) {
        this.jpaRepository = jpaRepository;
        this.eventDecorator = eventDecorator;
    }

    @Override
    public User save(User user) {
        return eventDecorator.publishEvents(user, () -> {
            var entity = UserEntity.fromDomain(user);
            var saved = jpaRepository.save(entity);
            return saved.toDomain();
        });
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value())
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value())
            .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }
}
