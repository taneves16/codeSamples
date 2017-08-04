package com.company.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.company.manager.AdNetworkDataManager;
import com.company.manager.TestStatusManager;
import com.company.manager.implementation.AdNetworkDataManagerImpl;
import com.company.manager.implementation.TestStatusManagerImpl;
import com.company.test.dao.TestDAO;
import com.company.test.exception.InvalidConfigurationException;
import com.company.test.exception.InvalidParameterException;
import com.company.test.exception.TestDAOException;
import com.company.test.bean.AdvertiserBean;
import com.company.test.util.Constants;
import com.company.test.util.Util;
import com.company.test.util.ThreadPool;

/**
 * Is the main entry point for the process. Determines the process type,the parameters and starts the process accordingly. Manages the
 * runs and reports accordingly.
 * 
 */
public class TestMain {

    private Utils utilObj = null;
    private TestDAO daoObj = null;

    private List<AdvertiserBean> mainBeansList = null;
    private List<LocalDate> datesList = null;

    private TestProcessType testProcessType;
    private ThreadPool threadPool;
    private Constants constants;
    private AdNetworkDataManager dataManager;
    private TestStatusManager statusManager;
    private PGSimpleDataSource reportingDataSource;
    private PGSimpleDataSource coreDataSource;

    private static String processName;
    private static String processType;
    private static String startDate;
    private static String endDate;
    
    private static String coreDBURL;
    private static String coreDBName;
    private static int coreDBPort;
    private static String coreDBUname;
    private static String coreDBpwd;
    
    private static String reportingDBURL;
    private static String reportingDBName;
    private static int reportingDBPort;
    private static String reportingDBUname;
    private static String reportingDBpwd;
    
    private static Logger logger = LoggerFactory.getLogger(TestMain.class);

    private TestMain() {
        utilObj = new Util();

        mainBeansList = new ArrayList<AdvertiserBean>();
        datesList = new ArrayList<LocalDate>();
    }

    /**
     * Method will read the conf file passed and using that initialize the constants required for the application.
     * 
     * @param confFileName -> The name of the config file, as passed while starting the process, from which the config 
     * properties are to be read.
     * @throws IOException
     * @throws InvalidConfigurationException -> In case any checks for the required constants fails.
     */
    
    private void initiateConstants(final String confFileName) throws IOException, InvalidConfigurationException {
        constants = Constants.getConstantsObject(confFileName);

        processName = constants.getString("PROCESS_NAME");
        if (StringUtils.isEmpty(processName))
            throw new InvalidConfigurationException("PROCESS_NAME");

        processType = constants.getString("TEST_PROCESS_TYPE");
        if (StringUtils.isEmpty(processType))
            throw new InvalidConfigurationException("TEST_PROCESS_TYPE");

        startDate = constants.getString("START_DATE");
        if (StringUtils.isEmpty(startDate))
            throw new InvalidConfigurationException("START_DATE");

        endDate = constants.getString("END_DATE");
        if (StringUtils.isEmpty(endDate.isEmpty))
            throw new InvalidConfigurationException("END_DATE");

        coreDBURL = constants.getString("SERVER_NAME");
        coreDBName = constants.getString("DB_NAME");
        coreDBPort = constants.getInteger("PORT_NUMBER");
        coreDBUname = constants.getString("USER_NAME");
        coreDBpwd = constants.getString("PASSWORD");
        
        if(StringUtils.isEmpty(coreDBURL) || StringUtils.isEmpty(coreDBName) || coreDBPort <= 0 || StringUtils.isEmpty(coreDBUname))
            throw new InvalidConfigurationException("CORE_DB_CONFIGURATIONS");
        
        reportingDBURL = constants.getString("REP_SERVER_NAME");
        reportingDBName = constants.getString("REP_DB_NAME");
        reportingDBPort = constants.getInteger("REP_PORT_NUMBER");
        reportingDBUname = constants.getString("REP_USER_NAME");
        reportingDBpwd = constants.getString("REP_PASSWORD");
        
        if(StringUtils.isEmpty(reportingDBURL) || StringUtils.isEmpty(reportingDBName) || reportingDBPort <= 0 || StringUtils.isEmpty(reportingDBUname))
            throw new InvalidConfigurationException("REPORTING_DB_CONFIGURATIONS");

        networkInClause = constants.getString("ZEDO_NT_INCLAUSE");
        networkNotInClause = constants.getString("ZEDO_NT_NOT_INCLAUSE");
        adnetworkInClause = constants.getString("ADNETWORK_NT_INCLAUSE");
        adnetworkNotInClause = constants.getString("ADNETWORK_NT_NOT_INCLAUSE");

    }

    /**
     * Will initialize the dataManager and statusManager object with the required data sources.
     */
    private void initializeManagers(){
        
        if(null == dataManager)
            dataManager = new AdNetworkDataManagerImpl(reportingDataSource, coreDataSource);
        if(null == statusManager)
            statusManager = new TestStatusManagerImpl(coreDataSource);
    }
    
