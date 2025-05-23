package com.scrumly.userservice.userservice.utils;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "Invalid file.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    @Override
    public void initialize(ValidFile constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                        contentType.equals(MediaType.IMAGE_PNG_VALUE))) {
            return false;
        }
        final long maxFileSize = 2 * 1024 * (long) 1024;
        return file.getSize() <= maxFileSize;
    }
}
