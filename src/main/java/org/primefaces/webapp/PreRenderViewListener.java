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
package org.primefaces.webapp;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.primefaces.util.Constants;
import org.primefaces.util.WidgetBuilder;

public class PreRenderViewListener implements SystemEventListener {

    public boolean isListenerForSource(Object source) {
        return true;
    }

    public void processEvent(SystemEvent event) throws AbortProcessingException {
        FacesContext.getCurrentInstance().getAttributes().put(Constants.WIDGET_BUILDER_ATTR, new WidgetBuilder());
    }
}
