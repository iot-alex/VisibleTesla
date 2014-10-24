/*
 * Prefs.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nov 17, 2013
 */
package org.noroomattheinn.visibletesla;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Range;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.noroomattheinn.utils.CalTime;
import org.noroomattheinn.utils.PWUtils;

/**
 * Prefs - Stores and Manages Preferences data for all of the tabs.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class Prefs {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public enum LoadPeriod {Last7, Last14, Last30, ThisWeek, ThisMonth, All, None};
    
    public static final BiMap<String,LoadPeriod> nameToLoadPeriod = HashBiMap.create();
    static {
        nameToLoadPeriod.put("Last 7 days", LoadPeriod.Last7);
        nameToLoadPeriod.put("Last 14 days", LoadPeriod.Last14);
        nameToLoadPeriod.put("Last 30 days", LoadPeriod.Last30);
        nameToLoadPeriod.put("This week", LoadPeriod.ThisWeek);
        nameToLoadPeriod.put("This month", LoadPeriod.ThisMonth);
        nameToLoadPeriod.put("All", LoadPeriod.All);
        nameToLoadPeriod.put("None", LoadPeriod.None);
    }
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    private final AppContext appContext;
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/

    public Prefs(AppContext ac) {
        appContext = ac;
        loadGeneralPrefs();
        loadGraphPrefs();
        loadSchedulerPrefs();
        loadLocationPrefs();
    }
    
/*------------------------------------------------------------------------------
 *
 * General Application Preferences
 * 
 *----------------------------------------------------------------------------*/
    
    // Basic Preferences
    public IntegerProperty  idleThresholdInMinutes  = new SimpleIntegerProperty();
    public BooleanProperty  wakeOnTabChange         = new SimpleBooleanProperty();
    public StringProperty   loadPeriod              = new SimpleStringProperty();
    public StringProperty   overviewRange           = new SimpleStringProperty();
    public StringProperty   notificationAddress     = new SimpleStringProperty();
    private static final String WakeOnTCKey         = "APP_WAKE_ON_TC";
    private static final String IdleThresholdKey    = "APP_IDLE_THRESHOLD";
    private static final String GraphPeriodPrefKey  = "GRAPH_PERIOD";
    private static final String OverviewRangeKey    = "OVERVIEW_RANGE";
    private static final String NotifyAddressKey    = "NOTIFICATION_ADDR";

    // Advanced
    public BooleanProperty  offerExperimental       = new SimpleBooleanProperty();
    public BooleanProperty  enableProxy             = new SimpleBooleanProperty();
    public StringProperty   proxyHost               = new SimpleStringProperty();
    public IntegerProperty  proxyPort               = new SimpleIntegerProperty();
    public BooleanProperty  useCustomGoogleAPIKey   = new SimpleBooleanProperty();
    public StringProperty   googleAPIKey            = new SimpleStringProperty();
    public BooleanProperty  useCustomMailGunKey     = new SimpleBooleanProperty();
    public StringProperty   mailGunKey              = new SimpleStringProperty();
    public IntegerProperty  fontScale               = new SimpleIntegerProperty();
    public BooleanProperty  enableRest              = new SimpleBooleanProperty();
    public IntegerProperty  restPort                = new SimpleIntegerProperty();
    public StringProperty   authCode                = new SimpleStringProperty();
    public StringProperty   customURLSource         = new SimpleStringProperty();
    private static final String OfferExpKey         = "APP_OFFER_EXP";
    private static final String EnableProxyKey      = "APP_ENABLE_PROXY";
    private static final String ProxyHostKey        = "APP_PROXY_HOST";
    private static final String ProxyPortKey        = "APP_PROXY_PORT";
    private static final String UseCustomGoogleKey  = "APP_USE_CUSTOM_GKEY";
    private static final String CustomGoogleKey     = "APP_CUSTOM_GKEY";
    private static final String UseCustomMailGunKey = "APP_USE_CUSTOM_MGKEY";
    private static final String CustomMailGunKey    = "APP_CUSTOM_MGKEY";
    private static final String FontScaleKey        = "APP_FONT_SCALE";
    private static final String RestPortKey         = "APP_REST_PORT";
    private static final String EnableRestKey       = "APP_ENABLE_REST";
    private static final String AuthCodeKey         = "APP_AUTH_CODE";
    private static final String CustomURLKey        = "APP_CUSTOM_URL";

    // Overrides
    public StringProperty   overideWheelsTo         = new SimpleStringProperty();
    public BooleanProperty  overideWheelsActive     = new SimpleBooleanProperty();
    public StringProperty   overideColorTo          = new SimpleStringProperty();
    public BooleanProperty  overideColorActive      = new SimpleBooleanProperty();
    public StringProperty   overideUnitsTo          = new SimpleStringProperty();
    public BooleanProperty  overideUnitsActive      = new SimpleBooleanProperty();
    public StringProperty   overideModelTo          = new SimpleStringProperty();
    public BooleanProperty  overideModelActive      = new SimpleBooleanProperty();
    public StringProperty   overideRoofTo           = new SimpleStringProperty();
    public BooleanProperty  overideRoofActive       = new SimpleBooleanProperty();
    private static final String ORWheelToKey        = "APP_OWT";
    private static final String ORWheelActiveKey    = "APP_OWA";
    private static final String ORColorToKey        = "APP_OCT";
    private static final String ORColorActiveKey    = "APP_OCA";
    private static final String ORUnitsToKey        = "APP_OUT";
    private static final String ORUnitsActiveKey    = "APP_OUA";
    private static final String ORModelToKey        = "APP_OMT";
    private static final String ORModelActiveKey    = "APP_OMA";
    private static final String ORRoofToKey         = "APP_ORT";
    private static final String ORRoofActiveKey     = "APP_ORA";
    
    private void loadGeneralPrefs() {
        // ----- Basic Preferences
        integerPref(IdleThresholdKey, idleThresholdInMinutes, 15);
        booleanPref(WakeOnTCKey, wakeOnTabChange, true);
        stringPref(NotifyAddressKey, notificationAddress, "");
        stringPref(GraphPeriodPrefKey, loadPeriod, LoadPeriod.All.name());
        stringPref(OverviewRangeKey, overviewRange, "Rated");
        // ----- Advanced Preferences
        booleanPref(OfferExpKey, offerExperimental, false);
        booleanPref(EnableProxyKey, enableProxy, false);
        stringPref(ProxyHostKey, proxyHost, "");
        integerPref(ProxyPortKey, proxyPort, 8080);
        booleanPref(UseCustomGoogleKey, useCustomGoogleAPIKey, false);
        stringPref(CustomGoogleKey, googleAPIKey, AppContext.GoogleMapsAPIKey);
        booleanPref(UseCustomMailGunKey, useCustomMailGunKey, false);
        stringPref(CustomMailGunKey, mailGunKey, AppContext.MailGunKey);
        integerPref(FontScaleKey, fontScale, 100);
        booleanPref(EnableRestKey, enableRest, false);
        integerPref(RestPortKey, restPort, 9090);
        stringPref(CustomURLKey, customURLSource, "");
        stringPref(AuthCodeKey, authCode, "");
        // Break down the external representation into the salt and password
        List<byte[]> internalForm = (new PWUtils()).internalRep(authCode.get());
        appContext.restSalt = internalForm.get(0);
        appContext.restEncPW = internalForm.get(1);
        // ----- Overrides
        stringPref(ORWheelToKey, overideWheelsTo, "From Car");
        booleanPref(ORWheelActiveKey, overideWheelsActive, false);
        stringPref(ORColorToKey, overideColorTo, "From Car");
        booleanPref(ORColorActiveKey, overideColorActive, false);
        stringPref(ORUnitsToKey, overideUnitsTo, "From Car");
        booleanPref(ORUnitsActiveKey, overideUnitsActive, false);
        stringPref(ORModelToKey, overideModelTo, "From Car");
        booleanPref(ORModelActiveKey, overideModelActive, false);
        stringPref(ORRoofToKey, overideRoofTo, "From Car");
        booleanPref(ORRoofActiveKey, overideRoofActive, false);
    }
    
    
    protected final Range<Long> getLoadPeriod() {
        Range<Long> range = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);

        long now = System.currentTimeMillis();
        LoadPeriod period = nameToLoadPeriod.get(loadPeriod.get());
        if (period == null) {
            period = LoadPeriod.All;
            loadPeriod.set(nameToLoadPeriod.inverse().get(period));
        }
        switch (period) {
            case None:
                range = Range.closed(now + 1000, now + 1000L); // Empty Range
                break;
            case Last7:
                range = Range.closed(now - (7 * 24 * 60 * 60 * 1000L), now);
                break;
            case Last14:
                range = Range.closed(now - (14 * 24 * 60 * 60 * 1000L), now);
                break;
            case Last30:
                range = Range.closed(now - (30 * 24 * 60 * 60 * 1000L), now);
                break;
            case ThisWeek:
                Range<Date> thisWeek = getThisWeek();
                range = Range.closed(
                        thisWeek.lowerEndpoint().getTime(),
                        thisWeek.upperEndpoint().getTime());
                break;
            case ThisMonth:
                Range<Date> thisMonth = getThisMonth();
                range = Range.closed(
                        thisMonth.lowerEndpoint().getTime(),
                        thisMonth.upperEndpoint().getTime());
                break;
            case All:
            default:
                break;

        }
        return range;
    }
    
