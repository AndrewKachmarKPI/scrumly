package com.scrumly.eventservice.config;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionBlockDto;
import com.scrumly.eventservice.dto.blocks.reflect.ReflectBlockDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true);
        return modelMapper;
    }
}
