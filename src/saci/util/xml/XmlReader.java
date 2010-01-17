/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 SACI Informática Ltda.
 */

package saci.util.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

class XmlReader {

    static Logger logger = Logger.getLogger(XmlReader.class.getName());
    static final char TAG_START = '<';
    static final char TAG_END = '>';
    static final char EQUAL = '=';
    static final char CLOSE = '/';
    static final char STRING_DELIMITER = '\"';
    static final char NULL = '\0';
    static final char EXCLAMATION = '!';
    static final String TAG_MAP_TAG_NAME = "__tagName";
    static final String TAG_MAP_TAG_CLOSE = "__close";
    private int length = 0, position = 0;
    private char[] buffer;
    protected HashMap<String, String> tagMap = new HashMap<String, String>();
    protected XmlMapper mapper = new XmlMapper();
    private InputStream inputStream;
    private Reader reader;
    private StringBuilder temp = new StringBuilder();
    private StringBuilder readContentTemp = new StringBuilder();

    protected XmlReader(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        init();
    }

    protected XmlReader(String xml) {
        buffer = xml.toCharArray();
        this.length = buffer.length;
    }

    private void init() throws IOException {
        buffer = new char[128 * 1024];
        Charset charset = null;
        boolean xmlTag = false;
        byte[] byteBuffer = new byte[512];
        this.length = inputStream.read(byteBuffer);
        ByteBuffer bb = ByteBuffer.allocate(byteBuffer.length);
        bb.put(byteBuffer);
        SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
        for (Entry<String, Charset> entry : availableCharsets.entrySet()) {
            bb.rewind();
            Charset tempCharset = entry.getValue();
            String s = tempCharset.decode(bb).toString();
            if (s.indexOf("<?") > -1) {
                int indice = s.indexOf("?>");
                if (indice > -1) {
                    s = s.substring(0, indice);
                    if ((indice = s.indexOf("encoding")) > -1) {
                        int i = s.indexOf("\"", indice);
                        int j = s.indexOf("\"", i + 1);
                        String encoding = s.substring(i + 1, j);
                        charset = Charset.forName(encoding);
                    }
                    xmlTag = true;
                    break;
                }
            }
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        this.reader = new InputStreamReader(inputStream, charset);

        // Preenche o buffer principal com os dados que já foram lidos
        InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(byteBuffer), charset);
        this.length = in.read(this.buffer, 0, this.length);

        // Preenche o restante do buffer
        int length = this.reader.read(this.buffer, this.length, this.buffer.length - this.length);
        if (length > -1) {
            this.length += length;
        }

        this.position = 0;
        if (xmlTag) {
            readTag(); // pula a tag inicial <? xml ... ?>
        }
    }

