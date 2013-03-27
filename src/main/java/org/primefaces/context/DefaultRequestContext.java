/*
 * Copyright 2009-2012 Prime Teknoloji.
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
package org.primefaces.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.config.ConfigContainer;
import org.primefaces.util.WidgetBuilder;
import org.primefaces.visit.ResetInputVisitCallback;

public class DefaultRequestContext extends RequestContext {

    private final static String ATTRIBUTES_KEY = "ATTRIBUTES";
    private final static String CALLBACK_PARAMS_KEY = "CALLBACK_PARAMS";
    private final static String EXECUTE_SCRIPT_KEY = "EXECUTE_SCRIPT";
    private final static String CONFIG_KEY = ConfigContainer.class.getName();

    private Map<String, Object> attributes;
    private WidgetBuilder widgetBuilder;
    private FacesContext context;
    private ConfigContainer config;

    public DefaultRequestContext(FacesContext context) {
    	this.context = context;
    	this.attributes = new HashMap<String, Object>();
    	this.widgetBuilder = new WidgetBuilder();

    	// get config from application map
    	this.config = (ConfigContainer) context.getExternalContext().getApplicationMap().get(CONFIG_KEY);
    	if (this.config == null) {
    		this.config = new ConfigContainer(context);
			context.getExternalContext().getApplicationMap().put(CONFIG_KEY, this.config);
    	}
    }

    @Override
    public boolean isAjaxRequest() {
        return context.getPartialViewContext().isAjaxRequest();
    }

    @Override
    public void addCallbackParam(String name, Object value) {
        getCallbackParams().put(name, value);
    }

    @Override
    public void execute(String script) {
        getScriptsToExecute().add(script);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCallbackParams() {
        if(attributes.get(CALLBACK_PARAMS_KEY) == null) {
            attributes.put(CALLBACK_PARAMS_KEY, new HashMap<String, Object>());
        }
        return (Map<String, Object>) attributes.get(CALLBACK_PARAMS_KEY);
    }

    @Override
	@SuppressWarnings("unchecked")
    public List<String> getScriptsToExecute() {
        if(attributes.get(EXECUTE_SCRIPT_KEY) == null) {
            attributes.put(EXECUTE_SCRIPT_KEY, new ArrayList());
        }
        return (List<String>) attributes.get(EXECUTE_SCRIPT_KEY);
    }

    @Override
	public WidgetBuilder getWidgetBuilder() {
        return widgetBuilder;
    }

    @Override
    public void scrollTo(String clientId) {
        this.execute("PrimeFaces.scrollTo('" + clientId +  "');");
    }

    @Override
    public void update(String clientId) {
    	context.getPartialViewContext().getRenderIds().add(clientId);
    }

    @Override
    public void update(Collection<String> collection) {
    	context.getPartialViewContext().getRenderIds().addAll(collection);
    }

    @Override
    public void reset(Collection<String> ids) {
        EnumSet<VisitHint> hints = EnumSet.of(VisitHint.SKIP_UNRENDERED);
        VisitContext visitContext = VisitContext.createVisitContext(context, null, hints);
        VisitCallback visitCallback = new ResetInputVisitCallback();
        UIViewRoot root = context.getViewRoot();

        for(String id : ids) {
            UIComponent targetComponent = root.findComponent(id);
            if(targetComponent == null) {
                throw new FacesException("Cannot find component with identifier \"" + id + "\" referenced from viewroot.");
            }

            targetComponent.visitTree(visitContext, visitCallback);
        }
    }

    @Override
    public void reset(String id) {
        Collection<String> list = new ArrayList<String>();
        list.add(id);

        reset(list);
    }

    @Override
    public void returnFromDialog(Object data) {
        Map<String,Object> session = context.getExternalContext().getSessionMap();
        Map<String,String> params = context.getExternalContext().getRequestParameterMap();
        String dcid = params.get("dcid");
        session.put(dcid, data);

        this.execute("PrimeFaces.hideDialog({dcid:'" + dcid + "'});");
    }
    
    @Override
    public void release() {
        attributes = null;
        widgetBuilder = null;;
        context = null;
        config = null;
    	
    	setCurrentInstance(null);
    }

	@Override
	public ConfigContainer getConfig() {
		return config;
	}

    @Override
    public Map<Object, Object> getAttributes() {
        if(attributes.get(ATTRIBUTES_KEY) == null) {
            attributes.put(ATTRIBUTES_KEY, new HashMap<Object, Object>());
        }
        return (Map<Object, Object>) attributes.get(ATTRIBUTES_KEY);
    }
}
