package org.freeyourmetadata.ner.services;

import static org.freeyourmetadata.util.UriUtil.createUri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.freeyourmetadata.util.ParameterList;

/**
 * DBpedia spotlight service connector
 *
 * @author Ruben Verborgh
 */
public class DBpediaSpotlight extends NERServiceBase implements NERService {
    private final static URI SERVICEBASEURL = createUri("https://api.dbpedia-spotlight.org/en/annotate");
    private final static String[] SERVICESETTINGS = {};
    private final static String[] EXTRACTIONSETTINGS = {"Confidence", "Support"};

    /**
     * Creates a new DBpedia spotlight service connector
     */
    public DBpediaSpotlight() {
        super(SERVICEBASEURL, null, SERVICESETTINGS, EXTRACTIONSETTINGS);
        setExtractionSettingDefault("Confidence", "0.5");
        setExtractionSettingDefault("Support", "30");
    }

    /**
     * {@inheritDoc}
     */
    protected HttpEntity createExtractionRequestBody(final String text, final Map<String, String> extractionSettings)
            throws UnsupportedEncodingException {
        final ParameterList parameters = new ParameterList();
        parameters.add("confidence", extractionSettings.get("Confidence"));
        parameters.add("support", extractionSettings.get("Support"));
        parameters.add("text", text);
        return parameters.toEntity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NamedEntity[] parseExtractionResponse(final ObjectNode response) throws Exception {
        // Empty result if no resources were found
        if (!response.has("Resources"))
            return EMPTY_EXTRACTION_RESULT;
        // Extract resources
        final ArrayNode resources = (ArrayNode) response.get("Resources");
        final NamedEntity[] results = new NamedEntity[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            final ObjectNode resource = (ObjectNode) resources.get(i);
            results[i] = new NamedEntity(resource.get("@surfaceForm").asText(),
                    createUri(resource.get("@URI").asText()));
        }
        return results;
    }
}
