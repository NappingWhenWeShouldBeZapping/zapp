package com.levelUp2.project_scaffolding_server.control;

import com.levelUp2.project_scaffolding_server.db.entity.Insertion;
import com.levelUp2.project_scaffolding_server.db.entity.Scaff;
import com.levelUp2.project_scaffolding_server.db.entity.User;
import com.levelUp2.project_scaffolding_server.db.service.InsertionService;
import com.levelUp2.project_scaffolding_server.db.service.ScaffService;
import com.levelUp2.project_scaffolding_server.db.service.UserService;
import com.levelUp2.project_scaffolding_server.lib.fs.FileSystem;
import com.levelUp2.project_scaffolding_server.models.CreateScaffRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/scaff")
public class ScaffController {
    // ✨ Recursively backtracks through the scaff tree, obtaining the ✨
    // ✨ in-order steps required to generate the rendered file system ✨
    private static final String RENDER_SCAFF_QUERY = "WITH RECURSIVE ancs(n, id, parent_id) AS ( "
            + "    SELECT 0, id, parent_id FROM scaffs WHERE id = :scaffId "
            + "    UNION ALL "
            + "    SELECT n+1, s.id, s.parent_id FROM scaffs s "
            + "    INNER JOIN ancs a ON s.id = a.parent_id "
            + "    WHERE a.id NOT LIKE '000%' "
            + "), "
            + "ins AS ( "
            + "    SELECT a.n, 0 AS step, a.id, a.parent_id, ins.filepath, NULL AS variable, ins.value "
            + "    FROM ancs a "
            + "    INNER JOIN insertion ins ON a.id = ins.scaff_id "
            + "), "
            + "sub AS ( "
            + "    SELECT a.n, 1 AS step, a.id, a.parent_id, NULL AS filepath, sub.variable, sub.value "
            + "    FROM ancs a "
            + "    INNER JOIN substitution sub ON a.id = sub.scaff_id "
            + ") "
            + "SELECT * FROM ins "
            + "UNION ALL "
            + "SELECT * FROM sub "
            + "ORDER BY n DESC, step ASC";
    private final ScaffService scaffService;
    private final InsertionService insertionService;
    private final UserService userService;
    @PersistenceContext
    private EntityManager entityManager;

    public ScaffController(ScaffService scaffService, InsertionService insertionService, UserService userService) {
        this.scaffService = scaffService;
        this.insertionService = insertionService;
        this.userService = userService;
    }

    @SuppressWarnings("unchecked")
    private static List<CreateScaffRequest.Insertion> getInsertions(Object obj) {
        if (obj instanceof List<?> list) {
            if (!list.isEmpty() && list.getFirst() instanceof Map<?, ?>) {
                return (List<CreateScaffRequest.Insertion>) list;
            } else {
                System.out.println("The elements in the list are not Map<String, String>");
            }
        }
        return null;
    }

    @GetMapping(path = "/", produces = "application/json")
    public List<Scaff> getAllScaffs() {
        return scaffService.getAllParentScaffs();
    }

    @PostMapping(path = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Scaff> createScaff(@RequestBody CreateScaffRequest requestData) {
        String parentId;

        if (requestData.getParentId() == null || requestData.getParentId().isBlank()) {
            parentId = "00000000000000000000000000000000";
        } else {
            parentId = requestData.getParentId();
        }

        Optional<Scaff> parent = scaffService.getScaffById(parentId);
        if (parent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        for (CreateScaffRequest.Insertion item : requestData.getInsertions()) {
            Optional<User> user = userService.getUserByEmail("user1@example.com");

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Scaff _scaff = new Scaff();
            _scaff.setId(UUID.randomUUID().toString().replace("-", ""));
            _scaff.setParent(parent.get());
            _scaff.setAuthor(user.get());
            _scaff.setName(requestData.getScaffName());
            _scaff.setDescr(requestData.getScaffDescr());
            Scaff scaff = scaffService.saveScaff(_scaff);

            Insertion _insertion = new Insertion();
            _insertion.setId(UUID.randomUUID().toString().replace("-", ""));
            _insertion.setScaff(scaff);
            _insertion.setFilepath(item.getRelativePath());
            _insertion.setValue(item.getContent());

            boolean created = insertionService.saveInsertion(_insertion);
            if (!created) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scaff> getScaffById(@PathVariable String id) {
        return ResponseEntity.of(scaffService.getScaffById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scaff> updateScaffById(@PathVariable String id) {
        return ResponseEntity.of(scaffService.getScaffById(id));
    }

    @GetMapping("/{id}/options")
    public ResponseEntity getOptions(@PathVariable String id) {
        Map<String, Scaff> scaffMap = scaffService.getScaffChildren(id);
        if (scaffMap == null || scaffMap.isEmpty()) {
            return ResponseEntity.ok().body(204);
        }
        return ResponseEntity.ok(scaffMap);
    }

    @PostMapping
    public Scaff createScaff(@RequestBody Scaff scaff) {
        return scaffService.saveScaff(scaff);
    }

    @DeleteMapping("/{id}")
    public void deleteScaff(@PathVariable String id) {
        scaffService.deleteScaff(id);
    }

    // Returns the rendered file system at the given scaff ID
    @GetMapping("/{id}/rendered")
    public ResponseEntity<?> getRenderedScaff(@PathVariable String id) {
        // Create initial empty filesystem
        FileSystem fs = new FileSystem();

        Query query = entityManager.createNativeQuery(RENDER_SCAFF_QUERY);
        query.setParameter("scaffId", id);

        // Row format: [ (0:n),   (1:step),   (2:id),   (3:parent_id),   (4:filepath),   (5:variable),   (6:value) ]
        @SuppressWarnings("unchecked") // A little bit 'trust me bro' unfortunately
        List<Object[]> rows = query.getResultList();

        // Process each row in order
        for (Object[] row : rows) {
            int step = ((Number) row[1]).intValue(); // trust me bro
            if (step == 0) {
                //-- Insert file --//
                String filepath = (String) row[4];
                String content = (String) row[6];
                fs.insertFile(filepath, content);
            } else if (step == 1) {
                //-- Substitute var --//
                String variable = (String) row[5];
                String replacement = (String) row[6];
                fs.substituteVar(variable, replacement);
            }
        }

        // Build final JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("vars", new HashMap<>()); // TODO: Populate "vars": { ... }
        response.put("files", fs.getFiles());

        return ResponseEntity.ok(response);
    }
}
