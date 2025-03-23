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
    static final String ROOT_SCAFF = "00000000000000000000000000000000";
    private final ScaffRepo scaffRepository;

    public ScaffService(ScaffRepo scaffRepository) {
        this.scaffRepository = scaffRepository;
    }

    public Map<String, Scaff> getScaffChildren(String id) {
        List<Scaff> children = scaffRepository.findByParentId(id);

        // Convert List<Scaff> to Map<String, Scaff> using the id as the key
        return children.stream().filter(x -> !x.getId().equals(ROOT_SCAFF)).collect(Collectors.toMap(Scaff::getId, scaff -> scaff));
    }

    public List<Scaff> getAllScaffs() {
        return scaffRepository.findAll();
    }

    public List<Scaff> getAllParentScaffs() {
        return scaffRepository.findByParentIdExcludingSelf("00000000000000000000000000000000", "00000000000000000000000000000000");
    }

    public Optional<Scaff> getScaffById(String id) {
        return scaffRepository.findById(id);
    }

    public Scaff saveScaff(Scaff scaff) {
        Optional<Scaff> scaff1 = scaffRepository.findByName(scaff.getName());
        if (scaff1.isEmpty()) {
            try {
                return scaffRepository.save(scaff);
            } catch (Exception ignored) {
                return scaff;
            }
        }
        return scaff1.get();
    }

    public void deleteScaff(String id) {
        scaffRepository.deleteById(id);
    }
}
