package com.levelUp2.project_scaffolding_server.db.repo;


import com.levelUp2.project_scaffolding_server.db.entity.Scaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScaffRepo extends JpaRepository<Scaff, String> {
    List<Scaff> findByParentId(String parentId);
    Optional<Scaff> findByName(String name);

    @Query("SELECT s FROM Scaff s WHERE s.parent.id = :parentId AND s.id <> :excludedId")
    List<Scaff> findByParentIdExcludingSelf(@Param("parentId") String parentId, @Param("excludedId") String excludedId);
}