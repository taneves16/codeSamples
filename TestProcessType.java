package com.company.test;

import java.util.EnumSet;
import java.util.HashMap;

import com.company.test.exception.InvalidParameterException;

public enum TestProcessType {

    DAILY("com.company.test.run.DailyRun", "normal"), CUSTOM_DATE("com.company.test.run.CustomDateRun",
            "cdr"), PAST_DATE("com.company.test.run.PastDateRun", "pdr");

    private final String processTemplatePath;
    private final String processName;

    private TestProcessType(String processTemplatePath, String processName) {
        this.processTemplatePath = processTemplatePath;
        this.processName = processName;
    }

    private static final HashMap<TestProcessType, String> processPathLookUp = new HashMap<TestProcessType, String>();
    private static final HashMap<String, TestProcessType> processNameLookUp = new HashMap<String, TestProcessType>();

    static {
        for (TestProcessType spt : EnumSet.allOf(TestProcessType.class)) {
            processPathLookUp.put(spt, spt.processTemplatePath);
            processNameLookUp.put(spt.processName, spt);
        }
    }

    public String getProcessTypeName() {
        return this.processName;
    }

    public static String lookUpProcessPath(TestProcessType spt) {
        return processPathLookUp.get(spt);
    }

    public static TestProcessType lookUpProcessName(String processName) throws InvalidParameterException {
        if (processNameLookUp.containsKey(processName))
            return processNameLookUp.get(processName);
        else
            throw new InvalidParameterException("Process Name configured is invalid");
    }
}
