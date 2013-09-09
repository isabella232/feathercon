package com.xoom.oss.feathercon;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;
import java.util.List;

class FilterWrapper {
    public final Class<? extends Filter> filterClass;
    public final List<String> pathSpec;
    public final EnumSet<DispatcherType> dispatches;

    public FilterWrapper(Class<? extends Filter> filterClass, List<String> pathSpec, EnumSet<DispatcherType> dispatches) {
        this.filterClass = filterClass;
        this.pathSpec = pathSpec;
        this.dispatches = dispatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterWrapper that = (FilterWrapper) o;

        if (!dispatches.equals(that.dispatches)) return false;
        if (!filterClass.equals(that.filterClass)) return false;
        if (!pathSpec.equals(that.pathSpec)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterClass.hashCode();
        result = 31 * result + pathSpec.hashCode();
        result = 31 * result + dispatches.hashCode();
        return result;
    }
}
