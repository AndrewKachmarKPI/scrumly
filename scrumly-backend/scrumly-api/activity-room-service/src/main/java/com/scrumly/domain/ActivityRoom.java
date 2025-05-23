package com.scrumly.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("ActivityRoomFilter")
@Builder(toBuilder = true)
public class ActivityRoom {
    @Id
    private String id;
    private String activityId;
    private String activityName;
    private ActivityRoomStatusMetadata statusMetadata;
    private TeamMetadata teamMetadata;
    private ActivityTemplateMetadata templateMetadata;
    private ActivityRoomNotesMetadata notesMetadata;
    private UserMetadata facilitator;
    private UserMetadata currentUser;
    private BlockNavigationMetadata blockNavigationMetadata;
    private List<UserMetadata> joinedUsers = new ArrayList<>();
    private List<ActivityBlock> activityBlocks = new ArrayList<>();


    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ActivityRoomNotesMetadata {
        private String notes;
    }

    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonFilter("ActivityRoomStatusMetadataFilter")
    public static class ActivityRoomStatusMetadata {
        private ActivityRoomStatus currentStatus;
        private TimeMetadata timeMetadata;
        private Boolean isActive;
        private List<ActivityRoomStatusChangeMetadata> statusChangeMetadata;
        @JsonIgnoreProperties
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        public static class ActivityRoomStatusChangeMetadata {
            private ActivityRoomStatus status;
            private ActivityRoomStatus prevStatus;
            private UserMetadata userMetadata;
        }
        @JsonIgnoreProperties
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        public static class TimeMetadata {
            private LocalDateTime startDateTime;
            private LocalDateTime finishDateTime;
            private Long duration;
        }

