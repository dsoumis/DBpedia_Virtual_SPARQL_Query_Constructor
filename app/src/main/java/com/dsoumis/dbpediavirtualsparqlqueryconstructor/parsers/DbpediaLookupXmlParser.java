package com.dsoumis.dbpediavirtualsparqlqueryconstructor.parsers;

import android.util.Xml;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.DbpediaLookupResultDto;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DbpediaLookupXmlParser {

    private final XmlPullParser parser;

    public DbpediaLookupXmlParser() throws XmlPullParserException {
        this.parser = Xml.newPullParser();
        this.parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    }

    public Set<DbpediaLookupResultDto> parse(final InputStream in) throws XmlPullParserException, IOException {
        try {
            parser.setInput(in, null);
            parser.nextTag();
            return readArrayOfResults(parser);
        } finally {
            in.close();
        }
    }

    private Set<DbpediaLookupResultDto> readArrayOfResults(final XmlPullParser parser) throws XmlPullParserException, IOException {
        final Set<DbpediaLookupResultDto> entries = new HashSet<>();

        parser.require(XmlPullParser.START_TAG, null, "ArrayOfResults");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the Result tag
            if (name.equals("Result")) {
                entries.add(readResult(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of a Result. If it encounters a Label or URI tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private DbpediaLookupResultDto readResult(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "Result");
        String label = null;
        String uri = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String fieldName = parser.getName();
            if (fieldName.equals("Label")) {
                label = readField(parser, fieldName);
            } else if (fieldName.equals("URI")) {
                uri = readField(parser, fieldName);
            } else {
                skip(parser);
            }
        }
        return new DbpediaLookupResultDto(label, uri);
    }

    private String readField(final XmlPullParser parser, final String fieldName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, fieldName);
        final String fieldValue= readText(parser);
        parser.require(XmlPullParser.END_TAG, null, fieldName);
        return fieldValue;
    }

    private String readText(final XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(final XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
