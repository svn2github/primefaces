/*
 * Copyright 2009-2013 PrimeTek.
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
 */
package org.primefaces.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.component.*;
import javax.faces.component.visit.VisitHint;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.validator.BeanValidator;
import org.primefaces.component.api.RTLAware;
import org.primefaces.component.api.Widget;
import org.primefaces.config.ConfigContainer;
import org.primefaces.context.RequestContext;

public class ComponentUtils {

	public static final EnumSet<VisitHint> VISIT_HINTS_SKIP_UNRENDERED = EnumSet.of(VisitHint.SKIP_UNRENDERED);
	
	/**
	 * Algorithm works as follows;
	 * - If it's an input component, submitted value is checked first since it'd be the value to be used in case validation errors
	 * terminates jsf lifecycle
	 * - Finally the value of the component is retrieved from backing bean and if there's a converter, converted value is returned
	 *
	 * @param context			FacesContext instance
	 * @param component			UIComponent instance whose value will be returned
	 * @return					End text
	 */
	public static String getValueToRender(FacesContext context, UIComponent component) {
		if(component instanceof ValueHolder) {
			
			if(component instanceof EditableValueHolder) {
                EditableValueHolder input = (EditableValueHolder) component;
                Object submittedValue = input.getSubmittedValue();
                ConfigContainer config = RequestContext.getCurrentInstance().getApplicationContext().getConfig();
                
                if(config.isInterpretEmptyStringAsNull() && submittedValue == null && context.isValidationFailed() && !input.isValid()) {
                    return null;
                }
                else if(submittedValue != null) {
                    return submittedValue.toString();
                }
			}

			ValueHolder valueHolder = (ValueHolder) component;
			Object value = valueHolder.getValue();
            
            //format the value as string
            if(value != null) {
                Converter converter = getConverter(context, component);
                
                if(converter != null)
                    return converter.getAsString(context, component, value);
                else
                    return value.toString();    //Use toString as a fallback if there is no explicit or implicit converter
                
            }
            else {
                //component is a value holder but has no value
                return null;
            }
		}
        
        //component it not a value holder
        return null;
	}
    
    /**
	 * Finds appropriate converter for a given value holder
	 * 
	 * @param context			FacesContext instance
	 * @param component			ValueHolder instance to look converter for
	 * @return					Converter
	 */
    public static Converter getConverter(FacesContext context, UIComponent component) {
    	if (!(component instanceof ValueHolder)) {
    		return null;
    	}

    	Converter converter = ((ValueHolder) component).getConverter();
    	if (converter != null) {
    		return converter;
    	}

    	ValueExpression valueExpression = component.getValueExpression("value");
    	if (valueExpression == null) {
    		return null;
    	}

    	Class<?> converterType = valueExpression.getType(context.getELContext());
    	if (converterType == null || converterType == Object.class) {
    		// no conversion is needed
    		return null;
    	}

        if (converterType == String.class
        		&& !RequestContext.getCurrentInstance().getApplicationContext().getConfig().isStringConverterAvailable()) {
        	return null;
        }

    	return context.getApplication().createConverter(converterType);
    }
    
	public static UIComponent findParentForm(FacesContext context, UIComponent component) {
		UIComponent parent = component.getParent();
		
		while(parent != null) {
			if(parent instanceof UIForm) {
				return parent;
            }
		
			parent = parent.getParent();
		}
		
		return null;
	}
    
    public static UniqueIdVendor findParentUniqueIdVendor(UIComponent component) {
        UIComponent parent = component.getParent();

        while(parent != null) {
            if(parent instanceof UniqueIdVendor) {
                return (UniqueIdVendor) parent;
            }
            
            parent = parent.getParent();
        }
        
        return null;
    }
    
    public static UIComponent findParentNamingContainer(UIComponent component) {
        UIComponent parent = component.getParent();

        while(parent != null) {
            if(parent instanceof NamingContainer) {
                return (UIComponent) parent;
            }
            
            parent = parent.getParent();
        }
        
        return null;
    }
	
	public static void decorateAttribute(UIComponent component, String attribute, String value) {
		String attributeValue = (String) component.getAttributes().get(attribute);
		
		if(attributeValue != null) {
			if(attributeValue.indexOf(value) == -1) {
				String decoratedValue = attributeValue + ";" + value;
				
				component.getAttributes().put(attribute, decoratedValue);
			} else {
				component.getAttributes().put(attribute, attributeValue);
			}
		} else {
				component.getAttributes().put(attribute, value);
		}
	}

	public static List<SelectItem> createSelectItems(UIComponent component) {
		List<SelectItem> items = new ArrayList<SelectItem>();
		Iterator<UIComponent> children = component.getChildren().iterator();
		
		while(children.hasNext()) {
			UIComponent child = children.next();
			
			if(child instanceof UISelectItem) {
				UISelectItem selectItem = (UISelectItem) child;
				
				items.add(new SelectItem(selectItem.getItemValue(), selectItem.getItemLabel()));
			} else if(child instanceof UISelectItems) {
				Object selectItems = ((UISelectItems) child).getValue();
			
				if(selectItems instanceof SelectItem[]) {
					SelectItem[] itemsArray = (SelectItem[]) selectItems;
					
					for(SelectItem item : itemsArray)
						items.add(new SelectItem(item.getValue(), item.getLabel()));
					
				} else if(selectItems instanceof Collection) {
					Collection<SelectItem> collection = (Collection<SelectItem>) selectItems;
					
					for(SelectItem item : collection)
						items.add(new SelectItem(item.getValue(), item.getLabel()));
				}
			}
		}
		
		return items;
	}

