package org.rsminion.core.matching.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.rsminion.classes.RSClass;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.multipliers.MultiplierCache;
import org.rsminion.tools.utils.Utils;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RSHook {

    public static final RSHook[] EMPTY_ARRAY = new RSHook[0];

    private String name;

    private String obfName;

    private String hookHolder; //not the owner, just the class that holds it, may be the owner if its not static

    private String obfOwner;

    private String desc;

    private String descRequired;

    private boolean staticField;

    private String multiplier;

    private Matchers.Importance importance;

    public RSHook(String hookHolder, String name, String descRequired, boolean staticField, Matchers.Importance importance) {
        this.hookHolder = hookHolder;
        this.name = name;
        this.descRequired = descRequired;
        this.staticField = staticField;
        this.importance = importance;
    }

    public void submitData(String obfOwner, String obfName, String desc) {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.desc = desc;
        this.multiplier = MultiplierCache.get(this);
    }

    public boolean isFound() {
        return obfName != null;
    }

    public boolean isMismatched() {
        if(descRequired.contains("#")) {
            int classIndex = descRequired.indexOf("#");
            RSClass clazz = Matchers.getClass(descRequired.substring(classIndex + 1));
            if(clazz != null) {
                StringBuilder descBuilder = new StringBuilder();
                for(int i = 0; i < classIndex; i++)
                    descBuilder.append("[");
                descBuilder.append(Utils.formatAsClass(clazz.getObfName()));
                return !descBuilder.toString().equals(desc);
            }
            return true;
        }
        return !desc.equalsIgnoreCase(descRequired);
    }

}
