// src/main/java/com/github/vibek/nic/service/TeamFormationService.java
package com.github.vibek.nic.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.vibek.nic.dto.TeamFormationDTO;
import com.github.vibek.nic.dto.TeamDto.ManualTeamFormationRequest;
import com.github.vibek.nic.dto.TeamDto.TeamResponseDTO;
import com.github.vibek.nic.entity.*;
import com.github.vibek.nic.enums.Role;
import com.github.vibek.nic.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamFormationService {

    @Autowired
    private TeamFormationRepository teamFormationRepository;
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CaseDetailsRepository caseDetailsRepository;
    @Autowired
    private TeamResponseRepository teamResponseRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SubdivisionRepository subdivisionRepository;

    private static final int MEMBERS_PER_DEPARTMENT = 2;
    private static final int MAX_ELIGIBLE_PER_DEPT = 8;
    private static final long ACCEPTANCE_TIMEOUT_MINUTES = 1440;

    // Constant for the specific rank of a Team Leader
    private static final int TEAM_LEADER_RANK = 4;

    @Transactional
    public void initiateTeamFormation(UUID caseId, Subdivision subdivision) {
        ChildMarriageCase caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        Subdivision targetSubdivision = subdivision;
        if (caseEntity.getCaseDetails() != null && !caseEntity.getCaseDetails().isEmpty()) {
            CaseDetails details = caseEntity.getCaseDetails().get(0);
            if (details.getGirlSubdivision() != null) {
                targetSubdivision = details.getGirlSubdivision();
            }
        }

        if (targetSubdivision == null) {
            throw new RuntimeException("Target subdivision for team formation cannot be null.");
        }

        List<String> allDepts = departmentRepository.findAll().stream()
                .map(Departments::getName)
                .collect(Collectors.toList());
        if (allDepts.isEmpty()) {
            throw new RuntimeException("No departments available in the system");
        }

        // ============================ CRITICAL FIX START ============================

        // Step 1: Find and validate the designated team leader FIRST.
        List<Person> allEligibleSupervisors = personRepository.findByRoleAndSubdivisionAndRank(Role.SUPERVISOR, targetSubdivision, TEAM_LEADER_RANK);
        if (allEligibleSupervisors.isEmpty()) {
            throw new RuntimeException("No eligible team leaders (Rank " + TEAM_LEADER_RANK + ") available in subdivision: " + targetSubdivision.getName());
        }
        Person designatedLeader = allEligibleSupervisors.get(0); // Get the leader

        // Find eligible members for other departments
        Map<String, List<Person>> deptMembersMap = new HashMap<>();
        List<Person> allEligibleMembers = new ArrayList<>();
        for (String deptName : allDepts) {
            List<Person> members = personRepository.findByDepartmentAndSubdivisionAndRoleAndRankGreaterThanEqual(
                    deptName, targetSubdivision, Role.MEMBER, 2);
            if (members.size() > MAX_ELIGIBLE_PER_DEPT) {
                members = members.subList(0, MAX_ELIGIBLE_PER_DEPT);
            }
            if (!members.isEmpty()) {
                deptMembersMap.put(deptName, members);
                allEligibleMembers.addAll(members);
            }
        }

        if (deptMembersMap.isEmpty()) {
            throw new RuntimeException("No eligible members in subdivision: " + targetSubdivision.getName());
        }
        
        // Step 2: Create the team object and assign the leader immediately.
        TeamFormation teamFormation = new TeamFormation();
        teamFormation.setCaseId(caseEntity);
        teamFormation.setSupervisor(designatedLeader); // <-- Assign the leader here
        teamFormation.setMembers(new ArrayList<>());
        teamFormation.setNotificationSentAt(LocalDateTime.now());
        teamFormation.setDepartmentStatuses(deptMembersMap.keySet().stream().collect(Collectors.toMap(d -> d, d -> "PENDING")));
        teamFormation.setDepartmentMembers(new HashMap<>());
        
        // Step 3: Now, saving will work because supervisor_id is not null.
        TeamFormation savedTeam = teamFormationRepository.save(teamFormation);
        
        // Step 4: Create the automatic "ACCEPTED" response for the designated leader
        TeamResponse supervisorResponse = new TeamResponse();
        supervisorResponse.setTeamId(savedTeam.getTeamId());
        supervisorResponse.setPersonId(designatedLeader.getId());
        supervisorResponse.setResponse("ACCEPTED");
        supervisorResponse.setRespondedAt(LocalDateTime.now());
        teamResponseRepository.save(supervisorResponse);
        
        // ============================= CRITICAL FIX END =============================
        
        caseEntity.setTeamFormation(savedTeam);
        caseRepository.save(caseEntity);

        CaseDetails caseDetails = caseEntity.getCaseDetails().isEmpty() ? new CaseDetails() : caseEntity.getCaseDetails().get(0);
        caseDetails.setCaseId(caseEntity);
        caseDetails.setTeamId(savedTeam.getTeamId());
        caseDetailsRepository.save(caseDetails);

        if (caseEntity.getCaseDetails().isEmpty()) {
            caseEntity.getCaseDetails().add(caseDetails);
        }
        caseRepository.save(caseEntity);

        List<UUID> allMemberIds = allEligibleMembers.stream().map(Person::getId).collect(Collectors.toList());
        // The leader is already assigned, so they don't need a "PENDING" notification.
        // We only notify the other potential supervisors if any exist.
        List<UUID> otherSupervisorIds = allEligibleSupervisors.stream()
            .map(Person::getId)
            .filter(id -> !id.equals(designatedLeader.getId()))
            .collect(Collectors.toList());

        sendNotifications(savedTeam.getTeamId(), caseEntity.getId(), allMemberIds, otherSupervisorIds);

        autoAssignTeamMembers(savedTeam, deptMembersMap);
    }

    private void autoAssignTeamMembers(TeamFormation teamFormation, Map<String, List<Person>> deptMembersMap) {
        Map<String, List<UUID>> finalDepartmentMembers = new HashMap<>();
        List<Person> allAssignedMembers = new ArrayList<>();

        for (Map.Entry<String, List<Person>> entry : deptMembersMap.entrySet()) {
            String department = entry.getKey();
            List<Person> availableMembers = entry.getValue();
            List<UUID> assignedToDeptIds = new ArrayList<>();
            int assignedCount = 0;

            for (Person member : availableMembers) {
                if (assignedCount < MEMBERS_PER_DEPARTMENT) {
                    assignedToDeptIds.add(member.getId());
                    allAssignedMembers.add(member);
                    assignedCount++;

                    TeamResponse memberResponse = new TeamResponse();
                    memberResponse.setTeamId(teamFormation.getTeamId());
                    memberResponse.setPersonId(member.getId());
                    memberResponse.setResponse("ACCEPTED");
                    memberResponse.setRespondedAt(LocalDateTime.now());
                    teamResponseRepository.save(memberResponse);
                } else {
                    TeamResponse memberResponse = new TeamResponse();
                    memberResponse.setTeamId(teamFormation.getTeamId());
                    memberResponse.setPersonId(member.getId());
                    memberResponse.setResponse("REJECTED");
                    memberResponse.setRespondedAt(LocalDateTime.now());
                    teamResponseRepository.save(memberResponse);
                }
            }

            if (!assignedToDeptIds.isEmpty()) {
                finalDepartmentMembers.put(department, assignedToDeptIds);
                teamFormation.getDepartmentStatuses().put(department, "ACCEPTED");
            }
        }

        teamFormation.setMembers(allAssignedMembers);
        teamFormation.setDepartmentMembers(finalDepartmentMembers);
        teamFormation.setFormedAt(LocalDateTime.now());
        teamFormationRepository.save(teamFormation);

        ChildMarriageCase caseEntity = teamFormation.getCaseId();
        CaseDetails caseDetails = caseEntity.getCaseDetails().get(0);
        caseDetails.setDepartmentMembers(finalDepartmentMembers);
        if (teamFormation.getSupervisor() != null) {
            caseDetails.setSupervisorId(teamFormation.getSupervisor().getId());
        }
        caseDetailsRepository.save(caseDetails);

        caseEntity.setStatus("IN_PROGRESS");
        caseRepository.save(caseEntity);
    }

    @Transactional
    public void handleResponse(UUID teamId, UUID personId, String department, String status) {
        TeamFormation teamFormation = teamFormationRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        TeamResponse response = teamResponseRepository.findByTeamIdAndPersonId(teamId, personId)
                .orElseThrow(() -> new RuntimeException("Response not found for this person and team"));

        if (teamFormation.getFormedAt() != null) {
            throw new RuntimeException("Team is already formed. Cannot add more members.");
        }

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        if ("ACCEPTED".equals(status)) {
            // Check for the specific team leader rank
            if (person.getRole() == Role.SUPERVISOR && person.getRank() == TEAM_LEADER_RANK) {
                if (teamFormation.getSupervisor() == null) {
                    teamFormation.setSupervisor(person);
                } else if (!teamFormation.getSupervisor().getId().equals(personId)) { // Allow existing leader to confirm
                    response.setResponse("REJECTED");
                    response.setRespondedAt(LocalDateTime.now());
                    teamResponseRepository.save(response);
                    throw new RuntimeException("Supervisor already assigned.");
                }
            } else if (person.getRole() == Role.MEMBER) {
                Map<String, List<UUID>> deptMembers = teamFormation.getDepartmentMembers();
                List<UUID> currentDeptMembers = deptMembers.getOrDefault(department, new ArrayList<>());
                if (currentDeptMembers.size() >= MEMBERS_PER_DEPARTMENT) {
                    response.setResponse("REJECTED");
                    response.setRespondedAt(LocalDateTime.now());
                    teamResponseRepository.save(response);
                    throw new RuntimeException("Department limit reached. Cannot add more.");
                }

                currentDeptMembers.add(personId);
                deptMembers.put(department, currentDeptMembers);
                teamFormation.setDepartmentMembers(deptMembers);

                teamFormation.getMembers().add(person);
            } else {
                 throw new RuntimeException("This person is a manager and cannot be assigned to a case team.");
            }
        }

        response.setResponse(status.toUpperCase());
        response.setRespondedAt(LocalDateTime.now());
        teamResponseRepository.save(response);

        updateDepartmentStatus(teamFormation, department);
        teamFormationRepository.save(teamFormation);

        if (isTeamComplete(teamFormation)) {
            confirmTeamFormation(teamFormation);
        }
    }

    private boolean isTeamComplete(TeamFormation team) {
        if (team.getSupervisor() == null) return false;

        for (String dept : team.getDepartmentStatuses().keySet()) {
            List<UUID> deptMembers = team.getDepartmentMembers().getOrDefault(dept, new ArrayList<>());
            if (deptMembers.size() < MEMBERS_PER_DEPARTMENT && !"NO_MEMBERS".equals(team.getDepartmentStatuses().get(dept))) {
                return false;
            }
        }
        return true;
    }

    private void confirmTeamFormation(TeamFormation team) {
        team.setFormedAt(LocalDateTime.now());
        teamFormationRepository.save(team);

        ChildMarriageCase caseEntity = team.getCaseId();
        CaseDetails caseDetails = caseEntity.getCaseDetails().get(0);
        caseDetails.setDepartmentMembers(team.getDepartmentMembers());
        if (team.getSupervisor() != null) {
            caseDetails.setSupervisorId(team.getSupervisor().getId());
        }
        caseDetailsRepository.save(caseDetails);

        caseEntity.setStatus("IN_PROGRESS");
        caseRepository.save(caseEntity);
    }

    @Scheduled(fixedRate = 60000)
    public void checkAcceptanceStatus() {
        List<TeamFormation> pendingTeams = teamFormationRepository.findByFormedAtIsNull();
        for (TeamFormation team : pendingTeams) {
            LocalDateTime timeout = team.getNotificationSentAt().plusMinutes(ACCEPTANCE_TIMEOUT_MINUTES);
            if (LocalDateTime.now().isAfter(timeout)) {
                for (String dept : team.getDepartmentStatuses().keySet()) {
                    if ("PENDING".equals(team.getDepartmentStatuses().get(dept))) {
                        handleDepartmentalEscalation(team, dept);
                    }
                }
            }
            if (isTeamComplete(team)) {
                confirmTeamFormation(team);
            }
        }
    }

    private void handleDepartmentalEscalation(TeamFormation team, String department) {
        Subdivision subdivision = team.getCaseId().getCaseDetails().get(0).getGirlSubdivision();
        if (subdivision == null) return;
        
        List<Person> rank1Supervisors = personRepository.findByDepartmentAndSubdivisionAndRoleAndRank(department, subdivision, Role.SUPERVISOR, 1);
        
        if (!rank1Supervisors.isEmpty()) {
            Person escalator = rank1Supervisors.get(0);
            List<UUID> currentDeptMembers = team.getDepartmentMembers().getOrDefault(department, new ArrayList<>());
            if (currentDeptMembers.size() < MEMBERS_PER_DEPARTMENT) {
                currentDeptMembers.add(escalator.getId());
                team.getDepartmentMembers().put(department, currentDeptMembers);
                team.getMembers().add(escalator);
                team.getDepartmentStatuses().put(department, "ESCALATED");

                TeamResponse response = new TeamResponse();
                response.setTeamId(team.getTeamId());
                response.setPersonId(escalator.getId());
                response.setResponse("ACCEPTED");
                response.setRespondedAt(LocalDateTime.now());
                teamResponseRepository.save(response);

                teamFormationRepository.save(team);
            }
        }
    }

    private void updateDepartmentStatus(TeamFormation team, String department) {
        // This logic can be enhanced if needed
    }

    public TeamFormationDTO getTeamFormationById(UUID id) {
        TeamFormation team = teamFormationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return mapToDTO(team);
    }

    public TeamFormationDTO getTeamFormationByCaseId(UUID caseId) {
        TeamFormation team = teamFormationRepository.findByCaseId_Id(caseId)
                .orElseThrow(() -> new RuntimeException("Team not found for case"));
        return mapToDTO(team);
    }

    private TeamFormationDTO mapToDTO(TeamFormation team) {
        TeamFormationDTO dto = new TeamFormationDTO();
        dto.setCaseId(team.getCaseId().getId());
        if (team.getSupervisor() != null) {
            dto.setSupervisorId(team.getSupervisor().getId());
        }
        dto.setDepartmentMembers(team.getDepartmentMembers());
        dto.setFormedAt(team.getFormedAt());
        dto.setDepartmentStatuses(team.getDepartmentStatuses());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getPendingResponses() {
        List<TeamResponse> pendingResponses = teamResponseRepository.findByResponse("PENDING");
        return pendingResponses.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getPendingResponsesByTeam(UUID teamId) {
        List<TeamResponse> pendingResponses = teamResponseRepository.findByTeamIdAndResponse(teamId, "PENDING");
        return pendingResponses.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamFormationDTO> getAllTeams() {
        List<TeamFormation> teams = teamFormationRepository.findAll();
        return teams.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamFormationDTO createManualTeam(ManualTeamFormationRequest req) {
        ChildMarriageCase cmCase = caseRepository.findById(req.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));
        Person supervisor = personRepository.findById(req.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        if (supervisor.getRole() != Role.SUPERVISOR || supervisor.getRank() != TEAM_LEADER_RANK)
            throw new RuntimeException("Manual team leader must have a rank of " + TEAM_LEADER_RANK);

        Map<String,List<UUID>> deptMembersUuids = new HashMap<>();
        for (Map.Entry<String,List<UUID>> e : req.getDepartmentMembers().entrySet()) {
            String dept = e.getKey();
            if (!departmentRepository.existsByName(dept))
                throw new RuntimeException("Unknown department: " + dept);
            List<UUID> goodIds = new ArrayList<>();
            for (UUID pid : e.getValue()) {
                Person p = personRepository.findById(pid)
                        .orElseThrow(() -> new RuntimeException("Person "+pid+" not found"));
                if (!dept.equalsIgnoreCase(p.getDepartment()))
                    throw new RuntimeException("Person "+pid+" is not in department "+dept);
                goodIds.add(pid);
            }
            deptMembersUuids.put(dept, goodIds);
        }

        List<UUID> allMemberUuids = deptMembersUuids.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<Person> memberPersons = personRepository.findAllById(allMemberUuids);

        TeamFormation team = new TeamFormation();
        team.setCaseId(cmCase);
        team.setSupervisor(supervisor);
        team.setMembers(memberPersons);
        team.setDepartmentMembers(deptMembersUuids);

        Map<String,String> statuses = deptMembersUuids.keySet()
                .stream()
                .collect(Collectors.toMap(d -> d, d -> "ACCEPTED"));
        team.setDepartmentStatuses(statuses);
        team.setFormedAt(LocalDateTime.now());
        teamFormationRepository.save(team);

        TeamResponse supResp = new TeamResponse(null, team.getTeamId(), supervisor.getId(),"ACCEPTED", LocalDateTime.now());
        teamResponseRepository.save(supResp);

        for (Person p : team.getMembers()) {
            TeamResponse r = new TeamResponse(null, team.getTeamId(), p.getId(),"ACCEPTED",LocalDateTime.now());
            teamResponseRepository.save(r);
        }

        cmCase.setStatus("IN_PROGRESS");
        caseRepository.save(cmCase);

        return mapToDTO(team);
    }

    private void sendNotifications(UUID teamId, UUID caseId, List<UUID> memberIds, List<UUID> supervisorIds) {
        // Create PENDING responses only for those who are not the designated leader
        for (UUID supervisorId : supervisorIds) {
            TeamResponse response = new TeamResponse(null, teamId, supervisorId, "PENDING", null);
            teamResponseRepository.save(response);
        }
        for (UUID personId : memberIds) {
            TeamResponse response = new TeamResponse(null, teamId, personId, "PENDING", null);
            teamResponseRepository.save(response);
        }

        List<UUID> allPersonIds = new ArrayList<>(supervisorIds);
        allPersonIds.addAll(memberIds);

        List<String> pushTokens = personRepository.findAllById(allPersonIds).stream()
                .map(Person::getPushToken)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (pushTokens.isEmpty()) {
            System.out.println("No push tokens found, skipping notification.");
            return;
        }

        String expoPushUrl = "https://exp.host/--/api/v2/push/send";
        List<Map<String, Object>> messages = pushTokens.stream().map(token -> {
            Map<String, Object> message = new HashMap<>();
            message.put("to", token);
            message.put("sound", "default");
            message.put("title", "New Case Assigned");
            message.put("body", "You have been assigned to a new child marriage case.");
            Map<String, Object> data = new HashMap<>();
            data.put("caseId", caseId.toString());
            message.put("data", data);
            return message;
        }).collect(Collectors.toList());

        try {
            restTemplate.postForEntity(expoPushUrl, messages, String.class);
            System.out.println("Push notifications sent successfully.");
        } catch (Exception e) {
            System.err.println("Error sending push notifications: " + e.getMessage());
        }
    }

    private TeamResponseDTO mapToResponseDTO(TeamResponse response) {
        TeamResponseDTO dto = new TeamResponseDTO();
        dto.setId(response.getId());
        dto.setTeamId(response.getTeamId());
        dto.setPersonId(response.getPersonId());
        dto.setResponse(response.getResponse());
        dto.setRespondedAt(response.getRespondedAt());

        personRepository.findById(response.getPersonId()).ifPresent(person -> {
            dto.setPersonName(person.getFirstName() + " " + person.getLastName());
            dto.setDepartment(person.getDepartment());
        });

        teamFormationRepository.findById(response.getTeamId()).ifPresent(team -> {
            dto.setCaseId(team.getCaseId().getId());
        });

        return dto;
    }
}