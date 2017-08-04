package com.company.test;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

import com.company.manager.AdNetworkDataManager;
import com.company.manager.StatusManager;
import com.company.test.bean.AdvertiserBean;
import com.company.test.dao.TestDAO;
import com.company.test.exception.AdNetworkUIException;
import com.company.test.exception.InvalidParameterException;
import com.company.test.exception.TestDAOException;
import com.company.test.exception.InternalFailureException;
import com.company.test.util.AdNetworkFactory;
import com.company.test.util.Util;

/**
 * Is the main template for the run. All the different processes have to be the children of this class.
 * 
 * Handles the process of getting the data from the reporting UI of the Ad Network and processing it into the required form.
 * 
 */
public abstract class TestRun implements Runnable {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy");
    protected static final LocalDate YESTERDAY = new LocalDate().minusDays(1);
    
    protected Logger logger;

    private AdNetworkFactory adnetworkFactory;

    protected AdvertiserBean adnBean;
    protected TestDAO daoObj;
    protected Util utilObj;
    protected AdNetworkDataManager dataManager;
    protected StatusManager statusManager;
    
    public void setStatusManagerObject(StatusManager statusManager){
        this.statusManager = statusManager;
    }
    
    public void setDataManagerObject(AdNetworkDataManager dataManager){
        this.dataManager = dataManager;
    }

    public void setUtilObj(Util utilObj) {
        this.utilObj = utilObj;
    }

    public void setDaoObj(TestDAO daoObj) {
        this.daoObj = daoObj;
    }

    public void setAdNetworkFactory(AdNetworkFactory adnetworkFactory) {
        this.adnetworkFactory = adnetworkFactory;
    }

    public void setAdvertiserBean(AdvertiserBean sBean) {
        this.adnBean = sBean;
    }

    public AdvertiserBean getsBean() {
        return this.adnBean;
    }

    /**
     * Starts the execute Method.
     */
    public void run() {
        try {
            execute();
        } catch (TestDAOException | AdNetworkUIException | InvalidParameterException e) {
            logger.error("Exception while executing the process", e);
            e.printStackTrace();
        }
    }

    /**
     * Method specifies the flow in which the data is got from the reporting UI 
     * 
     * @throws TestDAOException
     * @throws AdNetworkUIException
     * @throws InvalidParameterException
     */
    public void execute() throws TestDAOException, AdNetworkUIException, InvalidParameterException{


        try {
            adnetworkFactory.getGrabObject(adnBean.getPath()).fetchHtml(adnBean);
            setStatusAndActivityForEachDate(adnBean, StatusType.DONE);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NullPointerException e) {
            logger.error("Exception in Run", e);
            setStatusAndActivityForEachDate(adnBean, StatusType.ERROR);
            return;
        }

    }


    private void setStatusAndActivityForEachDate(final AdvertiserBean sBean, final StatusType statusToSet) {
            for (LocalDate date : sBean.getDatewiseStatusMap().keySet()) {
                sBean.setStatusForDate(date, StatusType.lookUpStatus(statusToSet));
            }
    }

}