	public static String escapeJQueryId(String id) {
		return "#" + id.replaceAll(":", "\\\\\\\\:");
	}
	
	@Deprecated
	public static String findClientIds(FacesContext context, UIComponent component, String list) {
		if(list == null) {
			return "@none";
        }

		String[] ids = list.split("[,\\s]+");
		StringBuilder buffer = new StringBuilder();
		
		for(int i = 0; i < ids.length; i++) {
			String id = ids[i].trim();
			
			if (isValueBlank(id)) {
				continue;
			}

			if(i != 0 && buffer.length() > 0) {
				buffer.append(" ");
            }

			if(id.equals("@all") || id.equals("@none")) {
				buffer.append(id);
            }
            else if(id.equals("@this")) {
                buffer.append(component.getClientId(context));
            }
            else if(id.equals("@form")) {
                UIComponent form = ComponentUtils.findParentForm(context, component);
                if(form == null) {
                    throw new FacesException("Component " + component.getClientId(context) + " needs to be enclosed in a form");
                } 
			
                buffer.append(form.getClientId(context));
            }
            else if(id.equals("@parent")) {
                buffer.append(component.getParent().getClientId(context));
            }
            else if(id.equals("@namingcontainer")) {
                UIComponent container = ComponentUtils.findParentNamingContainer(component);
                
                if(container != null) {
                    buffer.append(container.getClientId(context));
                } 
            }
			else {
				UIComponent comp = component.findComponent(id);
				if(comp != null) {
					buffer.append(comp.getClientId(context));
                }
				else {
                    throw new FacesException("Cannot find component with identifier \"" + id + "\" referenced from \"" + component.getClientId(context) + "\".");
                }
			}
		}
		
		String clientIds = buffer.toString();

		if (isValueBlank(clientIds)) {
			return "@none";
		}

		return clientIds;
	}
	
	public static String findComponentClientId(String id) {
	    FacesContext facesContext = FacesContext.getCurrentInstance();
	    UIComponent component = findComponent(facesContext.getViewRoot(), id);

	    return component.getClientId(facesContext);
	}
	
	public static UIComponent findComponent(UIComponent base, String id) {
	    if (id.equals(base.getId()))
	      return base;
	  
	    UIComponent kid = null;
	    UIComponent result = null;
	    Iterator<UIComponent> kids = base.getFacetsAndChildren();
	    while (kids.hasNext() && (result == null)) {
	      kid = (UIComponent) kids.next();
	      if (id.equals(kid.getId())) {
	        result = kid;
	        break;
	      }
	      result = findComponent(kid, id);
	      if (result != null) {
	        break;
	      }
	    }
	    return result;
	}

    public static String getWidgetVar(String id) {
	    UIComponent component = findComponent(FacesContext.getCurrentInstance().getViewRoot(), id);

        if(component == null) {
            throw new FacesException("Cannot find component " + id + " in view.");
        } else if(component instanceof Widget) {
            return ((Widget) component).resolveWidgetVar();
        } else {
            throw new FacesException("Component with id " + id + " is not a Widget");
        }

	}
	
	public static boolean isLiteralText(UIComponent component) {
		return component.getFamily().equalsIgnoreCase("facelets.LiteralText");
	}
    
    /**
     * Implementation from Apache Commons Lang
     */
    public static Locale toLocale(String str) {
        if(str == null) {
            return null;
        }
        int len = str.length();
        if(len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if(ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if(len == 2) {
            return new Locale(str, "");
        } else {
            if(str.charAt(2) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            char ch3 = str.charAt(3);
            if(ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if(ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if(len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if(str.charAt(5) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }

    public static boolean isValueBlank(String value) {
		if(value == null)
			return true;

		return value.trim().equals("");
    }
    
    public static boolean isRTL(FacesContext context, RTLAware component) {
        boolean globalValue = RequestContext.getCurrentInstance().getApplicationContext().getConfig().isRightToLeft();
        
        return globalValue||component.isRTL();
    }

    public static void processDecodesOfFacetsAndChilds(UIComponent component, FacesContext context) {
    	if (component.getFacetCount() > 0) {
    		for (UIComponent facet : component.getFacets().values()) {
    			facet.processDecodes(context);
    		}
    	}

    	if (component.getChildCount() > 0) {
	    	for (int i = 0, childCount = component.getChildCount(); i < childCount; i++) {
	    		UIComponent child = component.getChildren().get(i);
	    		child.processDecodes(context);
	    	}
    	}
    }
    
    public static void processValidatorsOfFacetsAndChilds(UIComponent component, FacesContext context) {
    	if (component.getFacetCount() > 0) {
    		for (UIComponent facet : component.getFacets().values()) {
    			facet.processValidators(context);
    		}
    	}

    	if (component.getChildCount() > 0) {
	    	for (int i = 0, childCount = component.getChildCount(); i < childCount; i++) {
	    		UIComponent child = component.getChildren().get(i);
	    		child.processValidators(context);
	    	}
    	}
    }
    
    public static void processUpdatesOfFacetsAndChilds(UIComponent component, FacesContext context) {
    	if (component.getFacetCount() > 0) {
    		for (UIComponent facet : component.getFacets().values()) {
    			facet.processUpdates(context);
    		}
    	}

    	if (component.getChildCount() > 0) {
	    	for (int i = 0, childCount = component.getChildCount(); i < childCount; i++) {
	    		UIComponent child = component.getChildren().get(i);
	    		child.processUpdates(context);
	    	}
    	}
    }
    
    public static NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        String outcomeValue = (outcome == null) ? context.getViewRoot().getViewId() : outcome;
        
        return navHandler.getNavigationCase(context, null, outcomeValue);
    }
}