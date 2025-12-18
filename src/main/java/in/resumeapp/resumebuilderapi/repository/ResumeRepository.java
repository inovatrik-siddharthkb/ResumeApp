package in.resumeapp.resumebuilderapi.repository;


import in.resumeapp.resumebuilderapi.dto.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends MongoRepository<Resume, String> {

    List<Resume> findByUserIdOrderByUpdatedAtDesc(String userID);

    Optional<Resume> findByUserIdAndId(String userID, String id);

}