/*------------------------------------------------------------------------------
 *
 * Preferences related to the Graphs Tab
 * 
 *----------------------------------------------------------------------------*/
     
    public BooleanProperty  ignoreGraphGaps = new SimpleBooleanProperty();
    public IntegerProperty  graphGapTime    = new SimpleIntegerProperty();
    public BooleanProperty  vsLimitEnabled  = new SimpleBooleanProperty();
    public ObjectProperty<CalTime>   vsFrom = new SimpleObjectProperty<>();
    public ObjectProperty<CalTime>   vsTo   = new SimpleObjectProperty<>();
    private static final String GraphIgnoreGapsKey  = "GRAPH_GAP_IGNORE";
    private static final String GraphGapTimeKey     = "GRAPH_GAP_TIME";
    private static final String VSLimitEnabledKey   = "VS_LIMIT_ENABLED";
    private static final String VSFromKey           = "VS_FROM";
    private static final String VSToKey             = "VS_TO";
    
    private void loadGraphPrefs() {
        booleanPref(GraphIgnoreGapsKey, ignoreGraphGaps, false);
        integerPref(GraphGapTimeKey, graphGapTime, 15); // 15 minutes 
        
        booleanPref(VSLimitEnabledKey, vsLimitEnabled, false);
        calTimePref(VSFromKey, vsFrom, new CalTime("10^00^PM"));
        calTimePref(VSToKey,   vsTo,   new CalTime("06^00^AM"));
    }
    
