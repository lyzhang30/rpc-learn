package cn.zly.rpcstudy.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhanglianyong
 * 2023-08-21 19:50
 */
public class FilterChain {

    private List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void addFilter(List<Object> filterList) {
        for (Object filter : filterList) {
            addFilter((Filter) filter);
        }
    }

    public void doFilter(FilterData data) {
        for (Filter filter : filters) {
            filter.doFilter(data);
        }
    }
}
