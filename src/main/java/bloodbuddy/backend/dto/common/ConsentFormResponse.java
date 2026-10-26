package bloodbuddy.backend.dto.common;

import lombok.Builder;
import lombok.Getter;

/** Consent wording a public form must display; its version is sent back when the form is submitted. */
@Getter
@Builder
public class ConsentFormResponse {

    private final String version;
    private final String title;
    private final String content;
}
