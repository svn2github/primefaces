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
package org.primefaces.component.galleria;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.renderkit.CoreRenderer;

public class GalleriaOverlayRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        GalleriaOverlay overlay = (GalleriaOverlay) component;
        ResponseWriter writer = context.getResponseWriter();
        String title = overlay.getTitle();

        writer.startElement("div", overlay);
        writer.writeAttribute("class", "gv-panel-overlay", null);

        if(title != null) {
            writer.startElement("h3", overlay);
            writer.writeText(title, null);
            writer.endElement("h3");
        }

        writer.startElement("p", overlay);
        renderChildren(context, overlay);
        writer.endElement("p");


        writer.endElement("div");
    }

     @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
