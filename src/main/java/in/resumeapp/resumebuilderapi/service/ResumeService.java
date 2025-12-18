package in.resumeapp.resumebuilderapi.service;

import in.resumeapp.resumebuilderapi.dto.AuthResponse;
import in.resumeapp.resumebuilderapi.dto.CreateResumeRequest;
import in.resumeapp.resumebuilderapi.dto.Resume;
import in.resumeapp.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public Resume createResume(CreateResumeRequest request, Object principalObject) {

        //Step 1: Create resume object.
        Resume newResume = new Resume();

        //Step 2: Get the current profile.
        AuthResponse response = authService.getProfile(principalObject);

        //Step 3: Update the resume object.
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

        //Step 4: Set default data for resume.
        setDefaultResumeData(newResume);

        //Step 5: Save the resume data.
        return resumeRepository.save(newResume);
    }

    private void setDefaultResumeData(Resume newResume) {

        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperience(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkills(new ArrayList<>());
        newResume.setCertifications(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<Resume> getUserResumes(Object principal) {
        //Step 1: Get the current profile.
        AuthResponse response = authService.getProfile(principal);

        //Step 2: Call the repository finder method.
        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        //Step 3: Return response.
        return resumes;
    }

    public Resume getResumeById(String resumeId, Object principal) {
        //Step 1: Get the current profile.
        AuthResponse response = authService.getProfile(principal);

        //Step 2: Call the repository finder method to get resume.
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found."));

        //Step 3: Return the result.
        return existingResume;
    }

    public Resume updateResume(String resumeId, Resume updatedData, Object principal) {

        //Step 1: Get the current profile.
        AuthResponse response = authService.getProfile(principal);

        //Step 2: Call the repository finder method.
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found."));

        //Step 3: Update the new data.
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperience(updatedData.getWorkExperience());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setProjects(updatedData.getProjects());
        existingResume.setCertifications(updatedData.getCertifications());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setInterests(updatedData.getInterests());

        //Step 4: Update the details into database.
        resumeRepository.save(existingResume);

        //Step 5: Return the result.
        return existingResume;
    }

    public void deleteResume(String resumeId, Object principal) {

        //Step 1: Get the current profile.
        AuthResponse response = authService.getProfile(principal);

        //Step 2: Call the repository finder method.
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found."));
        resumeRepository.delete(existingResume);
    }
}
