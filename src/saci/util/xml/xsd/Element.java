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

@XmlType(name = "xs:element", propOrder = { "complexType" })
public class Element {
	protected String javaName;
	
	@XmlAttribute(name = "name")
	protected String name;
	@XmlAttribute(name = "type")
	protected String type;
	@XmlAttribute(name = "maxOccurs")
	protected String maxOccurs;
	@XmlAttribute(name = "minOccurs")
	protected String minOccurs;
	@XmlAttribute(name = "use")
	protected String use;
	@XmlAttribute(name = "ref")
	protected String ref;
	@XmlAttribute(name = "nillable")
	protected String nillable;
	@XmlElement(name = "xs:complexType")
	protected ComplexType complexType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMaxOccurs() {
		return maxOccurs;
	}

	public void setMaxOccurs(String maxOccurs) {
		this.maxOccurs = maxOccurs;
	}

	public String getMinOccurs() {
		return minOccurs;
	}

	public void setMinOccurs(String minOccurs) {
		this.minOccurs = minOccurs;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}
	
	public ComplexType getComplexType() {
		return complexType;
	}

	public void setComplexType(ComplexType complexType) {
		this.complexType = complexType;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
	
	public String getNillable() {
		return nillable;
	}

	public void setNillable(String nillable) {
		this.nillable = nillable;
	}

	public String getJavaName() {
		return javaName;
	}

	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}
	
}
