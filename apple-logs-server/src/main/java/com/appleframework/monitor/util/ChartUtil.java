package com.appleframework.monitor.util;

import com.google.common.collect.Lists;
import com.appleframework.monitor.model.MetricValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hill.Hu
 */
@SuppressWarnings("rawtypes")
public class ChartUtil {
	
    private static SimpleDateFormat sdf = new SimpleDateFormat("M月dd HH:mm");

    /**
     * example：
     * [[time, request, pay],
     * [7月01 00:00, 10.0, 0.0],
     * [8月01 00:00, 12.0, 2.0],
     * [9月01 00:00, 13.0, 2.0]]
     *
     * @param metricList
     * @return
     */
	public static List<List> format(List<List<MetricValue>> metricList) {
        return format(metricList,true);
    }
	
	public static List<List> format(List<List<MetricValue>> metricList,boolean timeAsStr) {
        ArrayList<List> rows = Lists.newArrayList();
        if (metricList == null || metricList.isEmpty())
            return rows;
        List<MetricValue> max = metricList.get(0);
        for (int i = 0; i < metricList.size(); i++) {
            List<MetricValue> list = metricList.get(i);
            if (list.size() > max.size())
                max = list;
            if (list.isEmpty())
                metricList.remove(list);
        }

        rows.add(getColumnNames(metricList));
        for (MetricValue metricValue : max) {
            Object time = metricValue.getTimeStamp();
            if(timeAsStr){
                time = sdf.format(new Date(metricValue.getTimeStamp()));
            }

            List<Object> row = Lists.newArrayList(time);
            for (List<MetricValue> list : metricList) {
                row.add(findValue(metricValue.getTimeStamp(), list));
            }
            rows.add(row);
        }
        return rows;
    }
    
    private static List<String> getColumnNames(List<List<MetricValue>> metricList) {
        List<String> columns = Lists.newArrayList("time");
        for (List<MetricValue> list : metricList) {
            if(list.size()>0){
                String metricName = list.get(0).getName();
                columns.add(metricName);
            }
        }
        return columns;
    }

    /**
     *  find near value
     * @param timeStamp
     * @param list
     * @return
     */
    private static double findValue(long timeStamp, List<MetricValue> list) {
        double value = 0;
        for (MetricValue mv : list) {
            if (mv.getTimeStamp() <= timeStamp) {
                value = mv.getValue();
            }
        }
        return value;
    }

}
