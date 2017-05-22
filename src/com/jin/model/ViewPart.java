package com.jin.model;


import com.jin.util.Definitions;
import com.jin.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaeger
 * 15/11/25
 */
public class ViewPart {
    private static final String OUTPUT_DECLARE_STRING = "private var %s: %s?=null;\n";
    private static final String OUTPUT_DECLARE_STRING_NOT_PRIVATE = "var %s: %s?=null;\n";
   /* private static final String OUTPUT_FIND_VIEW_STRING = "%s = (%s) findViewById(R.id.%s);\n";
    private static final String OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW = "%s = (%s) %s.findViewById(R.id.%s);\n";
    private static final String OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER = "viewHolder.%s = (%s) %s.findViewById(R.id.%s);\n";*/
    private static final String OUTPUT_FIND_VIEW_STRING = "%s = findViewById(R.id.%s) as %s;\n";
    private static final String OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW = "%s = %s.findViewById(R.id.%s)as %s;\n";
    private static final String OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER = "viewHolder.%s = %s.findViewById(R.id.%s)as %s;\n";
    private String type;
    private String typeFull;
    private String id;
    private String name;
    private boolean selected;

    public ViewPart() {
        selected = true;
    }

    private void generateName(String id) {
        Pattern pattern = Pattern.compile("_([a-zA-Z])");
        Matcher matcher = pattern.matcher(id);

        char[] chars = id.toCharArray();
        while (matcher.find()) {
            int index = matcher.start(1);
            chars[index] = Character.toUpperCase(chars[index]);
        }
        String name = String.copyValueOf(chars);
        name = name.replaceAll("_", "");
        setName(name);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        String[] packages = type.split("\\.");
        if (packages.length > 1) {
            this.typeFull = type;
            this.type = packages[packages.length - 1];
        } else {
            this.typeFull = null;
            this.type = type;
        }
    }

    public String getTypeFull() {
        return typeFull;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        generateName(id);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDeclareString(boolean isViewHolder, boolean isShow) {
        if (isViewHolder) {
            return String.format(OUTPUT_DECLARE_STRING_NOT_PRIVATE, name, type);
        } else {
            if (isShow) {
                return String.format(OUTPUT_DECLARE_STRING, name, type);
            }

            String realType;
            if (!Utils.isEmptyString(getTypeFull())) {
                realType = getTypeFull();
            } else if (Definitions.paths.containsKey(getType())) {
                realType = Definitions.paths.get(getType());
            } else {
                realType = "android.widget." + getType();
            }
            return String.format(OUTPUT_DECLARE_STRING, name, realType);
        }
    }


    public String getFindViewStringWithRootView(String rootView) {
        return String.format(OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW, name,  rootView, id ,type);
    }

    public String getFindViewString() {

        return String.format(OUTPUT_FIND_VIEW_STRING, name, id, type);
    }

    public void resetName() {
        generateName(id);
    }

    public void addM2Name() {
        generateName("m_" + id);
    }

    public String getFindViewStringForViewHolder(String rootView) {
        return String.format(OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER, name,  rootView, id,type);
    }

    @Override
    public String toString() {
        return "ViewPart{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                '}';
    }
}

