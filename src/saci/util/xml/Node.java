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

import java.util.HashMap;
import java.util.Iterator;

public class Node implements Iterable<Node> {

    private HashMap<String, String> attributes;
    private String name;
    private boolean hasChild;
    private String content;

    protected Node() {
    }

    public Iterator<Node> iterator() {
        if (hasChild) {
            StringBuilder content = new StringBuilder();
            content.append(this.content);
            content.append("</");
            content.append(name);
            content.append(">");
            return new Parser(content.toString(), attributes).iterator();
        }
        return null;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    protected void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean hasChild() {
        return hasChild;
    }

    protected void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }
}
