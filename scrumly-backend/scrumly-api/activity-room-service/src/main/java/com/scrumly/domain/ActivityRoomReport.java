package com.scrumly.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ActivityRoomReport {
    @Id
    private String id;
    private String activityId;
    private String activityName;
    private String activityType;
    private Integer totalAnswers;
    private ActivityRoom.ActivityRoomStatusMetadata.TimeMetadata timeMetadata;
    private ActivityRoom.TeamMetadata teamMetadata;
    private ActivityRoom.UserMetadata facilitator;
    private List<ActivityRoom.UserMetadata> usersParticipated;
    private List<ActivityBlockReport> blockReports;
    private ActivityRoom.ActivityRoomNotesMetadata notesMetadata;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityReportDocumentMetadata {
        private String fileId;
        private String fileName;
        private Long fileSize;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonTypeInfo(use = NAME, include = PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value= ActivityBlockReport.ActivityQuestionBlockReport.class, name = "ActivityQuestionBlockReport"),
            @JsonSubTypes.Type(value= ActivityBlockReport.ActivityItemBoardBlockReport.class, name = "ActivityItemBoardBlockReport"),
            @JsonSubTypes.Type(value= ActivityBlockReport.ActivityReflectBlockReport.class, name = "ActivityReflectBlockReport"),
            @JsonSubTypes.Type(value= ActivityBlockReport.ActivityEstimateBlockReport.class, name = "ActivityEstimateBlockReport"),
    })
    public static class ActivityBlockReport {
        private ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityQuestionBlockReport extends ActivityBlockReport {
            private Integer totalAnswers;
            private List<ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer> userQuestionAnswers;

            @Builder
            public ActivityQuestionBlockReport(ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata, Integer totalAnswers, List<ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer> userQuestionAnswers) {
                super(metadata);
                this.totalAnswers = totalAnswers;
                this.userQuestionAnswers = userQuestionAnswers;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityItemBoardBlockReport extends ActivityBlockReport {
            private Integer totalItems;
            private List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> boardColumnIssues;
            private ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata teamLoadMetadata;

            @Builder
            public ActivityItemBoardBlockReport(ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata, Integer totalItems, List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> boardColumnIssues, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata teamLoadMetadata) {
                super(metadata);
                this.totalItems = totalItems;
                this.boardColumnIssues = boardColumnIssues;
                this.teamLoadMetadata = teamLoadMetadata;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityReflectBlockReport extends ActivityBlockReport {
            private Integer totalReflections;
            private List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> columnCards;

            @Builder
            public ActivityReflectBlockReport(ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata, Integer totalReflections, List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> columnCards) {
                super(metadata);
                this.totalReflections = totalReflections;
                this.columnCards = columnCards;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityEstimateBlockReport extends ActivityBlockReport {
            private Integer totalEstimated;
            private List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> estimateIssues;

            @Builder
            public ActivityEstimateBlockReport(ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata, Integer totalEstimated, List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> estimateIssues) {
                super(metadata);
                this.totalEstimated = totalEstimated;
                this.estimateIssues = estimateIssues;
            }
        }
    }
}