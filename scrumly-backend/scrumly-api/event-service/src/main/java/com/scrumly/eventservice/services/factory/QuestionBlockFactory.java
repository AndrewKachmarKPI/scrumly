package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.blocks.question.QuestionBlockEntity;
import com.scrumly.eventservice.domain.blocks.question.QuestionEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.repository.QuestionBlockRepository;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionBlockFactory implements ActivityBlockFactory {
    private final ModelMapper modelMapper;
    private final QuestionBlockRepository questionBlockRepository;

    @Override
    public ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createRq) {
        if (!(createRq instanceof CreateQuestionBlockRQ rq)) {
            throw new ServiceErrorException("Failed to create activity block");
        }
        List<QuestionEntity> questions = rq.getQuestions().stream()
                .map(questionRQ -> QuestionEntity.builder()
                        .answerOptions(questionRQ.getAnswerOptions())
                        .question(questionRQ.getQuestion())
                        .build())
                .toList();

        QuestionBlockEntity block = QuestionBlockEntity.builder()
                .blockId(UUID.randomUUID().toString())
                .type(rq.getType())
                .name(rq.getName())
                .description(rq.getDescription())
                .isMandatory(rq.getIsMandatory())
                .questions(questions)
                .build();
        return questionBlockRepository.save(block);
    }

    @Override
    public ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto dto) {
        if (!(dto instanceof QuestionBlockDto blockDto)) {
            throw new ServiceErrorException("Failed to create activity block");
        }
        QuestionBlockEntity block = (QuestionBlockEntity) findActivityBlock(blockId);
        if (blockDto.getName() != null && !Objects.equals(block.getName(), blockDto.getName())) {
            block.setName(blockDto.getName());
        }
        if (blockDto.getDescription() != null && !Objects.equals(block.getDescription(), blockDto.getDescription())) {
            block.setDescription(blockDto.getDescription());
        }
        if (blockDto.getIsMandatory() != null && !Objects.equals(block.getIsMandatory(), blockDto.getIsMandatory())) {
            block.setIsMandatory(blockDto.getIsMandatory());
        }
        if (blockDto.getAnswerTimeLimit() != null && !Objects.equals(block.getAnswerTimeLimit(), blockDto.getAnswerTimeLimit())) {
            block.setAnswerTimeLimit(blockDto.getAnswerTimeLimit());
        }
        if (blockDto.getQuestions() != null && !blockDto.getQuestions().isEmpty()) {
            List<QuestionEntity> newQuestions = new ArrayList<>();
            for (QuestionDto questionDto : blockDto.getQuestions()) {
                QuestionEntity question = block.getQuestions().stream()
                        .filter(questionEntity -> questionEntity.getId().equals(questionDto.getId()))
                        .findFirst()
                        .orElse(null);
                if (question != null) {
                    question.setQuestion(question.getQuestion());
                    question.setAnswerOptions(questionDto.getAnswerOptions());
                } else if (questionDto.getId() == null) {
                    newQuestions.add(QuestionEntity.builder()
                            .question(questionDto.getQuestion())
                            .answerOptions(questionDto.getAnswerOptions())
                            .build());
                }
            }

            block.getQuestions().removeIf(questionEntity -> blockDto.getQuestions().stream()
                    .noneMatch(questionDto -> questionEntity.getId().equals(questionDto.getId())));

            block.getQuestions().addAll(newQuestions);
        }
        return questionBlockRepository.save(block);
    }

    @Override
    @Transactional
    public void deleteActivityBlock(String blockId) {
        questionBlockRepository.deleteByBlockId(blockId);
    }

    @Override
    public ActivityBlockEntity copyActivityBlock(String blockId) {
        QuestionBlockEntity block = (QuestionBlockEntity) findActivityBlock(blockId);
        QuestionBlockEntity newBlock = new QuestionBlockEntity(block);
        newBlock.setId(null);
        newBlock.setBlockId(UUID.randomUUID().toString());
        return questionBlockRepository.save(newBlock);
    }

    @Override
    public ActivityBlockEntity findActivityBlock(String blockId) {
        return questionBlockRepository.findByBlockId(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block is not found"));
    }
}
