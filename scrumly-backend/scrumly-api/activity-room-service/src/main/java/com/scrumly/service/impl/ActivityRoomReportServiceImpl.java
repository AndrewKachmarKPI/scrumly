package com.scrumly.service.impl;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomReport;
import com.scrumly.domain.ActivityRoomReportEntity;
import com.scrumly.event.dto.activity.ActivityDto;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.feign.EventServiceFeignClient;
import com.scrumly.repository.ActivityRoomReportRepository;
import com.scrumly.service.ActivityRoomReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.scrumly.mappers.BusinessMapper.*;

@Service
@RequiredArgsConstructor
public class ActivityRoomReportServiceImpl implements ActivityRoomReportService {

    private final ActivityRoomReportRepository roomReportRepository;
    private final EventServiceFeignClient eventServiceFeignClient;

    @Override
    public ActivityRoomReport createActivityRoomReport(ActivityRoom room) {
        ActivityRoomReportEntity roomReportEntity = roomReportRepository.findByActivityId(room.getActivityId());
        if (roomReportEntity != null) {
            throw new DuplicateEntityException("Room report already exists");
        }

        List<ActivityRoomReport.ActivityBlockReport> blockReports = new ArrayList<>();
        ActivityDto activityDto = eventServiceFeignClient.getActivityById(room.getActivityId());

        int totalAnswers = 0;
        for (ActivityRoom.ActivityBlock activityBlock : room.getActivityBlocks()) {
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityQuestionBlock block) {
                int answers = block.getUserQuestionAnswers().size();
                totalAnswers += answers;
                blockReports.add(ActivityRoomReport.ActivityBlockReport.ActivityQuestionBlockReport.builder()
                                         .metadata(block.getMetadata())
                                         .totalAnswers(answers)
                                         .userQuestionAnswers(block.getUserQuestionAnswers())
                                         .build());
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                int answers = block.getColumnCards().size();
                totalAnswers += answers;
                blockReports.add(ActivityRoomReport.ActivityBlockReport.ActivityReflectBlockReport.builder()
                                         .metadata(block.getMetadata())
                                         .totalReflections(answers)
                                         .columnCards(block.getColumnCards())
                                         .build());
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                int answers = block.getEstimateIssues().size();
                totalAnswers += answers;
                blockReports.add(ActivityRoomReport.ActivityBlockReport.ActivityEstimateBlockReport.builder()
                                         .metadata(block.getMetadata())
                                         .totalEstimated(block.getEstimateIssues().size())
                                         .estimateIssues(block.getEstimateIssues())
                                         .build());
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                int answers = block.getColumnIssues().size();
                totalAnswers += answers;
                blockReports.add(ActivityRoomReport.ActivityBlockReport.ActivityItemBoardBlockReport.builder()
                                         .metadata(block.getMetadata())
                                         .totalItems(answers)
                                         .boardColumnIssues(block.getColumnIssues())
                                         .teamLoadMetadata(block.getTeamLoadMetadata())
                                         .build());
            }
        }

        ActivityRoomReport roomReport = ActivityRoomReport.builder()
                .id(UUID.randomUUID().toString())
                .activityId(room.getActivityId())
                .activityName(room.getActivityName())
                .activityType(activityDto.getActivityTemplate().getType().getType())
                .timeMetadata(room.getStatusMetadata().getTimeMetadata())
                .teamMetadata(room.getTeamMetadata())
                .facilitator(room.getFacilitator())
                .totalAnswers(totalAnswers)
                .usersParticipated(room.getJoinedUsers())
                .blockReports(blockReports)
                .notesMetadata(room.getNotesMetadata())
                .build();

        if (roomReportRepository.findByActivityId(activityDto.getActivityId()) != null) {
            roomReportRepository.deleteByActivityId(activityDto.getActivityId());
        }
        roomReportEntity = ActivityRoomReportEntity.builder()
                .reportPayload(serializeMeetingRoomReport(roomReport))
                .activityId(activityDto.getActivityId())
                .build();
        roomReportRepository.save(roomReportEntity);
        return roomReport;
    }

    @Override
    public ActivityRoomReport getActivityRoomReport(String activityId) {
        ActivityRoomReportEntity report = roomReportRepository.findByActivityId(activityId);
        if (report == null) {
            throw new ServiceErrorException("Room report is not found");
        }
        return deserializeMeetingRoomReport(report.getReportPayload());
    }

    @Override
    @Transactional
    public ActivityRoomReport test(ActivityRoom room) {
        ActivityRoomReportEntity report = roomReportRepository.findByActivityId(room.getActivityId());
        if (report != null) {
            roomReportRepository.deleteByActivityId(room.getActivityId());
        }
        return createActivityRoomReport(room);
    }
}