    private boolean fillBuffer() {
        try {
            length = reader.read(buffer);
            position = 0;
            return length > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected char read() {
        if (position >= length) {
            boolean filled = fillBuffer();
            if (!filled) {
                throw new RuntimeException("Invalid XML");
            }
        }
        return buffer[position++];
    }
    static int readed = 0;

    protected String readTag() {
        readed++;
        String tagName = null;
        char c;
        try {
            c = read();
        } catch (RuntimeException e) {
            logger.severe("Error reading tag, tags readed: " + readed);
            throw e;
        }
        tagMap.clear();
        temp.setLength(0);
        while (c != TAG_START && c != NULL) {
            c = read();
        }
        boolean firstTime = true; // se está passando pela primeira vez
        do {
            c = read();
            if (firstTime && c == EXCLAMATION) {
                c = read();
                skipComment(c == '-' ? true : false);
                return readTag();
            } else if (Character.isWhitespace(c)) {
                tagName = readTagWhitespace(tagName);
            } else if (Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-' || c == '[' || c == ']') {
                temp.append(c);
            } else {
                if (temp.length() > 0) {
                    if (c == EQUAL) {
                        String name = temp.toString();
                        if (!readString(c)) {
                            return null;
                        }
                        tagMap.put(name, temp.toString());
                        temp.setLength(0);
                    } else {
                        tagName = readTagWhitespace(tagName);
                    }
                }
            }
            if (c == CLOSE) {
                tagMap.put(TAG_MAP_TAG_CLOSE, null);
            }
            firstTime = false;
        } while (c != TAG_END && c != NULL);
        return tagName;
    }

    protected void skipComment(boolean hifen) {
        char c;
        int i = 0;
        while (true) {
            c = read();
            if (hifen && c == '-') {
                i++;
            } else if (c == TAG_END) {
                if (i == 2 || !hifen) {
                    break;
                }
            } else {
                i = 0;
            }
        }
    }

    protected String readTagWhitespace(String tagName) {
        if (temp.length() > 0) {
            String st = temp.toString();
            if (tagName == null) {
                tagName = st;
                tagMap.put(TAG_MAP_TAG_NAME, tagName);
            }
            tagMap.put(st, null);
            temp.setLength(0);
        }
        return tagName;
    }

    protected boolean readString(char c) {
        while (c != NULL && c != STRING_DELIMITER) {
            c = read();
        }
        c = read();
        temp.setLength(0);
        while (c != NULL && c != STRING_DELIMITER) {
            temp.append(c);
            c = read();
        }
        return true;
    }

    protected String readContent(String tagName) {
        if (tagName.equals(tagMap.get(TAG_MAP_TAG_NAME)) && tagMap.containsKey(TAG_MAP_TAG_CLOSE)) {
            return null;
        }
        char c = read();
        int openedTags = 1;
        temp.setLength(0);
        while (c != NULL) {
            if (c == TAG_START) {
                readContentTemp.setLength(0);
                if ((c = read()) == CLOSE) {
                    while ((c = read()) != TAG_END) {
                        readContentTemp.append(c);
                    }
                    if (readContentTemp.toString().equals(tagName) && --openedTags == 0) {
                        break;
                    } else {
                        temp.append(TAG_START).append(CLOSE).append(readContentTemp).append(c);
                    }
                } else if (c == EXCLAMATION) {
                    readCData();
                } else {
                    temp.append(TAG_START);
                    readContentTemp.setLength(0);
                    readContentTemp.append(c);
                    while ((c = read()) != TAG_END) {
                        readContentTemp.append(c);
                    }
                    if (readContentTemp.toString().equals(tagName)) {
                        openedTags++;
                    }
                    temp.append(readContentTemp);
                    temp.append(TAG_END);
                }
            } else {
                temp.append(c);
            }
            c = read();
        }
        return temp.toString();
    }

    private void readCData() {
        readContentTemp.setLength(0);
        char c = read();
        if (c == '[') {
            while (Character.isLetter(c = read())) {
                readContentTemp.append(c);
            }
            if (!"CDATA".equals(readContentTemp.toString())) {
                temp.append(TAG_START).append(EXCLAMATION).append('[').append(readContentTemp).append(c);
            } else {
                do {
                    c = read();
                    if (c == ']') {
                        c = read();
                        if (c == ']') {
                            c = read();
                            if (c == TAG_END) {
                                break;
                            } else {
                                temp.append(']').append(']').append(c);
                            }
                        } else {
                            temp.append(']').append(c);
                        }
                    } else {
                        temp.append(c);
                    }
                } while (true);
            }
        } else if (c == '-') {
            skipComment(true);
        }
    }

    protected void skipTag(String subTagName) {
        if (logger.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Procurando final da tag " + subTagName + "\n");
            for (Entry<String, String> entry : tagMap.entrySet()) {
                sb.append("\n");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
            logger.info(sb.toString());
        }
        while (!isEndTag(subTagName, true)) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(" tag atual " + tagMap.get(TAG_MAP_TAG_NAME));
            }
        }
        if (logger.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Final da tag  " + subTagName + "\n");
            for (Entry<String, String> entry : tagMap.entrySet()) {
                sb.append("\n");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
            logger.info(sb.toString());
        }
    }

    protected boolean isEndTag(String tagName, boolean canReadTag) {
        try {
            if (tagMap.containsKey(TAG_MAP_TAG_CLOSE) && tagName.equals(tagMap.get(TAG_MAP_TAG_NAME))) {
                return true;
            } else if (canReadTag) {
                String s = readTag();
                if (s != null && s.equals(tagName)) {
                    return tagMap.containsKey(TAG_MAP_TAG_CLOSE);
                }
            }
            return false;
        } catch (RuntimeException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Tag " + tagName + " not closed.\nLast tag info:");
            for (Entry<String, String> entry : tagMap.entrySet()) {
                sb.append("\n");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
            logger.severe(sb.toString());
            throw new RuntimeException("Tag " + tagName + " not closed", e);
        }
    }

}
