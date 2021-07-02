package com.luci.pano_touch.honghu_gamepad;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UtilsLoadLayout {
    private final static String TAG = "ResourceUtils";

    /**
     * 从 keys_base.xml 中解析按键，生成 HonghuKey 基本数据类
     * @param c
     * @param hashMapResId
     * @return
     */
    public static Map<String, HonghuKey> parseKeysBase(Context c, int hashMapResId) {
        Map<String, HonghuKey> map = new HashMap<>();
        XmlResourceParser parser = c.getResources().getXml(hashMapResId);
        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
//                    Log.v(TAG, "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("map")) {
                        boolean isLinked = parser.getAttributeBooleanValue(null, "linked", false);
                    } else {
//                        提取属性（当namespace为空时的写法）：https://stackoverflow.com/a/46520446/9422455
                        String key = parser.getName();
                        String icon = parser.getAttributeValue(null, "icon");
                        int code = parser.getAttributeIntValue( null, "code", 0);
                        int id = parser.getAttributeIntValue(null, "id", 0);
                        HonghuKey mHonghuKey = new HonghuKey();
                        mHonghuKey.icon = icon;
                        mHonghuKey.code = code;
                        mHonghuKey.id = id;
                        map.put(key, mHonghuKey);
                    }
                } else {
//                    Log.v(TAG, String.format("event type: %d", eventType));
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return map;
    }

    /**
     * 从 keys_layout_1920x1080.xml 中基于键名解析生成 HonghuKeysBean (含 HonghuJoystickBean )
     * @param c
     * @param hashMapResId
     * @return
     */
    public static Map<String, Object> parseKeysLayout(Context c, int hashMapResId) {
        Map<String, Object> map = null;
        XmlResourceParser parser = c.getResources().getXml(hashMapResId);
        String key, value, type;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
//                    Log.v(TAG, "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("map")) {
                        boolean isLinked = parser.getAttributeBooleanValue(null, "linked", false);

                        map = isLinked ? new LinkedHashMap<String, Object>() : new HashMap<String, Object>();
                    } else {
//                        提取属性（当namespace为空时的写法）：https://stackoverflow.com/a/46520446/9422455
                        type = parser.getName();
                        key = parser.getAttributeValue(null, "name");
                        value = parser.getAttributeValue(null, "value");
                        switch (type) {
                            case "int":
                                map.put(key, Integer.parseInt(value));
                                break;
                            case "boolean":
                                map.put(key, Boolean.parseBoolean(value));
                                break;
                            default:
                                map.put(key, value);
                                break;
                        }
                    }
                } else {
//                    Log.v(TAG, String.format("event type: %d", eventType));
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return map;
    }

}
