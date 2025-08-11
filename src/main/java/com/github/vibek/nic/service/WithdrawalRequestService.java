package com.github.vibek.nic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.vibek.nic.dto.WithdrawalDto.*;
import com.github.vibek.nic.entity.*;
import com.github.vibek.nic.enums.WithdrawalRequestStatus;
import com.github.vibek.nic.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalRequestService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final PersonRepository personRepository;
    private final CaseRepository caseRepository;
    private final TeamFormationRepository teamFormationRepository;

    @Transactional
    public WithdrawalRequest createRequest(WithdrawalRequestCreateDTO dto) {
        Person requester = personRepository.findById(dto.getRequesterId())
            .orElseThrow(() -> new RuntimeException("Requester not found"));
        ChildMarriageCase caseEntity = caseRepository.findById(dto.getCaseId())
            .orElseThrow(() -> new RuntimeException("Case not found"));
        TeamFormation team = teamFormationRepository.findByCaseId_Id(dto.getCaseId())
            .orElseThrow(() -> new RuntimeException("Team not found for case"));

        boolean isMember = team.getMembers().stream().anyMatch(m -> m.getId().equals(dto.getRequesterId()));
        if (!isMember) {
            throw new IllegalStateException("Requester is not a member of this case team");
        }

        // Prevent duplicate pending requests
        Optional<WithdrawalRequest> existingRequest = withdrawalRequestRepository.findByCaseEntity_IdAndRequester_Id(dto.getCaseId(), dto.getRequesterId());
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == WithdrawalRequestStatus.PENDING) {
            throw new IllegalStateException("A pending withdrawal request for this case already exists.");
        }

        WithdrawalRequest request = WithdrawalRequest.builder()
            .caseEntity(caseEntity)
            .requester(requester)
            .reason(dto.getReason())
            .build();
        
        return withdrawalRequestRepository.save(request);
    }

    public List<WithdrawalRequestResponseDTO> getPendingRequestsForSupervisor(String department, String subdivision) {
        List<WithdrawalRequest> requests = withdrawalRequestRepository
            .findByStatusAndDepartmentAndSubdivision(WithdrawalRequestStatus.PENDING, department, subdivision);
        
        return requests.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    public Optional<WithdrawalRequest> findByCaseAndRequester(UUID caseId, UUID requesterId) {
        return withdrawalRequestRepository.findByCaseEntity_IdAndRequester_Id(caseId, requesterId);
    }

    @Transactional
    public void approveAndReassign(UUID requestId, WithdrawalReassignDTO dto) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));
        
        if (request.getStatus() != WithdrawalRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        Person supervisor = personRepository.findById(dto.getSupervisorId()).orElseThrow(() -> new RuntimeException("Supervisor not found"));
        Person newMember = personRepository.findById(dto.getNewMemberId()).orElseThrow(() -> new RuntimeException("New member not found"));
        Person oldMember = request.getRequester();
        
        TeamFormation team = teamFormationRepository.findByCaseId_Id(request.getCaseEntity().getId())
            .orElseThrow(() -> new RuntimeException("Team not found for case"));

        // The Swap Logic
        team.getMembers().removeIf(member -> member.getId().equals(oldMember.getId()));
        team.getMembers().add(newMember);
        
        List<UUID> deptMemberList = team.getDepartmentMembers().get(oldMember.getDepartment());
        if (deptMemberList != null) {
            deptMemberList.remove(oldMember.getId());
            deptMemberList.add(newMember.getId());
        }

        teamFormationRepository.save(team);

        // Update the request status
        request.setStatus(WithdrawalRequestStatus.APPROVED);
        request.setReviewer(supervisor);
        request.setReviewedAt(LocalDateTime.now());
        withdrawalRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(UUID requestId, UUID supervisorId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));
            
        if (request.getStatus() != WithdrawalRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        Person supervisor = personRepository.findById(supervisorId).orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        request.setStatus(WithdrawalRequestStatus.REJECTED);
        request.setReviewer(supervisor);
        request.setReviewedAt(LocalDateTime.now());
        withdrawalRequestRepository.save(request);
    }

    public WithdrawalRequestResponseDTO mapToResponseDTO(WithdrawalRequest request) {
        return WithdrawalRequestResponseDTO.builder()
            .requestId(request.getId())
            .caseId(request.getCaseEntity().getId())
            .requesterId(request.getRequester().getId())
            .requesterName(request.getRequester().getFirstName() + " " + request.getRequester().getLastName())
            .reason(request.getReason())
            .status(request.getStatus().name())
            .createdAt(request.getCreatedAt())
            .build();
    }
}