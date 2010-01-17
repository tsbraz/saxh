package ${packageName};

import saci.util.xml.*;

@XmlType(name="${name}"<#if propOrder?if_exists != "">, propOrder={${propOrder}}</#if>)
public <#if abstract == "true">abstract </#if>class ${className}<#if extension?if_exists != ""> extends ${extension}</#if> {
	<#list attributes?if_exists as attribute>
	@XmlAttribute(name="${attribute.name}"<#if attribute.use == "required">, required=true</#if>)
	protected ${attribute.type} ${attribute.javaName};
	</#list>
	<#list elements?if_exists as element>
	@XmlElement(name="${element.name}"<#if element.use == "required">, required=true</#if><#if element.nillable == "true">, nillable=true</#if>)
	protected ${element.type} ${element.javaName};
	</#list>
	
	<#list attributes?if_exists as attribute>
	public void set${attribute.javaName?cap_first}(${attribute.type} value) {
		this.${attribute.javaName} = value;
	}
	
	public ${attribute.type} get${attribute.javaName?cap_first}() {
		return this.${attribute.javaName};
	}
	</#list>
	
	<#list elements?if_exists as element>
	public void set${element.javaName?cap_first}(${element.type} value) {
		this.${element.javaName} = value;
	}
	
	public ${element.type} get${element.javaName?cap_first}() {
		return this.${element.javaName};
	}
	</#list>

}