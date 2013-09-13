import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;
import javax.el.ValueExpression;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.Constants;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;

    private final static String DEFAULT_EVENT = "tabChange";

    public final static String CONTAINER_CLASS = "ui-accordion ui-widget ui-helper-reset ui-hidden-container";
    public final static String ACTIVE_TAB_HEADER_CLASS = "ui-accordion-header ui-helper-reset ui-state-default ui-state-active ui-corner-top";
    public final static String TAB_HEADER_CLASS = "ui-accordion-header ui-helper-reset ui-state-default ui-corner-all";
    public final static String TAB_HEADER_ICON_CLASS = "ui-icon ui-icon-triangle-1-e";
    public final static String TAB_HEADER_ICON_RTL_CLASS = "ui-icon ui-icon-triangle-1-w";
    public final static String ACTIVE_TAB_HEADER_ICON_CLASS = "ui-icon ui-icon-triangle-1-s";
    public final static String ACTIVE_TAB_CONTENT_CLASS = "ui-accordion-content ui-helper-reset ui-widget-content";
    public final static String INACTIVE_TAB_CONTENT_CLASS = "ui-accordion-content ui-helper-reset ui-widget-content ui-helper-hidden";

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList("tabChange","tabClose"));

    public boolean isContentLoadRequest(FacesContext context) {
        return context.getExternalContext().getRequestParameterMap().containsKey(this.getClientId(context) + "_contentLoad");
    }

    private boolean isRequestSource(FacesContext context) {
        return this.getClientId(context).equals(context.getExternalContext().getRequestParameterMap().get(Constants.RequestParams.PARTIAL_SOURCE_PARAM));
    }

	public Tab findTab(String tabClientId) {
        for(UIComponent component : getChildren()) {
            if(component.getClientId().equals(tabClientId))
                return (Tab) component;
        }

        return null;
    }

    List<Tab> loadedTabs;
    public List<Tab> getLoadedTabs() {
        if(loadedTabs == null) {
            loadedTabs = new ArrayList<Tab>();

            for(UIComponent component : getChildren()) {
                if(component instanceof Tab) {
                    Tab tab =  (Tab) component;
                    
                    if(tab.isLoaded())
                        loadedTabs.add(tab);
                }
            }
        }

        return loadedTabs;
    }

    @Override
    public void queueEvent(FacesEvent event) {
        FacesContext context = getFacesContext();

        if(isRequestSource(context) && event instanceof AjaxBehaviorEvent) {
            Map<String,String> params = context.getExternalContext().getRequestParameterMap();
            String eventName = params.get(Constants.RequestParams.PARTIAL_BEHAVIOR_EVENT_PARAM);
            String clientId = this.getClientId(context);

            AjaxBehaviorEvent behaviorEvent = (AjaxBehaviorEvent) event;

            if(eventName.equals("tabChange")) {
                String tabClientId = params.get(clientId + "_newTab");
                TabChangeEvent changeEvent = new TabChangeEvent(this, behaviorEvent.getBehavior(), findTab(tabClientId));

                if(this.getVar() != null) {
                    int index = Integer.parseInt(params.get(clientId + "_tabindex"));
                    setRowIndex(index);
                    changeEvent.setData(this.getRowData());
                    changeEvent.setTab((Tab) getChildren().get(0));
                    setRowIndex(-1);
                }

                changeEvent.setPhaseId(behaviorEvent.getPhaseId());
                super.queueEvent(changeEvent);
            }
            else if(eventName.equals("tabClose")) {
                String tabClientId = params.get(clientId + "_tabId");
                TabCloseEvent closeEvent = new TabCloseEvent(this, behaviorEvent.getBehavior(), findTab(tabClientId));

                if(this.getVar() != null) {
                    int index = Integer.parseInt(params.get(clientId + "_tabindex"));
                    setRowIndex(index);
                    closeEvent.setData(this.getRowData());
                    closeEvent.setTab((Tab) getChildren().get(0));
                    setRowIndex(-1);
                }

                closeEvent.setPhaseId(behaviorEvent.getPhaseId());
                super.queueEvent(closeEvent);
            }
        }
        else {
            super.queueEvent(event);
        }
    }

    @Override
    public void processDecodes(FacesContext context) {
        if(!isRendered()) {
            return;
        }

        if(isDynamic()) {
            if(this.getVar() == null) {
            	pushComponentToEL(context, null);
                for(Tab tab : getLoadedTabs()) {
                    tab.processDecodes(context);
                }
                this.decode(context);
                popComponentFromEL(context);
            }
            else {
                super.processDecodes(context);
            }
        }
        else {
            if(this.getVar() == null) {
            	pushComponentToEL(context, null);
            	ComponentUtils.processDecodesOfFacetsAndChilds(this, context);
                this.decode(context);
                popComponentFromEL(context);
            }
            else {
                super.processDecodes(context);
            }
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        if(!isRendered()) {
            return;
        }

        //only process loaded tabs on dynamic case
        if(isDynamic()) {
            if(this.getVar() == null) {
                for(Tab tab : getLoadedTabs()) {
                    tab.processValidators(context);
                }
            } 
            else {
                super.processValidators(context);
            }
        }
        else {
            if(this.getVar() == null) {
            	ComponentUtils.processValidatorsOfFacetsAndChilds(this, context);
            }
            else {
                super.processValidators(context);
            }
        }
    }

    @Override
    public void processUpdates(FacesContext context) {
        if(!isRendered()) {
            return;
        }

        ValueExpression expr = this.getValueExpression("activeIndex");
        if(expr != null) {
            expr.setValue(getFacesContext().getELContext(), getActiveIndex());
            resetActiveIndex();
        }

        //only process loaded tabs on dynamic case
        if(isDynamic()) {
            if(this.getVar() == null) {
                for(Tab tab : getLoadedTabs()) {
                    tab.processUpdates(context);
                }
            }
            else {
                super.processUpdates(context);
            }
        }
        else {
            if(this.getVar() == null) {
            	ComponentUtils.processUpdatesOfFacetsAndChilds(this, context);
            }
            else {
                super.processUpdates(context);
            }  
        }
    }

    protected void resetActiveIndex() {
		getStateHelper().remove(PropertyKeys.activeIndex);
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT;
    }

    @Override
    public boolean visitTree(VisitContext context,  VisitCallback callback) {
    
        if(this.getVar() == null) {
            if (!isVisitable(context))
                return false;

            FacesContext facesContext = context.getFacesContext();
            pushComponentToEL(facesContext, null);

            try {
                VisitResult result = context.invokeVisitCallback(this, callback);

                if (result == VisitResult.COMPLETE)
                  return true;

                if (result == VisitResult.ACCEPT) {
                    Iterator<UIComponent> kids = this.getFacetsAndChildren();

                    while(kids.hasNext()) {
                        boolean done = kids.next().visitTree(context, callback);

                        if (done)
                            return true;
                    }
                }
            }
            finally {
                popComponentFromEL(facesContext);
            }

            return false;
        }
        else {
            return super.visitTree(context, callback);
        }
    }

    public boolean isRTL() {
        return this.getDir().equalsIgnoreCase("rtl");
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {
        if(this.getVar() == null) {
            if (null == context || null == clientId || null == callback) {
                throw new NullPointerException();
            }

            boolean found = false;
            if (clientId.equals(this.getClientId(context))) {
                try {
                    this.pushComponentToEL(context, this);
                    callback.invokeContextCallback(context, this);
                    return true;
                } catch (Exception e) {
                    throw new FacesException(e);
                } finally {
                    this.popComponentFromEL(context);
                }
            } else {
                Iterator<UIComponent> itr = this.getFacetsAndChildren();

                while (itr.hasNext() && !found) {
                    found = itr.next().invokeOnComponent(context, clientId,
                            callback);
                }
            }
            return found;
        }
        else {
            return super.invokeOnComponent(context, clientId, callback);
        }
    }