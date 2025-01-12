package uk.gov.hmcts.darts.authorisation.component.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@Component
@NoArgsConstructor
abstract class BaseControllerAuthorisation {

    public static final String BAD_REQUEST_AUTHORISATION_PARAM_ERROR_MESSAGE =
        "Unable to extract the %s in request path, query or header params for this Authorisation endpoint: %s";

    public static final String BAD_REQUEST_AUTHORISATION_ID_ERROR_MESSAGE =
        "id is empty";

    Optional<String> getPathParamValue(HttpServletRequest request, String pathParam) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return Optional.ofNullable(pathVariables.get(pathParam));
    }

    Optional<String> getEntityParamOptional(HttpServletRequest request, String entityIdParam) {
        Optional<String> entityIdParamOptional = getPathParamValue(request, entityIdParam);

        if (entityIdParamOptional.isEmpty()) {
            entityIdParamOptional = Optional.ofNullable(request.getParameter(entityIdParam));
        }
        if (entityIdParamOptional.isEmpty()) {
            entityIdParamOptional = Optional.ofNullable(request.getHeader(entityIdParam));
        }
        return entityIdParamOptional;
    }
}
