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

package saci.util.xml.xsd;

import saci.util.xml.XmlAttribute;
import saci.util.xml.XmlElement;
import saci.util.xml.XmlType;

@XmlType(name = "xs:extension", propOrder = { "sequence" })
public class Extension {
	@XmlAttribute(name = "base")
	protected String base;
	@XmlElement(name = "xs:sequence")
	protected Sequence sequence;
	
	public String getBase() {
		return base;
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	public Sequence getSequence() {
		return sequence;
	}
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

}
