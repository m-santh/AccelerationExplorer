package com.kircherelectronics.accelerationexplorer.datalogger;

/**
 * Created by KircherEngineerH on 4/27/2016.
 */
public interface DataLoggerInterface
{
    public void setHeaders(Iterable<String> headers) throws IllegalStateException;
    public void addRow(Iterable<String> values) throws IllegalStateException;
    public void writeToFile();
}
