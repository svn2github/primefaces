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
package org.primefaces.component.poll;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;

import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.ComponentUtils;

public class PollRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Poll poll = (Poll) component;

        if(context.getExternalContext().getRequestParameterMap().containsKey(poll.getClientId(context))) {
            ActionEvent event = new ActionEvent(poll);
            if(poll.isImmediate())
                event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
            else
                event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            
            poll.queueEvent(event);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Poll poll = (Poll) component;
        String clientId = poll.getClientId(context);
        String widgetVar = poll.resolveWidgetVar();

        UIComponent form = ComponentUtils.findParentForm(context, poll);
        if(form == null) {
            throw new FacesException("Poll:" + clientId + " needs to be enclosed in a form component");
        }

        //wrap complete handler to handle server side stop
        if(poll.getValueExpression("stop") != null) {
            String userOncomplete = poll.getOncomplete();
            String defaultOncomplete = widgetVar + ".handleComplete(xhr, status, args);";
            String oncomplete = userOncomplete == null ? defaultOncomplete : userOncomplete + ";" + defaultOncomplete;

            poll.setOncomplete(oncomplete);
        }

        //dummy markup
        writer.startElement("span", null);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("style", "display:none", "style");
        writer.endElement("span");

        //script
        writer.startElement("script", null);
        writer.writeAttribute("type", "text/javascript", null);

        writer.write("$(function() {");
        writer.write(widgetVar + "= new PrimeFaces.widget.Poll('" + clientId + "', {");
        writer.write("frequency:" + poll.getInterval());
        writer.write(",autoStart:" + poll.isAutoStart());
        writer.write(",fn: function() {");
        writer.write(buildAjaxRequest(context, poll));
        writer.write("}});});");

        writer.endElement("script");
    }
}