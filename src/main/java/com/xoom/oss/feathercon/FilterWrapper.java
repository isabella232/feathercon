package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.FilterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterWrapper {
    public final FilterHolder filterHolder;
    public final List<String> pathSpec;
    public final EnumSet<DispatcherType> dispatches;

    private FilterWrapper(FilterHolder filterHolder, List<String> pathSpec, EnumSet<DispatcherType> dispatches) {
        this.filterHolder = filterHolder;
        this.pathSpec = Collections.unmodifiableList(pathSpec);
        this.dispatches = dispatches;
    }

    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);
        Class<? extends Filter> filterClass;
        Map<String, String> initParams = new HashMap<String, String>();
        List<String> pathSpecs = new ArrayList<String>();
        EnumSet<DispatcherType> dispatcherTypeSet = EnumSet.of(DispatcherType.REQUEST);

        private boolean built;

        public Builder withFilterClass(Class<? extends Filter> filterClass) {
            this.filterClass = filterClass;
            return this;
        }

        public Builder withInitParameter(String key, String value) {
            initParams.put(key, value);
            return this;
        }

        public Builder withPathSpec(String pathSpec) {
            pathSpecs.add(pathSpec);
            return this;
        }

        public Builder withDispatcherTypeSet(EnumSet<DispatcherType> dispatcherTypeSet) {
            this.dispatcherTypeSet = dispatcherTypeSet;
            return this;
        }

        public FilterWrapper build() {
            if (built) {
                throw new IllegalStateException("This builder can be used to produce one filter wrapper instance.  Please create a new builder.");
            }
            if (filterClass == null) {
                throw new IllegalStateException("Cannot build filter wrapper without a filter class");
            }
            if (pathSpecs.isEmpty()) {
                logger.warn("Filter {} has no pathSpecs, therefore this filter will not handle any requests.", filterClass);
            }
            FilterHolder filterHolder = new FilterHolder(filterClass);
            for (String key : initParams.keySet()) {
                filterHolder.setInitParameter(key, initParams.get(key));
            }
            built = true;
            logger.info("Built {}", this);
            return new FilterWrapper(filterHolder, pathSpecs, dispatcherTypeSet);
        }

        @Override
        public String toString() {
            return "FilterWrapperBuilder{" +
                    "filterClass=" + filterClass +
                    ", initParams=" + initParams +
                    ", pathSpecs=" + pathSpecs +
                    ", dispatcherTypeSet=" + dispatcherTypeSet +
                    ", built=" + built +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FilterWrapper{" +
                "filterHolder=" + filterHolder +
                ", pathSpec=" + pathSpec +
                ", dispatches=" + dispatches +
                '}';
    }
}
