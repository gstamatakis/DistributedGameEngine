package ui.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ui.model.PlayEntity;

import javax.transaction.Transactional;

public interface PlayRepository extends JpaRepository<PlayEntity, Integer> {
    boolean existsByPlayID(String playID);

    PlayEntity findByPlayID(String username);

    @Transactional
    void deleteByPlayID(String username);
}