/*------------------------------------------------------------------------------
 *
 * Preferences related to the Scheduler Tab
 * 
 *----------------------------------------------------------------------------*/
    
    public BooleanProperty safeIncludesMinCharge    = new SimpleBooleanProperty();
    public BooleanProperty safeIncludesPluggedIn    = new SimpleBooleanProperty();
    private static final String SchedSafeInclBat    = "SCHED_SAFE_BATTERY";
    private static final String SchedSafeInclPlug   = "SCHED_SAFE_PLUGGED_IN";
    
    private void loadSchedulerPrefs() {
        booleanPref(SchedSafeInclBat, safeIncludesMinCharge, true);
        booleanPref(SchedSafeInclPlug, safeIncludesPluggedIn, false);
    }
    
/*------------------------------------------------------------------------------
 *
 * Preferences related to the Location Tab
 * 
 *----------------------------------------------------------------------------*/
    
    public BooleanProperty collectLocationData = new SimpleBooleanProperty();
    public BooleanProperty streamWhenPossible = new SimpleBooleanProperty();
    public IntegerProperty locMinTime = new SimpleIntegerProperty();
    public IntegerProperty locMinDist = new SimpleIntegerProperty();
    
    private static final String LocCollectData = "LOC_COLLECT_DATA";
    private static final String LocStreamMore = "LOC_STREAM_MORE";
    private static final String LocMinTime = "LOC_MIN_TIME";
    private static final String LocMinDist = "LOC_MIN_DIST";
    
    private void loadLocationPrefs() {
        booleanPref(LocCollectData, collectLocationData, true);
        booleanPref(LocStreamMore, streamWhenPossible, true);
        integerPref(LocMinTime, locMinTime, 5); // 5 Seconds
        integerPref(LocMinDist, locMinDist, 5); // 5 Meters
    }
    

/*------------------------------------------------------------------------------
 *
 * PRIVATE - Convenience Methods for handling preferences
 * 
 *----------------------------------------------------------------------------*/

    private void integerPref(final String key, IntegerProperty property, int defaultValue) {
        property.set(appContext.persistentState.getInt(key, defaultValue));
        property.addListener(new ChangeListener<Number>() {
            @Override public void changed(
                ObservableValue<? extends Number> ov, Number old, Number cur) {
                    appContext.persistentState.putInt(key, cur.intValue());
            }
        });
    }
    
    private void booleanPref(final String key, BooleanProperty property, boolean defaultValue) {
        property.set(appContext.persistentState.getBoolean(key, defaultValue));
        property.addListener(new ChangeListener<Boolean>() {
            @Override public void changed(
                ObservableValue<? extends Boolean> ov, Boolean old, Boolean cur) {
                    appContext.persistentState.putBoolean(key, cur);
            }
        });
    }
    
    private void stringPref(final String key, StringProperty property, String defaultValue) {
        property.set(appContext.persistentState.get(key, defaultValue));
        property.addListener(new ChangeListener<String>() {
            @Override public void changed(
                ObservableValue<? extends String> ov, String old, String cur) {
                    appContext.persistentState.put(key, cur);
            }
        });
    }
    
    
    private void calTimePref(final String key, ObjectProperty<CalTime> property, CalTime defaultValue) {
        String initial = appContext.persistentState.get(key, defaultValue.toString());
        property.set(new CalTime(initial));
        property.addListener(new ChangeListener<CalTime>() {
            @Override public void changed(
                ObservableValue<? extends CalTime> ov, CalTime old, CalTime cur) {
                    appContext.persistentState.put(key, cur.toString());
            }
        });
    }
    
/*------------------------------------------------------------------------------
 *
 * PRIVATE - Utility Methods
 * 
 *----------------------------------------------------------------------------*/
    
    private Range<Date> getThisWeek() {
        return getDateRange(Calendar.DAY_OF_WEEK);
    }
    
    private Range<Date> getThisMonth() {
        return getDateRange(Calendar.DATE);
    }
    
    private Range<Date> getDateRange(int dateField) {
        Calendar cal = Calendar.getInstance();
        cal.set(dateField, 1);
        Date start = cal.getTime();
        cal.set(dateField, cal.getActualMaximum(dateField));
        Date end = cal.getTime();
        return Range.closed(start, end);
    }

}
