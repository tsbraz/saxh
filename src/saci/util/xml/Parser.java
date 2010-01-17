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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import static saci.util.xml.XmlReader.*;

public class Parser implements Iterable<Node> {

    private XmlReader reader;
    private String mainTag;
    private HashMap<String, String> mainTagAttributes;

    public Parser(InputStream inputStream) throws IOException {
        reader = new XmlReader(inputStream);
        mainTag = reader.readTag();
    }

    protected Parser(String xml, HashMap<String, String> tagMap) {
        reader = new XmlReader(xml);
        reader.tagMap = tagMap;
        mainTag = tagMap.get(TAG_MAP_TAG_NAME);
        mainTagAttributes = cloneMap();
    }

    /**
     * Iterator dos nós principais
     */
    public Iterator<Node> iterator() {
        return new Iterator<Node>() {

            boolean readNeeded = true;
            boolean iterated = false;

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Node next() {
                if (readNeeded) {
                    reader.readTag();
                }
                readNeeded = true;
                iterated = true;
                Node node = new Node();
                node.setName(reader.tagMap.get(TAG_MAP_TAG_NAME));
                if (!reader.tagMap.containsKey(TAG_MAP_TAG_CLOSE)) {
                    StringBuilder content = new StringBuilder();
                    boolean hasChild = readContent(content);
                    node.setContent(content.toString());
                    node.setHasChild(hasChild);
                }
                node.setAttributes(cloneMap());
                return node;
            }

            @Override
            public boolean hasNext() {
                if (iterated && mainTag.equals(reader.tagMap.get(TAG_MAP_TAG_NAME)) && reader.tagMap.containsKey(TAG_MAP_TAG_CLOSE)) {
                    return false;
                }
                if (readNeeded) {
                    reader.readTag();
                    readNeeded = false;
                }
                return !reader.isEndTag(mainTag, false);
            }
        };
    }

    protected boolean readContent(StringBuilder content) {
        String tagName = reader.tagMap.get(TAG_MAP_TAG_NAME);
        String s = reader.readContent(tagName);
        content.append(s);
        boolean hasChild = s.indexOf("<") > -1;
        return hasChild;
    }

    @SuppressWarnings("unchecked")
    protected HashMap<String, String> cloneMap() {
        HashMap<String, String> cloneTagMap = (HashMap) reader.tagMap.clone();
        return cloneTagMap;
    }

    public String getMainTagName() {
        return mainTag;
    }

    public HashMap<String, String> getMainTagAttributes() {
        return mainTagAttributes;
    }
}
