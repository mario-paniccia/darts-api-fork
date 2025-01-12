package uk.gov.hmcts.darts.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.zalando.problem.jackson.ProblemModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
public final class TestUtils {

    private TestUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getContentsFromFile(String filelocation) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File(classLoader.getResource(filelocation).getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ProblemModule());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    public static String substituteHearingDateWithToday(String expectedResponse) {
        return expectedResponse.replace("todays_date", LocalDate.now().toString());
    }

    public static String readTempFileContent(final String annotationsFile) {
        String xmlContent = "";

        try (BufferedReader reader = Files.newBufferedReader(Path.of(annotationsFile))) {
            String line;
            StringBuilder content = new StringBuilder();
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                content.append(line);
            }
            xmlContent = content.toString();
        } catch (Exception e) {
            fail("Error reading XML file: " + e.getMessage());
        }

        return xmlContent;
    }

    public static <T> T unmarshalXmlFile(Class<T> type, String xmlFile) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(type);
        return type.cast(context.createUnmarshaller().unmarshal(Files.newBufferedReader(Path.of(xmlFile))));
    }

    public static String removeTags(List<String> tagsToRemove, String input) {
        String output = input;
        for (String tagToRemove : tagsToRemove) {
            output = output.replaceAll("\"" + tagToRemove + "\".{1,6},", "");
        }
        return output;
    }
}
