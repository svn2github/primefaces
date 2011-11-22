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
package org.primefaces.component.selectbooleancheckbox;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.renderkit.InputRenderer;

public class SelectBooleanCheckboxRenderer extends InputRenderer {

    @Override
	public void decode(FacesContext context, UIComponent component) {
		SelectBooleanCheckbox checkbox = (SelectBooleanCheckbox) component;

        if(checkbox.isDisabled()) {
            return;
        }

        decodeBehaviors(context, checkbox);

		String clientId = checkbox.getClientId(context);
		String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId + "_input");

        if(submittedValue != null && submittedValue.equalsIgnoreCase("on")) {
            checkbox.setSubmittedValue("true");
        }
        else {
            checkbox.setSubmittedValue("false");
        }
	}

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SelectBooleanCheckbox checkbox = (SelectBooleanCheckbox) component;

        encodeMarkup(context, checkbox);
        encodeScript(context, checkbox);
    }

    protected void encodeMarkup(FacesContext context, SelectBooleanCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);
        Boolean checked = (Boolean) checkbox.getValue();
        boolean disabled = checkbox.isDisabled();

        String style = checkbox.getStyle();
        String styleClass = checkbox.getStyleClass();
        styleClass = styleClass == null ? SelectBooleanCheckbox.STYLE_CLASS : SelectBooleanCheckbox.STYLE_CLASS + " " + styleClass;
        styleClass = disabled ? styleClass + " ui-state-disabled" : styleClass;

        writer.startElement("div", checkbox);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", styleClass, "styleClass");
        if(style != null) {
            writer.writeAttribute("style", style, "style");
        }

        encodeInput(context, checkbox, clientId, checked, disabled);
        encodeOutput(context, checkbox, checked);
        encodeItemLabel(context, checkbox, clientId);

        writer.endElement("div");
    }

    protected void encodeInput(FacesContext context, SelectBooleanCheckbox checkbox, String clientId, boolean checked, boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String inputId = clientId + "_input";
        
        writer.startElement("div", checkbox);
        writer.writeAttribute("class", SelectBooleanCheckbox.CHECKBOX_INPUT_WRAPPER_CLASS, null);
        
        writer.startElement("input", null);
        writer.writeAttribute("id", inputId, "id");
        writer.writeAttribute("name", inputId, null);
        writer.writeAttribute("type", "checkbox", null);

        if(checked) writer.writeAttribute("checked", "checked", null);
        if(disabled) writer.writeAttribute("disabled", "disabled", null);
        if(checkbox.getOnchange() != null) writer.writeAttribute("onchange", checkbox.getOnchange(), null);

        writer.endElement("input");

        writer.endElement("div");
    }

    protected void encodeOutput(FacesContext context, SelectBooleanCheckbox checkbox, boolean checked) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String styleClass = SelectBooleanCheckbox.CHECKBOX_BOX_CLASS;
        styleClass = checked ? styleClass + " ui-state-active" : styleClass;

        String iconClass = SelectBooleanCheckbox.CHECKBOX_ICON_CLASS;
        iconClass = checked ? iconClass + " " + SelectBooleanCheckbox.CHECKBOX_CHECKED_ICON_CLASS : iconClass;

        writer.startElement("div", null);
        writer.writeAttribute("class", styleClass, null);

        writer.startElement("span", null);
        writer.writeAttribute("class", iconClass, null);
        writer.endElement("span");

        writer.endElement("div");
    }
    
    protected void encodeItemLabel(FacesContext context, SelectBooleanCheckbox checkbox, String clientId) throws IOException {
        String label = checkbox.getItemLabel();
        
        if(label != null) {
            ResponseWriter writer = context.getResponseWriter();
            
            writer.startElement("span", null);
            writer.writeAttribute("class", SelectBooleanCheckbox.LABEL_CLASS, null);
            writer.writeText(label, "itemLabel");
            writer.endElement("span");
        }
    }

    protected void encodeScript(FacesContext context, SelectBooleanCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);

        startScript(writer, clientId);
        
        writer.write("PrimeFaces.cw('SelectBooleanCheckbox','" + checkbox.resolveWidgetVar() + "',{");
        writer.write("id:'" + clientId + "'");
        
        encodeClientBehaviors(context, checkbox);

        writer.write("});");

        endScript(writer);
    }
}
