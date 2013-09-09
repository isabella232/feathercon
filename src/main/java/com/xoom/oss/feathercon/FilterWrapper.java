package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FilterWrapper {
    public final FilterHolder filterHolder;
    public final List<String> pathSpec;
    public final EnumSet<DispatcherType> dispatches;

    public FilterWrapper(FilterHolder filterHolder, List<String> pathSpec, EnumSet<DispatcherType> dispatches) {
        this.pathSpec = pathSpec;
        this.dispatches = dispatches;
        this.filterHolder = filterHolder;
    }

    public static class FilterWrapperBuilder {
        Map<String, String> initParams = new HashMap<String, String>();
        List<String> pathSpecs = new ArrayList<String>();

        Class<? extends Filter> filterClass;
        private EnumSet<DispatcherType> dispatcherTypeSet;

        public FilterWrapperBuilder withFilterClass(Class<? extends Filter> filterClass) {
            this.filterClass = filterClass;
            return this;
        }

        public FilterWrapperBuilder withInitParameter(String key, String value) {
            initParams.put(key, value);
            return this;
        }

        public FilterWrapperBuilder withPathSpec(String pathSpec) {
            pathSpecs.add(pathSpec);
            return this;
        }

        public FilterWrapperBuilder withDispatcherTypeSet(EnumSet<DispatcherType> dispatcherTypeSet) {
            this.dispatcherTypeSet = dispatcherTypeSet;
            return this;
        }

        FilterWrapper build() {
            FilterHolder filterHolder = new FilterHolder(filterClass);
            for (String key : initParams.keySet()) {
                filterHolder.setInitParameter(key, initParams.get(key));
            }
            return new FilterWrapper(filterHolder, pathSpecs, dispatcherTypeSet);
        }
    }
}
