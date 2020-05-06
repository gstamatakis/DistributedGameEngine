package ui.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ui.model.UserEntity;

import javax.transaction.Transactional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    @Transactional
    void deleteByUsername(String username);

}