        public enum ActivityRoomStatus {
            ACTIVE, CLOSED
        }
    }
    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonFilter("TeamMetadataFilter")
    public static class TeamMetadata {
        @NotBlank
        private String organizationId;
        @NotBlank
        private String teamId;
        @NotBlank
        private String organizationName;
        @NotBlank
        private String teamName;
    }
    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonFilter("ActivityTemplateMetadataFilter")
    public static class ActivityTemplateMetadata {
        private String activityId;
        private String name;
        private String type;
    }
    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonFilter("UserMetadataFilter")
    public static class UserMetadata {
        private String userId;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarId;
    }
    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class BlockNavigationMetadata {
        private String activeBlockId;
        private List<ActivityBlockMetadata> blocks;
        private List<ActivityBlockUserNavigation> userNavigations;
        @JsonIgnoreProperties
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        public static class ActivityBlockUserNavigation {
            private String activeBlockId;
            private String userId;
        }
        @JsonIgnoreProperties
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        public static class ActivityBlockMetadata {
            private String id;
            private Integer order;
            private String type;
            private String name;
            private String description;
        }
    }
    @JsonIgnoreProperties
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeInfo(use = NAME, include = PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value= ActivityBlock.ActivityQuestionBlock.class, name = "ActivityQuestionBlock"),
            @JsonSubTypes.Type(value= ActivityBlock.ActivityReflectBlock.class, name = "ActivityReflectBlock"),
            @JsonSubTypes.Type(value= ActivityBlock.ActivityItemBoardBlock.class, name = "ActivityItemBoardBlock"),
            @JsonSubTypes.Type(value= ActivityBlock.ActivityEstimateBlock.class, name = "ActivityEstimateBlock")
    })
    public static class ActivityBlock {
        private BlockNavigationMetadata.ActivityBlockMetadata metadata;

        @JsonIgnoreProperties
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityQuestionBlock extends ActivityBlock {
            private List<QuestionMetadata> questionMetadata;
            private List<UserQuestionAnswer> userQuestionAnswers;

            @Builder
            public ActivityQuestionBlock(BlockNavigationMetadata.ActivityBlockMetadata metadata, List<QuestionMetadata> questionMetadata, List<UserQuestionAnswer> userQuestionAnswers) {
                super(metadata);
                this.questionMetadata = questionMetadata;
                this.userQuestionAnswers = userQuestionAnswers;
            }

            @Builder
            public ActivityQuestionBlock(ActivityQuestionBlock questionBlock) {
                super(questionBlock.getMetadata());
                this.questionMetadata = questionBlock.questionMetadata;
                this.userQuestionAnswers = questionBlock.getUserQuestionAnswers();
            }

            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("QuestionMetadataFilter")
            public static class QuestionMetadata {
                private Long id;
                private String question;
                private List<String> answerOptions;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            public static class UserQuestionAnswer {
                private UserMetadata userMetadata;
                private List<QuestionAnswer> answers;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            public static class QuestionAnswer {
                private QuestionMetadata questionMetadata;
                private String answer;
            }
        }

        @JsonIgnoreProperties
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityReflectBlock extends ActivityBlock {
            private List<ReflectColumnMetadata> columnMetadata;
            private List<ReflectColumnCard> columnCards;

            @Builder
            public ActivityReflectBlock(BlockNavigationMetadata.ActivityBlockMetadata metadata, List<ReflectColumnMetadata> columnMetadata, List<ReflectColumnCard> columnCards) {
                super(metadata);
                this.columnMetadata = columnMetadata;
                this.columnCards = columnCards;
            }

            @Builder
            public ActivityReflectBlock(ActivityReflectBlock block) {
                super(block.getMetadata());
                this.columnMetadata = block.columnMetadata;
                this.columnCards = block.columnCards;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            public static class ReflectColumnMetadata {
                private Long id;
                private Integer order;
                private String title;
                private String color;
                private String instruction;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("ReflectColumnCardFilter")
            public static class ReflectColumnCard {
                @Version
                private Long version;
                private ReflectColumnMetadata columnMetadata;
                private List<UserColumnReflectCard> userColumnReflectCards;
                @JsonIgnoreProperties
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder(toBuilder = true)
                public static class UserColumnReflectCard {
                    private Long columnId;
                    private String cardId;
                    private Integer order;
                    private UserMetadata userMetadata;
                    private String cardContent;
                }
            }
        }
        @JsonIgnoreProperties
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonFilter("ActivityItemBoardBlockFilter")
        public static class ActivityItemBoardBlock extends ActivityBlock {
            private List<IssueMetadata> issueBacklog;
            private List<ItemBoardColumnMetadata> columnMetadata;
            private List<ItemBoardColumnIssues> columnIssues;
            private List<UserMetadata> teamMembers;
            private TeamLoadMetadata teamLoadMetadata;

            @Builder
            public ActivityItemBoardBlock(BlockNavigationMetadata.ActivityBlockMetadata metadata, List<IssueMetadata> issueBacklog, List<ItemBoardColumnMetadata> columnMetadata, List<ItemBoardColumnIssues> columnIssues, List<UserMetadata> teamMembers, TeamLoadMetadata teamLoadMetadata) {
                super(metadata);
                this.issueBacklog = issueBacklog;
                this.columnMetadata = columnMetadata;
                this.columnIssues = columnIssues;
                this.teamMembers = teamMembers;
                this.teamLoadMetadata = teamLoadMetadata;
            }

            @Builder
            public ActivityItemBoardBlock(ActivityItemBoardBlock block) {
                super(block.getMetadata());
                this.issueBacklog = block.issueBacklog;
                this.columnMetadata = block.columnMetadata;
                this.columnIssues = block.columnIssues;
                this.teamMembers = block.teamMembers;
                this.teamLoadMetadata = block.teamLoadMetadata;
            }
            @JsonIgnoreProperties
            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("TeamLoadMetadataFilter")
            public static class TeamLoadMetadata {
                private Double totalEstimation;
                private Integer totalItems;
                private List<ItemStatusMetadata> doneStatuses;
                private List<ItemStatusMetadata> inProgressStatuses;
                private List<TeamMemberLoadMetadata> membersLoadMetadata;
                @JsonIgnoreProperties
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder(toBuilder = true)
                public static class TeamMemberLoadMetadata {
                    private Integer totalItems;
                    private Double totalCapacity;
                    private Double committedLoad;
                    private Double doneLoad;
                    private Double loadInProgress;
                    private Double progress;
                    private MeterProgressMetadata meterProgressMetadata;
                    private UserMetadata userMetadata;
                }
                @JsonIgnoreProperties
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder(toBuilder = true)
                public static class MeterProgressMetadata {
                    private Double donePercentage;
                    private Double inProgressPercentage;
                    private Double todoPercentage;
                }
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("ItemBoardColumnMetadataFilter")
            public static class ItemBoardColumnMetadata {
                private Long id;
                private Integer order;
                private String title;
                private String color;
                private String instruction;
                private List<ItemStatusMetadata> columnIssueStatuses;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("ItemStatusMetadataFilter")
            public static class ItemStatusMetadata {
                private String statusName;
                private String statusId;
                private String color;
                private String backlogId;
            }
            @JsonIgnoreProperties
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder(toBuilder = true)
            @JsonFilter("ItemBoardColumnIssuesFilter")
            public static class ItemBoardColumnIssues {
                @Version
                private Long version;
                private ItemBoardColumnMetadata columnMetadata;
                private List<ItemBoardColumnUserIssue> userColumnIssues;
                @JsonIgnoreProperties
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder(toBuilder = true)
                @JsonFilter("ItemBoardColumnUserIssueFilter")
                public static class ItemBoardColumnUserIssue {
                    private Long columnId;
                    private String columnIssueId;
                    private Integer order;
                    private ItemStatusMetadata statusMetadata;
                    private IssueMetadata issueMetadata;
                    private UserMetadata userMetadata;
                }
            }
        }
        @JsonIgnoreProperties
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonFilter("ActivityEstimateBlockFilter")
        public static class ActivityEstimateBlock extends ActivityBlock {
            private EstimateScaleMetadata scaleMetadata;
            private List<EstimateIssue> estimateIssues;
            private String activeEstimateIssueId;

            @Builder
            public ActivityEstimateBlock(BlockNavigationMetadata.ActivityBlockMetadata metadata,
                                         EstimateScaleMetadata scaleMetadata,
                                         List<EstimateIssue> estimateIssues) {
                super(metadata);
                this.scaleMetadata = scaleMetadata;
                this.estimateIssues = estimateIssues;
            }

            @Builder
            public ActivityEstimateBlock(ActivityEstimateBlock block) {
                super(block.getMetadata());
                this.scaleMetadata = block.scaleMetadata;
                this.estimateIssues = block.estimateIssues;
                this.activeEstimateIssueId = block.activeEstimateIssueId;
            }
            @JsonIgnoreProperties
            @Getter
            @Setter
            @Builder(toBuilder = true)
            @NoArgsConstructor
            @AllArgsConstructor
            public static class EstimateScaleMetadata {
                private String estimateMethod;
                private String name;
                private List<String> scale;
            }
            @JsonIgnoreProperties
            @Getter
            @Setter
            @Builder(toBuilder = true)
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonFilter("EstimateIssueFilter")
            public static class EstimateIssue {
                private String id;
                private IssueMetadata issueMetadata;
                private String finalEstimate;
                private Boolean isRevealed;
                private List<UserEstimateMetadata> userEstimateMetadata;
            }
            @JsonIgnoreProperties
            @Getter
            @Setter
            @Builder(toBuilder = true)
            @NoArgsConstructor
            @AllArgsConstructor
            public static class UserEstimateMetadata {
                private UserMetadata userMetadata;
                private String estimate;
            }
        }

        @JsonIgnoreProperties
        @Getter
        @Setter
        @Builder(toBuilder = true)
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonFilter("IssueMetadataFilter")
        public static class IssueMetadata {
            private String issueId;
            private String issueKey;
            private String imgPath;
            private String issueUrl;
            private String description;
            private String title;
            private String provider;
            private String projectName;
            private String estimate;
            private String projectId;
        }
    }
}