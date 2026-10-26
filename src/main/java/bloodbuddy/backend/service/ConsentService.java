package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.common.ConsentFormResponse;
import bloodbuddy.backend.entity.ConsentForm;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.ConsentFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

    private final ConsentFormRepository consentFormRepository;

    public ConsentService(ConsentFormRepository consentFormRepository) {
        this.consentFormRepository = consentFormRepository;
    }

    @Transactional(readOnly = true)
    public ConsentFormResponse getActive(ConsentType type) {
        ConsentForm form = requireActive(type);
        return ConsentFormResponse.builder()
                .version(form.getVersion())
                .title(form.getTitle())
                .content(form.getContent())
                .build();
    }

    /**
     * Returns the active form if the submitted version matches it. A mismatch means the person saw
     * outdated wording, so they must reload and accept the current text.
     */
    public ConsentForm requireAccepted(ConsentType type, String acceptedVersion) {
        ConsentForm form = requireActive(type);
        if (!form.getVersion().equals(acceptedVersion)) {
            throw new BadRequestException("Consent form has been updated; please review and accept the latest version");
        }
        return form;
    }

    private ConsentForm requireActive(ConsentType type) {
        return consentFormRepository.findFirstByConsentTypeAndIsActiveTrueOrderByCreatedAtDesc(type)
                .orElseThrow(() -> new ResourceNotFoundException("No active " + type + " consent form"));
    }
}
