package lemma.lemmavideosdk.vast.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import lemma.lemmavideosdk.common.LMLog;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LMLocationManager {

    final private static String TAG = "PMLocationDetector";
    final private Context context;
    private Location location;
    private LocationManager locationManager;

    public LMLocationManager(Context context) {
        this.context = context;
    }

    public static boolean hasPermission(Context context, String permission) {
        return !(context == null || permission == null) &&
                (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Method is used to get location on the basic of LocationProvider(GPS, Network)
     *
     * @return instance of Location class
     */
    public Location getLocation() {

        if (LOCATION_PROVIDER.GPS.hasLocationPermission(context) || LOCATION_PROVIDER.NETWORK.hasLocationPermission(context)) {
            if (shouldUpdateLocation()) {
                Location gpsLocation = getLocationFromProvider(context, LOCATION_PROVIDER.GPS);
                Location networkLocation = getLocationFromProvider(context, LOCATION_PROVIDER.NETWORK);

                location = getRecentLocation(gpsLocation, networkLocation);

                // Check for Passive provider in case location is not received from gps and network
                if (location == null) {
                    location = getLocationFromProvider(context, LOCATION_PROVIDER.PASSIVE);
                }

                return location;
            } else {
                return location;
            }
        } else {
            return null;
        }
    }

    /**
     * Method used to check if there is any change in last location
     *
     * @return true when any update happens in last location else returns false
     */
    private boolean shouldUpdateLocation() {
        return true;
    }

    /**
     * Method is used to get LocationManager
     *
     * @param context android app context
     * @return instance of LocationManager
     */
    private LocationManager getLocationManager(Context context) {
        if (null == locationManager) {
            locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager;
    }

    /**
     * This method is used to get location from particular location provider
     * Provides last know location
     *
     * @param context  android app context
     * @param provider location provider on which location is fetched
     * @return instance of Location
     */
    @SuppressLint("MissingPermission")
    private Location getLocationFromProvider(Context context, LOCATION_PROVIDER provider) {

        Location location = null;

        if (provider.hasLocationPermission(context)) {
            final LocationManager locationManager = getLocationManager(context);

            if (locationManager != null) {
                try {
                    location = locationManager.getLastKnownLocation(provider.toString());
                } catch (IllegalArgumentException ex) {
                    LMLog.e(TAG, "Unable to fetch the location in PM SDK");
                } catch (SecurityException ex) {
                    LMLog.e(TAG, "Unable to fetch the location in PM SDK due to lack of permission");
                } catch (Exception ex) {
                    LMLog.e(TAG, "Unable to fetch the location in PM SDK due to unknown reason");
                }
            }
        }
        return location;
    }

    /**
     * Method used to get update location by comparing location retrieved different sources
     *
     * @param locationOne location retrieved from source one
     * @param locationTwo location retrieved from source two
     * @return instance of updated Location
     */
    private Location getRecentLocation(Location locationOne, Location locationTwo) {
        if (locationOne == null) {
            return locationTwo;
        }
        if (locationTwo == null) {
            return locationOne;
        }
        return (locationOne.getTime() > locationTwo.getTime()) ? locationOne : locationTwo;
    }

    /**
     * Enum is defined to manage location provider
     * Network, GPS
     */
    private enum LOCATION_PROVIDER {
        NETWORK("network"), GPS("gps"), PASSIVE("passive");

        private final String value;

        LOCATION_PROVIDER(String val) {
            this.value = val;
        }

        public String toString() {
            return value;
        }

        /**
         * checks is app has the permission for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION respective of network provider
         *
         * @param context android app context
         * @return true if
         */
        boolean hasLocationPermission(Context context) {
            switch (this) {
                case NETWORK:
                    return hasPermission(context, ACCESS_FINE_LOCATION)
                            || hasPermission(context, ACCESS_COARSE_LOCATION);
                case GPS:
                    return hasPermission(context, ACCESS_FINE_LOCATION);
                case PASSIVE:
                    return hasPermission(context, ACCESS_FINE_LOCATION);
                default:
                    return false;
            }
        }
    }
}
