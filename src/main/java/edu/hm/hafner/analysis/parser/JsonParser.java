package edu.hm.hafner.analysis.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;

/**
 * Parser report in JSON format as exported by the "Jenkins Warnings Next Generation Plugin".
 *
 * @author Jeremie Bresson
 */
public class JsonParser extends JsonBaseParser {
    private static final long serialVersionUID = -6494117943149352139L;
    private static final String ISSUES = "issues";

    @Override
    public boolean accepts(final ReaderFactory readerFactory) {
        return readerFactory.getFileName().endsWith(".json");
    }

    @Override
    public Report parse(final ReaderFactory readerFactory) throws ParsingException {
        try (Reader reader = readerFactory.create()) {
            JSONObject jsonReport = (JSONObject) new JSONTokener(reader).nextValue();

            Report report = new Report();
            if (jsonReport.has(ISSUES)) {
                JSONArray issues = jsonReport.getJSONArray(ISSUES);
                StreamSupport.stream(issues.spliterator(), false)
                        .filter(o -> o instanceof JSONObject)
                        .map(o -> convertToIssue((JSONObject) o))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(report::add);
            }
            return report;
        }
        catch (IOException | JSONException e) {
            throw new ParsingException(e);
        }
    }
}