package com.levelUp2.project_scaffolding_server.db.service;

import com.levelUp2.project_scaffolding_server.db.entity.Scaff;
import com.levelUp2.project_scaffolding_server.db.repo.ScaffRepo;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ScaffService {
    private final ScaffRepo scaffRepository;

    public ScaffService(ScaffRepo scaffRepository) {
        this.scaffRepository = scaffRepository;
    }

    public Map<String, Scaff> getScaffChildren(String id) {
        List<Scaff> children = scaffRepository.findByParentId(id);

        // Convert List<Scaff> to Map<String, Scaff> using the id as the key
        return children.stream().collect(Collectors.toMap(Scaff::getId, scaff -> scaff));
    }

    public List<Scaff> getAllScaffs() {
        return scaffRepository.findAll();
    }

    public Optional<Scaff> getScaffById(String id) {
        return scaffRepository.findById(id);
    }

    public Scaff saveScaff(Scaff scaff) {
        return scaffRepository.save(scaff);
    }

    public void deleteScaff(String id) {
        scaffRepository.deleteById(id);
    }
}
