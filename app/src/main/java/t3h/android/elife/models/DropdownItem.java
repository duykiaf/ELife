package t3h.android.elife.models;

import androidx.annotation.NonNull;

public class DropdownItem {
    private String hiddenValue;

    private String displayText;

    public DropdownItem(String hiddenValue, String displayText) {
        this.hiddenValue = hiddenValue;
        this.displayText = displayText;
    }

    public String getHiddenValue() {
        return hiddenValue;
    }

    public void setHiddenValue(String hiddenValue) {
        this.hiddenValue = hiddenValue;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    @NonNull
    @Override
    public String toString() {
        return displayText;
    }
}
