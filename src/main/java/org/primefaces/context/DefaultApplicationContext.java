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
package org.primefaces.context;

import java.util.HashMap;

import javax.faces.context.FacesContext;

import org.primefaces.config.ConfigContainer;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.StringEncrypter;
import org.primefaces.util.WidgetBuilder;

public class DefaultApplicationContext extends ApplicationContext {

	private ConfigContainer config;

    public DefaultApplicationContext(FacesContext context) {
    	this.config = new ConfigContainer(context);
    }

	@Override
	public ConfigContainer getConfig() {
		return config;
	}
}
