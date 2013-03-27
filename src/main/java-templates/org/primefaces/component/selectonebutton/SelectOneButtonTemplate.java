import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

    public final static String STYLE_CLASS = "ui-selectonebutton ui-buttonset ui-widget ui-corner-all";

    private final static String DEFAULT_EVENT = "change";

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList(DEFAULT_EVENT));

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT;
    }