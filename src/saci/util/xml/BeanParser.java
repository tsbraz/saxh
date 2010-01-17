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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import saci.util.Types;

class BeanParser extends XmlReader {

    protected class ComplexType {
        Class<?> type;
        Class<?> keyType;
        boolean isArray;
        @SuppressWarnings("unchecked")
        Collection list;
        @SuppressWarnings("unchecked")
        Map map;
        boolean isPrintable;
    }

    public BeanParser(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    public BeanParser(String xml) {
        super(xml);
    }

    public <T> T fillBean(Class<T> bean) {
        try {
            T newType = bean.newInstance();
            fillBean(newType, null);
            return newType;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean fillBean(Object bean, String tagName) throws IllegalAccessException, InstantiationException {
        return fillBean(bean, tagName, null);
    }

    @SuppressWarnings("unchecked")
    protected boolean fillBean(Object bean, String tagName, String fieldName) throws IllegalAccessException, InstantiationException {
        XmlMap map = mapper.mapBean(bean);
        if (Types.isNullOrEmpty(tagName)) {
            tagName = readTag();
        }
        if (fieldName == null) {
            fieldName = map.name;
        }
        if (fieldName.equals(tagName)) {
            for (FieldMap field : map.attributes) {
                setFieldValue(bean, field.printable, field.field, tagMap.get(field.name));
            }
            if (tagMap.containsKey(TAG_MAP_TAG_CLOSE)) {
                return true;
            }
            boolean subTagReaded = false;
            while (!isEndTag(tagName, !subTagReaded)) {
                String subTagName = tagMap.get(TAG_MAP_TAG_NAME);
                subTagReaded = false;
                if (map.fieldMap.containsKey(subTagName)) {
                    FieldMap field = map.fieldMap.get(subTagName);
                    if (field.printable) {
                        setFieldValue(bean, field.printable, field.field, readContent(subTagName));
                    } else {
                        ComplexType complexType = newComplexType(field.field, bean);
                        if (complexType.list != null) {
                            subTagReaded = fillList(complexType, subTagName, field, bean);
                        } else if (complexType.map != null) {
                            fillMap(subTagName, complexType, field);
                        } else {
                            if (complexType.isPrintable) {
                                String s = readContent(subTagName);
                                setFieldValue(bean, true, field.field, s);
                            } else {
                                Object newType = complexType.type.newInstance();
                                fillBean(newType, subTagName, field.name);
                                setFieldValue(bean, field.printable, field.field, newType);
                            }
                        }
                    }
                } else {
                    // busca o final da tag descartada
                    skipTag(subTagName);
                }
            }
            return true;
        }
        return false;
    }

    private boolean fillList(ComplexType complexType, String subTagName, FieldMap field, Object bean) throws IllegalAccessException, NegativeArraySizeException, InstantiationException {
        boolean subTagReaded;
        boolean objectFilled;
        do {
            objectFilled = false;
            if (complexType.isPrintable) {
                String s = readContent(subTagName);
                complexType.list.add(Types.cast(s, complexType.type));
            } else {
                Object newType = complexType.type.newInstance();
                if (fillBean(newType, subTagName, field.name)) {
                    complexType.list.add(newType);
                }
            }
            subTagReaded = true;
            String newSubTagName = readTag();
            if (subTagName.equals(newSubTagName)) {
                objectFilled = true;
            }
        } while (objectFilled);
        if (complexType.isArray) {
            Object[] o = (Object[]) Array.newInstance(complexType.type, complexType.list.size());
            complexType.list.toArray(o);
            setFieldValue(bean, false, field.field, o);
        }
        return subTagReaded;
    }

    private void fillMap(String subTagName, ComplexType complexType, FieldMap field) throws InstantiationException, IllegalAccessException, RuntimeException {
        while (!isEndTag(subTagName, true)) {
            if ("Entry".equals(tagMap.get(TAG_MAP_TAG_NAME))) {
                readTag();
                if ("key".equals(tagMap.get(TAG_MAP_TAG_NAME))) {
                    Object key = null;
                    if (!Types.isPrintable(complexType.keyType)) {
                        key = complexType.type.newInstance();
                        mapper.mapBean(key).name = "key";
                        fillBean(key, "key", field.name);
                    } else {
                        key = readContent("key");
                    }
                    readTag();
                    if ("value".equals(tagMap.get(TAG_MAP_TAG_NAME))) {
                        Object value = null;
                        if (!Types.isPrintable(complexType.type)) {
                            value = complexType.type.newInstance();
                            mapper.mapBean(value).name = "value";
                            fillBean(value, "value", field.name);
                        } else {
                            value = readContent("value");
                        }
                        complexType.map.put(key, value);
                        skipTag("Entry");
                    } else {
                        throw new RuntimeException("Invalid Map. No Value Found.");
                    }
                } else {
                    throw new RuntimeException("Invalid Map. No Key Found.");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected ComplexType newComplexType(Field field, Object instance) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = field.getType();
        ComplexType complexType = new ComplexType();
        if (Collection.class.isAssignableFrom(clazz)) {
            ParameterizedType type = null;
            Type listType = null;
            try {
                type = (ParameterizedType) field.getGenericType();
                listType = type.getActualTypeArguments()[0];
            } catch (Exception e) {
                throw new RuntimeException("Collection has no defined type");
            }
            if (listType instanceof Class<?>) {
                complexType.type = (Class<?>) listType;
            } else {
                complexType.type = (Class<?>) ((ParameterizedType) listType).getRawType();
            }
            Object o = field.get(instance);
            if (o == null) {
                complexType.list = new ArrayList();
                setFieldValue(instance, false, field, complexType.list);
            } else {
                complexType.list = (Collection) o;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            ParameterizedType type = null;
            Type keyType = null;
            Type valueType = null;
            try {
                type = (ParameterizedType) field.getGenericType();
                keyType = type.getActualTypeArguments()[0];
                valueType = type.getActualTypeArguments()[1];
            } catch (Exception e) {
                throw new RuntimeException("Map has no defined type");
            }
            complexType.type = (Class<?>) valueType;
            complexType.keyType = (Class<?>) keyType;
            Object o = field.get(instance);
            if (o == null) {
                complexType.map = new HashMap();
                setFieldValue(instance, false, field, complexType.map);
            } else {
                complexType.map = (Map) o;
            }
        } else if (clazz.isArray()) {
            complexType.isArray = true;
            complexType.type = clazz.getComponentType();
            complexType.list = new ArrayList();
            Object o = field.get(instance);
            if (o != null) {
                complexType.list.addAll(Arrays.asList((Object[]) o));
            }
        } else {
            complexType.type = clazz;
        }
        complexType.isPrintable = Types.isPrintable(complexType.type);
        return complexType;
    }

    protected void setFieldValue(Object bean, boolean cast, Field field, Object value) {
        if (!Modifier.isFinal(field.getModifiers())) {
            value = cast ? Types.cast(value, field.getType()) : value;
            try {
                field.set(bean, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }
    }
}