    private void initializeObjects(){

        /*
         * INITIALIZE THE DATA SOURCES
         */
        reportingDataSource = new PGSimpleDataSource();
        reportingDataSource.setServerName(reportingDBURL);
        reportingDataSource.setDatabaseName(reportingDBName);
        reportingDataSource.setPortNumber(reportingDBPort);
        reportingDataSource.setUser(reportingDBUname);
        reportingDataSource.setPassword(reportingDBpwd);
        
        coreDataSource = new PGSimpleDataSource();
        coreDataSource.setServerName(coreDBURL);
        coreDataSource.setDatabaseName(coreDBName);
        coreDataSource.setPortNumber(coreDBPort);
        coreDataSource.setUser(coreDBUname);
        coreDataSource.setPassword(coreDBpwd);
        
        daoObj = new TestDAO(coreDataSource, reportingDataSource);
    }
    
    /**
     * Contains the main flow logic for the entire process
     * 
     * @param args
     */
    public static void main(String[] args) {

        TestMain TestMain = new TestMain();
        String confFileName = null;

        /**
         * #1. Possible Failure Scenario :: i. name of conf file is not passed along as a parameter when executing the process. ii. name of the
         * conf file passed is not present in the conf folder. iii. IO Exception while reading the conf file.
         */
        try {
            confFileName = args[0];
            TestMain.initiateConstants(confFileName);
        } catch (IOException e) {
            logger.error("Failure while initializing the process." + "\nCause: Error while reading the conf file."
                    + "\nPossible Solution:" + "\n1. Check if the correct parameter was passed while starting the process."
                    + "\n2. Value received by the process was : " + confFileName + " this should not be null OR empty."
                    + "\n3. If value is not NULL or EMPTY; ensure that a file with this name is present at the location.");
            TestMain.terminate();
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            logger.error("Failure while initializing the process." + "\nCause: Required parameter has not been passed as argument."
                    + "\nSolution:" + "\nCheck if conf file is present. "
                    + "\nIf present then re-start the process");
            TestMain.terminate();
        } catch (InvalidConfigurationException ice) {
            logger.error("Failure while initializing the process." + "\nCause: Required Configuration parameter not available."
                    + "\nDetail: Parameter : " + ice.getNameOfConfigWithInvalidParam() + " Could not be determined");
            TestMain.terminate();
        }
        
        /**
         * #2. Possible Failure Scenario :: The conf file does not contain the expected parameters for initiating the Required objects.
         */
        try{
            TestMain.initializeObjects();
        } catch (RuntimeException re) {
            logger.error("Exception while initializing the Required Objects --> " + re);
            TestMain.terminate();
        }
        TestMain.initializeManagers();

        try {

            /**
             * 1. Determine process type. The testProcessType Object, will provide us with the path for the process template
             */
            TestMain.testProcessType = TestMain.utilObj.determineTestProcessType(processType);
            
            /**
             * 2. Based on the process type, set the start and end dates.
             */
            TestMain.datesList = TestMain.utilObj.getListofDates(TestMain.testProcessType, startDate, endDate);

            TestMain.mainBeansList = TestMain.fetchListOfAllAdvertiserBeans();

                try {
                    if (TestMain.mainBeansList != null && !TestMain.mainBeansList.isEmpty()) {
                        /**
                         * 9. Create Thread pool of the test process type class Pool is created specific to the test process type.
                         */
                        TestMain.threadPool = new ThreadPool(5,
                                testProcessType.lookUpProcessPath(TestMain.testProcessType), TestMain.utilObj, TestMain.daoObj,
                                TestMain.dataManager, TestMain.statusManager);

                        /**
                         * 10. Add the Beans to the pool and start
                         */
                        for (AdvertiserBean sBean : TestMain.mainBeansList) {
                            TestMain.threadPool.runTask(sBean);
                        }

                    }
                } catch (Exception e) {
                    logger.error("Error in TestMain --> " + e.getMessage());
                    throw new Exception(e);
                } finally {
                    if (TestMain.threadPool != null) {
                        try {
                            // close the pool and wait for all tasks to finish.
                            TestMain.threadPool.join();
                            TestMain.threadPool = null;
                        } catch (Exception e) {
                            logger.error("Some Exception thrown --> " + e);
                        }
                    }
                }

        } catch (Exception ex) {
            logger.error("Error in TestMain --> " + ex.getMessage() ,ex.getCause());
            TestMain.terminate();
        } finally {
            logger.info(" REMOVING ALL THE THREAD IN JVM ");
            TestMain.terminate();
        }
    }

    /**
     * . Method will fetch all advertisers which are to be processed. 1. Fetch all the advertisers which are eligible for processing. 
     * 2. Update the Status. 
     * 
     * @throws TestDAOException
     * @throws InvalidParameterException
     */
    private void fetchListOfAllAdvertiserBeans() throws TestDAOException,
            InvalidParameterException{

        logger.info("In Method to fetch list of all Advertisers ");

        // Fetch initial list of all eligible advertiser beans
        mainBeansList = utilObj.getAdvertiserBeansList(daoObj, testProcessType);

        // Update the status
        switch (testProcessType) {
        case DAILY: {
            utilObj.setStatusForDaily(mainBeansList, statusManager);
        }
            break;
        case CUSTOM_DATE: {
            utilObj.setStatusForCustom(mainBeansList, statusManager);
        }
            break;

        default: {
            /*
             * CURRENTLY FOR PAST_DATE; DO NOTHING
             */
        }
            break;
        }
        
    }

    private void terminate() {
        utilObj = null;
        daoObj = null;
        mainBeansList.clear();
        mainBeansList = null;
        datesList.clear();
        datesList = null;
        if (null != constants)
            constants.destroyConstantsObject();

        System.exit(1);
    }

}