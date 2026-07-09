package com.aetherix.search;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    private static final String TAG = "EnvLoader";
    private static Map<String, String> envVars = null;

    public static synchronized void load(Context context) {
        if (envVars != null) {
            return;
        }
        
        envVars = new HashMap<String, String>();

        try {
            InputStream is = context.getAssets().open(".env");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    String value = line.substring(eqIndex + 1).trim();
                    envVars.put(key, value);
                    Log.d(TAG, "Loaded env: " + key + "=***");
                }
            }
            
            reader.close();
            Log.d(TAG, "Environment loaded successfully with " + envVars.size() + " variables");
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to load .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        if (envVars == null) {
            return null;
        }
        return envVars.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static boolean has(String key) {
        return envVars != null && envVars.containsKey(key);
    }
}