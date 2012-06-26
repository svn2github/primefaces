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
package org.primefaces.component.datatable.feature;

import java.io.IOException;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableRenderer;

public class ResizableColumnsFeature implements DataTableFeature {

    public boolean isEnabled(FacesContext context, DataTable table) {
        return table.isResizableColumns();
    }

    public void decode(FacesContext context, DataTable table) {
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String clientId = table.getClientId();
        
        String columnId = params.get(clientId + "_columnId");
        String width = params.get(clientId + "_width");
        Column column = table.findColumn(columnId);
        
        column.setWidth(Integer.parseInt(width));
    }
    
    public void encode(FacesContext context, DataTableRenderer renderer, DataTable table) throws IOException {
        Map<String,String> params = context.getExternalContext().getRequestParameterMap();
        int editedRowId = Integer.parseInt(params.get(table.getClientId(context) + "_rowEditIndex"));

        table.setRowIndex(editedRowId);
        if(table.isRowAvailable()) {
            renderer.encodeRow(context, table, table.getClientId(context), editedRowId, table.getRowIndexVar());
        }
    }
}