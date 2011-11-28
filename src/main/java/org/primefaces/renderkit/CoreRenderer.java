/*
 * Copyright 2009-2011 Prime Technology.
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
package org.primefaces.renderkit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.primefaces.component.api.AjaxSource;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.Constants;

public class CoreRenderer extends Renderer {
	
	protected void renderChildren(FacesContext facesContext, UIComponent component) throws IOException {
		for (Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
			UIComponent child = (UIComponent) iterator.next();
			renderChild(facesContext, child);
		}
	}

	protected void renderChild(FacesContext facesContext, UIComponent child) throws IOException {
		if (!child.isRendered()) {
			return;
		}

		child.encodeBegin(facesContext);
		
		if (child.getRendersChildren()) {
			child.encodeChildren(facesContext);
		} else {
			renderChildren(facesContext, child);
		}
		child.encodeEnd(facesContext);
	}
	
	protected String getActionURL(FacesContext facesContext) {
		String actionURL = facesContext.getApplication().getViewHandler().getActionURL(facesContext, facesContext.getViewRoot().getViewId());
		
		return facesContext.getExternalContext().encodeActionURL(actionURL);
	}
	
    protected String getResourceURL(FacesContext facesContext, String value) {
        if (value.contains(ResourceHandler.RESOURCE_IDENTIFIER)) {
            return value;
        } else {
            String url = facesContext.getApplication().getViewHandler().getResourceURL(facesContext, value);

            return facesContext.getExternalContext().encodeResourceURL(url);
        }
    }
    
    protected String getResourceRequestPath(FacesContext facesContext, String resourceName) {
		Resource resource = facesContext.getApplication().getResourceHandler().createResource(resourceName, "primefaces");

        return resource.getRequestPath();
	}
    	
	public boolean isPostback(FacesContext facesContext) {
		return facesContext.getRenderKit().getResponseStateManager().isPostback(facesContext);
	}

    public boolean isAjaxRequest(FacesContext facesContext) {
		return facesContext.getPartialViewContext().isAjaxRequest();
	}

	protected void renderPassThruAttributes(FacesContext facesContext, UIComponent component, String var, String[] attrs) throws IOException {
		ResponseWriter writer = facesContext.getResponseWriter();
		
		for(String event : attrs) {			
			String eventHandler = (String) component.getAttributes().get(event);
			
			if(eventHandler != null)
				writer.write(var + ".addListener(\"" + event.substring(2, event.length()) + "\", function(e){" + eventHandler + ";});\n");
		}
	}
	
	protected void renderPassThruAttributes(FacesContext facesContext, UIComponent component, String[] attrs) throws IOException {
		ResponseWriter writer = facesContext.getResponseWriter();
		
		for(String attribute : attrs) {
			Object value = component.getAttributes().get(attribute);
			
			if(shouldRenderAttribute(value))
				writer.writeAttribute(attribute, value.toString(), attribute);
		}
	}
	
	protected void renderPassThruAttributes(FacesContext facesContext, UIComponent component, String[] attrs, String[] ignoredAttrs) throws IOException {
		ResponseWriter writer = facesContext.getResponseWriter();
		
		for(String attribute : attrs) {
			if(isIgnoredAttribute(attribute, ignoredAttrs)) {
				continue;
			}
			
			Object value = component.getAttributes().get(attribute);
			
			if(shouldRenderAttribute(value))
				writer.writeAttribute(attribute, value.toString(), attribute);
		}
	}
	
	private boolean isIgnoredAttribute(String attribute, String[] ignoredAttrs) {
		for(String ignoredAttribute : ignoredAttrs) {
			if(attribute.equals(ignoredAttribute)) {
				return true;
			}
		}
		
		return false;
	}
	
    protected boolean shouldRenderAttribute(Object value) {
        if(value == null)
            return false;
      
        if(value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        else if(value instanceof Number) {
        	Number number = (Number) value;
        	
            if (value instanceof Integer)
                return number.intValue() != Integer.MIN_VALUE;
            else if (value instanceof Double)
                return number.doubleValue() != Double.MIN_VALUE;
            else if (value instanceof Long)
                return number.longValue() != Long.MIN_VALUE;
            else if (value instanceof Byte)
                return number.byteValue() != Byte.MIN_VALUE;
            else if (value instanceof Float)
                return number.floatValue() != Float.MIN_VALUE;
            else if (value instanceof Short)
                return number.shortValue() != Short.MIN_VALUE;
        }
        
        return true;
    }
    
    protected boolean isPostBack() {
    	FacesContext facesContext = FacesContext.getCurrentInstance();
    	return facesContext.getRenderKit().getResponseStateManager().isPostback(facesContext);
    }
   
    public String getEscapedClientId(String clientId){
    	return clientId.replaceAll(":", "\\\\\\\\:");
    }
    
    public boolean isValueEmpty(String value) {
		if (value == null || "".equals(value))
			return true;
		
		return false;
	}
	
	public boolean isValueBlank(String value) {
		if(value == null)
			return true;
		
		return value.trim().equals("");
	}
	
    protected String buildAjaxRequest(FacesContext context, AjaxSource source) {
        UIComponent component = (UIComponent) source;
        String clientId = component.getClientId(context);
        UIComponent form = ComponentUtils.findParentForm(context, component);
        
        if(form == null) {
            throw new FacesException("Component " + component.getClientId(context) + " must be enclosed in a form.");
        }

        StringBuilder req = new StringBuilder();
        req.append("PrimeFaces.ab(");

        //form
        req.append("{formId:").append("'").append(form.getClientId(context)).append("'");

        //source
        req.append(",source:").append("'").append(clientId).append("'");

        //process
        String process = source.getProcess();
        if(process == null) {
            process = "@all";
        } 
        else {
            process = ComponentUtils.findClientIds(context, component, process);
        }
        req.append(",process:'").append(process).append("'");


        //update
        if(source.getUpdate() != null) {
            req.append(",update:'").append(ComponentUtils.findClientIds(context, component, source.getUpdate())).append("'");
        }

        //async
        if(source.isAsync())
            req.append(",async:true");

        //global
        if(!source.isGlobal())
            req.append(",global:false");

        //callbacks
        if(source.getOnstart() != null)
            req.append(",onstart:function(){").append(source.getOnstart()).append(";}");
        if(source.getOnerror() != null)
            req.append(",onerror:function(xhr, status, error){").append(source.getOnerror()).append(";}");
        if(source.getOnsuccess() != null)
            req.append(",onsuccess:function(data, status, xhr){").append(source.getOnsuccess()).append(";}");
        if(source.getOncomplete() != null)
            req.append(",oncomplete:function(xhr, status, args){").append(source.getOncomplete()).append(";}");

        //params
        boolean paramWritten = false;

        for(UIComponent child : component.getChildren()) {
            if(child instanceof UIParameter) {
                UIParameter parameter = (UIParameter) child;

                if(!paramWritten) {
                    paramWritten = true;
                    req.append(",params:{");
                } else {
                    req.append(",");
                }

                req.append("'").append(parameter.getName()).append("':'").append(parameter.getValue()).append("'");
            }
        }

        if(paramWritten) {
            req.append("}");
        }

        req.append("});");

        return req.toString();
    }
	
	protected String buildNonAjaxRequest(FacesContext facesContext, UIComponent component, String formId, String decodeParam) {		
        StringBuilder request = new StringBuilder();
        Map<String,String> params = new HashMap<String, String>();
        params.put(decodeParam, decodeParam);
		
		for(UIComponent child : component.getChildren()) {
			if(child instanceof UIParameter) {
                UIParameter param = (UIParameter) child;

                params.put(param.getName(), String.valueOf(param.getValue()));
			}
		}
        
        //append params
        request.append("PrimeFaces.addSubmitParam('").append(formId).append("',{");
        for(Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            String value = params.get(key);
            
            request.append("'").append(key).append("':'").append(value).append("'");
            
            if(it.hasNext())
                request.append(",");
        }
        request.append("})");
        
        request.append(".submit('").append(formId).append("');");
		
		return request.toString();
	}

    /**
     * Non-obstrusive way to apply client behaviors.
     * Behaviors are rendered as options to the client side widget and applied by widget to necessary dom element
     */
    protected void encodeClientBehaviors(FacesContext context, ClientBehaviorHolder component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        
        //ClientBehaviors
        Map<String,List<ClientBehavior>> behaviorEvents = component.getClientBehaviors();

        if(!behaviorEvents.isEmpty()) {
            String clientId = ((UIComponent) component).getClientId(context);
            List<ClientBehaviorContext.Parameter> params = Collections.emptyList();

            writer.write(",behaviors:{");

            for(Iterator<String> eventIterator = behaviorEvents.keySet().iterator(); eventIterator.hasNext();) {
                String event = eventIterator.next();
                String domEvent = event;

                if(event.equalsIgnoreCase("valueChange"))       //editable value holders
                    domEvent = "change";
                else if(event.equalsIgnoreCase("action"))       //commands
                    domEvent = "click";

                writer.write(domEvent + ":");

                writer.write("function(event) {");
                for(Iterator<ClientBehavior> behaviorIter = behaviorEvents.get(event).iterator(); behaviorIter.hasNext();) {
                    ClientBehavior behavior = behaviorIter.next();
                    ClientBehaviorContext cbc = ClientBehaviorContext.createClientBehaviorContext(context, (UIComponent) component, event, clientId, params);
                    String script = behavior.getScript(cbc);    //could be null if disabled

                    if(script != null) {
                        writer.write(script);
                    }
                }
                writer.write("}");

                if(eventIterator.hasNext()) {
                    writer.write(",");
                }
            }

            writer.write("}");
        }
    }

    protected boolean themeForms() {
        FacesContext context = FacesContext.getCurrentInstance();
        String value = context.getExternalContext().getInitParameter(Constants.THEME_FORMS_PARAM);

        return value == null ? true : Boolean.valueOf(value);
    }

    protected void decodeBehaviors(FacesContext context, UIComponent component)  {

        if(!(component instanceof ClientBehaviorHolder)) {
            return;
        }

        Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
        if(behaviors.isEmpty()) {
            return;
        }

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String behaviorEvent = params.get("javax.faces.behavior.event");

        if(null != behaviorEvent) {
            List<ClientBehavior> behaviorsForEvent = behaviors.get(behaviorEvent);

            if(behaviorsForEvent != null && !behaviorsForEvent.isEmpty()) {
               String behaviorSource = params.get("javax.faces.source");
               String clientId = component.getClientId();

               if(behaviorSource != null && clientId.startsWith(behaviorSource)) {
                   for(ClientBehavior behavior: behaviorsForEvent) {
                       behavior.decode(context, component);
                   }
               }
            }
        }
    }
    
    protected void startScript(ResponseWriter writer, String clientId) throws IOException {
        writer.startElement("script", null);
        writer.writeAttribute("id", clientId + "_s", null);
        writer.writeAttribute("type", "text/javascript", null);
    }
    
    protected void endScript(ResponseWriter writer) throws IOException {
        writer.endElement("script");
    }
        
    /**
     * Duplicate code from json-simple project under apache license
     * http://code.google.com/p/json-simple/source/browse/trunk/src/org/json/simple/JSONValue.java
     */
    protected String escapeText(String text) {
        if(text == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }
                
        return sb.toString();
    }
    
    protected String getOnclickBehaviors(FacesContext context, ClientBehaviorHolder cbh) {
        List<ClientBehavior> behaviors = cbh.getClientBehaviors().get("action");
        StringBuilder sb = new StringBuilder();
        
        if(behaviors != null && !behaviors.isEmpty()) {
            UIComponent component = (UIComponent) cbh;
            String clientId = component.getClientId(context);
            List<ClientBehaviorContext.Parameter> params = Collections.emptyList();
            
            for(Iterator<ClientBehavior> behaviorIter = behaviors.iterator(); behaviorIter.hasNext();) {
                ClientBehavior behavior = behaviorIter.next();
                ClientBehaviorContext cbc = ClientBehaviorContext.createClientBehaviorContext(context, component, "action", clientId, params);
                String script = behavior.getScript(cbc);

                if(script != null)
                    sb.append(script).append(";");
            }
        }
        
        return sb.length() == 0 ? null : sb.toString();
    }

}