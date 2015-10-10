/* 
 * Copyright (C) 2010 Robert Stewart (robert@wombatnation.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.log4mongo.layout;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.log4mongo.contrib.HostInfoPatternParser;

/**
 * PatternLayout that must be used or extended when logging with MongoDbPatternLayoutAppender.
 * <p>
 * Much of the PatternLayout functionality needed to be re-implemented, because double quotes and \
 * need to be escaped in the formatted String. The formatted String will later be parsed as a JSON
 * document, so quotes in the values must be escaped.
 *
 * @author Robert Stewart (robert@wombatnation.com)
 */
public class MongoDbDefaultLayout extends PatternLayout {

    private String conversionPattern;
    private PatternConverter headConverter;

    public MongoDbDefaultLayout() {
        this(DEFAULT_CONVERSION_PATTERN);
    }

    public MongoDbDefaultLayout(String pattern) {
        this.conversionPattern = pattern;
        headConverter = createPatternParser(
                (pattern == null) ? DEFAULT_CONVERSION_PATTERN : pattern).parse();
    }

    @Override
    public void setConversionPattern(String conversionPattern) {
        this.conversionPattern = conversionPattern;
        headConverter = createPatternParser(conversionPattern).parse();
    }

    @Override
    public String getConversionPattern() {
        return conversionPattern;
    }

    @Override
    public PatternParser createPatternParser(String pattern) {
        PatternParser parser;
        if (pattern == null)
            parser = new HostInfoPatternParser(DEFAULT_CONVERSION_PATTERN);
        else
            parser = new HostInfoPatternParser(pattern);

        return parser;
    }



    /**
     * Produces a formatted string as specified by the conversion pattern.
     * <p>
     * The PatternConverter expects to append to a StringBuffer. However, for converters other than
     * a LiteralPatternConverter, double quotes need to be escaped in the characters appended to the
     * StringBuffer.
     */
    @Override
    public String format(LoggingEvent event) {
        StringBuffer buf = new StringBuffer(BUF_SIZE);
        StringBuilder builder = new StringBuilder(BUF_SIZE);
        // Reset working StringBuilder
        if (builder.capacity() > MAX_CAPACITY) {
            builder = new StringBuilder(BUF_SIZE);
        } else {
            builder.setLength(0);
        }

        PatternConverter c = headConverter;

        while (c != null) {
            if (buf.capacity() > MAX_CAPACITY) {
                buf = new StringBuffer(BUF_SIZE);
            } else {
                buf.setLength(0);
            }
            c.format(buf, event);

            // Escape double quotes and \ in String generated by converters
            // other than a LiteralPatternConverter. Can't use "instance of"
            // because class is private.
            if (c.getClass().getSimpleName().equals("LiteralPatternConverter")) {
                builder.append(buf);
            } else {
                char[] chars = buf.toString().toCharArray();
                int pos = 0;
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == '\"') {
                        builder.append(chars, pos, i - pos).append("\\\"");
                        pos = i + 1;
                    } else if (chars[i] == '\\') {
                        builder.append(chars, pos, i - pos).append("\\\\");
                        pos = i + 1;
                    }
                }
                builder.append(chars, pos, chars.length - pos);
            }

            c = c.next;
        }
        return builder.toString();
    }
}
